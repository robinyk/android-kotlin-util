package com.ownapp.core.extensions.resource

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.logException
import java.io.File


/**
 * Updated by Robin on 2020/12/4
 */

val Uri.isExternalStorageDocument: Boolean
	get() = authority == "com.android.externalstorage.documents"

val Uri.isDownloadsDocument: Boolean
	get() = authority == "com.android.providers.downloads.documents"

val Uri.isMediaDocument: Boolean
	get() = authority == "com.android.providers.media.documents"

val Uri.isGooglePhotosUri: Boolean
	get() = authority == "com.google.android.apps.photos.content"

fun Uri.toDrawable(context: Context): Drawable? =
	context.contentResolver.openInputStream(this)?.let {
		 Drawable.createFromStream(it, this.toString())
	}

fun File.toBitmap(): Bitmap?
{
	try
	{
		if(exists())
			return BitmapFactory.decodeFile(absolutePath)
	}
	catch(e: Exception)
	{
		e.message.logError()
	}
	
	return null
}

fun File.saveBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 100)
{
	outputStream().use {
		bitmap.compress(format, quality, it)
		it.flush()
		it.close()
	}
}

fun Drawable.toFile(
	context: Context,
	fileName: String = System.currentTimeMillis().toString(),
	format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
	quality: Int = 100
): File = bitmap.toFile(context, fileName, format, quality)

fun Bitmap.toFile(
	context: Context,
	fileName: String = System.currentTimeMillis().toString(),
	format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
	quality: Int = 100
): File
{
	// Create a file to write bitmap data
	return File(
		context.cacheDir, fileName + when (format) {
			Bitmap.CompressFormat.JPEG -> ".jpeg"
			Bitmap.CompressFormat.PNG -> ".png"
			else -> ".png"
		}
	).apply {
		createNewFile()
		saveBitmap(this@toFile, format, quality)
	}
}

val String.mimeType: String?
	get() = try
	{
		MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(this))
	}
	catch (e: Exception)
	{
		e.logException()
		null
	}

val File.mimeType: String?
	get() = path.mimeType

val File.isImage: Boolean
	get() = mimeType?.startsWith("image") == true

val File.isVideo: Boolean
	get() = mimeType?.startsWith("video") == true



@Suppress("DEPRECATION")
fun Uri.getRealPath(context: Context): String?
{
	// DocumentProvider
	if (DocumentsContract.isDocumentUri(context, this))
	{
		// ExternalStorageProvider
		if (isExternalStorageDocument)
		{
			val docId = DocumentsContract.getDocumentId(this)
			val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			val type = split[0]

			if ("primary".equals(type, ignoreCase = true))
			{
				return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
			}
		}
		else if (isDownloadsDocument)
		{
			var cursor: Cursor? = null

			try
			{
				cursor = context.contentResolver.query(
					this, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
				)
				cursor?.moveToNext()

				val fileName = cursor?.getString(0)
				val path = Environment.getExternalStorageDirectory().toString() + "/Download/" + fileName.orEmpty()

				if (!TextUtils.isEmpty(path))
					return path
			}
			finally
			{
				cursor?.close()
			}

			val id = DocumentsContract.getDocumentId(this)

			if (id.startsWith("raw:"))
				return id.replaceFirst("raw:".toRegex(), "")

			val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads"), java.lang.Long.valueOf(id))
			return getDataColumn(context, contentUri, null, null)
		}
		else if (isMediaDocument)
		{
			val docId = DocumentsContract.getDocumentId(this)
			val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
			val type = split[0]

			var contentUri: Uri? = null

			when (type)
			{
				"image" -> contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
				"video" -> contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
				"audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
			}

			val selection = "_id=?"
			val selectionArgs = arrayOf(split[1])

			return getDataColumn(context, contentUri, selection, selectionArgs)
		}// MediaProvider
		// DownloadsProvider
	}
	else if ("content".equals(this.scheme.orEmpty(), ignoreCase = true)) {
		// Return the remote address
		return if (isGooglePhotosUri) this.lastPathSegment else getDataColumn(context, this, null, null)
	}
	else if ("file".equals(this.scheme.orEmpty(), ignoreCase = true))
	{
		return this.path
	}// File
	// MediaStore (and general)

	return null
}

private fun getDataColumn(context: Context, uri: Uri?, selection: String?, selectionArgs: Array<String>?): String?
{
	if(uri == null)
		return null

	var cursor: Cursor? = null
	val column = "_data"
	val projection = arrayOf(column)

	try
	{
		cursor = context.contentResolver.query(uri, projection, selection, selectionArgs, null)

		if (cursor != null && cursor.moveToFirst()) {
			val index = cursor.getColumnIndexOrThrow(column)
			return cursor.getString(index)
		}
	}
	finally
	{
		cursor?.close()
	}

	return null
}