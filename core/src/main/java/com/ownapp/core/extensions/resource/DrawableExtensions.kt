package com.ownapp.core.extensions.resource

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.util.Base64
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.graphics.drawable.DrawableCompat
import com.ownapp.core.extensions.utility.getEnabledColorListOrDefault
import com.ownapp.core.extensions.utility.logException
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.roundToInt

/**
 * Updated by Robin on 2020/12/4
 */

fun Drawable?.recycleBitmap() = (this as? BitmapDrawable?)?.bitmap?.tryRecycle()

val <T: Drawable> T.bitmap: Bitmap
	get()
	{
		if (this is BitmapDrawable && bitmap != null)
			return bitmap

		val bitmap = if(intrinsicWidth <= 0 || intrinsicHeight <= 0)
		{
			// Single color bitmap will be created of 1x1 pixel
			Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
		}
		else
			Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)

		Canvas(bitmap).apply {
			setBounds(0, 0, width, height)
			draw(this)
		}

		return bitmap
	}

fun <T: Drawable> T.bytesEqualTo(drawable: T?) = bitmap.bytesEqualTo(drawable?.bitmap, true)

fun <T: Drawable> T.pixelsEqualTo(drawable: T?) = bitmap.pixelsEqualTo(drawable?.bitmap, true)

/**
 * Wrap the color into a state and tint the drawable
 */
fun Drawable.tint(@ColorInt color: Int): Drawable = tint(ColorStateList.valueOf(color))

/**
 * Tint the drawable with a given color state list
 */
fun Drawable.tint(colorStateList: ColorStateList): Drawable
{
	val drawable = DrawableCompat.wrap(mutate())
	DrawableCompat.setTintList(drawable, colorStateList)
	return drawable
}

fun Drawable.getTintedDrawable(@ColorInt color: Int, tintMode: PorterDuff.Mode? = PorterDuff.Mode.SRC_IN): Drawable
{
	return getTintedDrawable(ColorStateList.valueOf(color), tintMode)
}

fun Drawable.getTintedDrawable(tintList: ColorStateList?, tintMode: PorterDuff.Mode? = PorterDuff.Mode.SRC_IN): Drawable
{
	DrawableCompat.wrap(this).mutate().let {
		if (tintList != null)
			DrawableCompat.setTintList(it, tintList.getEnabledColorListOrDefault())

		if (tintMode != null)
			DrawableCompat.setTintMode(it, tintMode)

		return it
	}
}

fun Drawable.setSize(width: Int, height: Int)
{
	bounds = Rect(0, 0, width, height)
}

val LayerDrawable.layers: List<Drawable>
	get() = (0 until numberOfLayers).map { getDrawable(it) }

fun Bitmap?.tryRecycle() { if(this != null && !isRecycled) recycle() }

/**
 * Converts this Bitmap to a Drawable
 */
fun Bitmap.toDrawable(context: Context): Drawable = BitmapDrawable(context.resources, this)

fun Bitmap.bytesEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
	if(width == other.width && height == other.height)
	{
		toBytes().contentEquals(other.toBytes()).let {
			if (shouldRecycle)
				recycle().also { otherBitmap.recycle() }

			it
		}
	}
	else
		false
} ?: false

fun Bitmap.pixelsEqualTo(otherBitmap: Bitmap?, shouldRecycle: Boolean = false) = otherBitmap?.let { other ->
	if(width == other.width && height == other.height)
	{
		toPixels().contentEquals(other.toPixels()).let {
			if(shouldRecycle)
				recycle().also { otherBitmap.recycle() }

			it
		}
	}
	else
		false
} ?: false

fun Bitmap.toBytes(): ByteArray = ByteArrayOutputStream().use { stream ->
	compress(Bitmap.CompressFormat.JPEG, 100, stream)
	stream.toByteArray()
}

fun Bitmap.toPixels() = IntArray(width * height).apply { getPixels(this, 0, width, 0, 0, width, height) }

fun Bitmap.downScale(
	format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
	, quality: Int = 80
	, maxSize: Int = 1080
): Bitmap
{
	try
	{
		ByteArrayOutputStream().let { os ->
			compress(format, quality, os)
			os.toByteArray().run {
				BitmapFactory.decodeByteArray(this, 0, this.size, BitmapFactory.Options()
					.apply { inScaled = false })
					.let {
						var desiredWidth = it.width
						var desiredHeight = it.height

						if(height > maxSize)
						{
							val percentage: Float = 1 - ((height - maxSize) / height.toFloat())
							desiredWidth = (desiredWidth * percentage).roundToInt()
							desiredHeight = (desiredHeight * percentage).roundToInt()
						}

						return Bitmap.createScaledBitmap(it, desiredWidth, desiredHeight, false)
					}
			}
		}
	}
	catch(e: Exception)
	{
		e.logException()
		return this
	}
}

fun Bitmap.toBase64(): String?
{
	try
	{
		ByteArrayOutputStream().let { os ->
			this.compress(Bitmap.CompressFormat.PNG, 50, os)
			return Base64.encodeToString(os.toByteArray(), Base64.DEFAULT)
		}
	} catch(e: Exception)
	{
		e.logException()
	}


	return null
}

fun Bitmap.overlay(overlay: Bitmap): Bitmap
{
	Bitmap.createBitmap(width, height, config).let {
		Canvas(it).run {
			drawBitmap(this@overlay, Matrix(), null)
			drawBitmap(overlay, Matrix(), null)
		}
		return it
	}
}

fun Bitmap.toIcon(): Icon = Icon.createWithBitmap(this)

/**
 * Blend the Bitmap Corners to Round with Given radius
 */
fun Bitmap.toRoundCorner(radius: Float, borderSize: Float = 0f, @ColorInt borderColor: Int = 0, recycle: Boolean = true): Bitmap
{
	val width = width
	val height = height
	val paint = Paint(Paint.ANTI_ALIAS_FLAG)
	val ret = Bitmap.createBitmap(width, height, config)
	val shader = BitmapShader(this, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
	paint.shader = shader
	val canvas = Canvas(ret)
	val rectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
	val halfBorderSize = borderSize / 2f
	rectF.inset(halfBorderSize, halfBorderSize)
	canvas.drawRoundRect(rectF, radius, radius, paint)
	if (borderSize > 0) {
		paint.shader = null
		paint.color = borderColor
		paint.style = Paint.Style.STROKE
		paint.strokeWidth = borderSize
		paint.strokeCap = Paint.Cap.ROUND
		canvas.drawRoundRect(rectF, radius, radius, paint)
	}
	if (recycle && !isRecycled && ret != this) recycle()
	return ret
}