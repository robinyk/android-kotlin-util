package com.ownapp.core.extensions.view

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import coil.ImageLoader
import coil.load
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.signature.ObjectKey
import com.google.android.material.imageview.ShapeableImageView
import com.ownapp.core.R
import com.ownapp.core.extensions.resource.*
import com.ownapp.core.extensions.utility.dp
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.view.AspectRatioImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min


/**
 * Updated by Robin on 2020/12/4
 */

infix fun ImageView.set(@DrawableRes id: Int) = setImageResource(id)
infix fun ImageView.set(bitmap: Bitmap?) = setImageBitmap(bitmap)
infix fun ImageView.set(drawable: Drawable?) = setImageDrawable(drawable)
infix fun ImageView.set(icon: Icon) = setImageIcon(icon)
infix fun ImageView.set(uri: Uri) = setImageURI(uri)

@BindingAdapter("imageQR")
fun ImageView.setImageQr(text: CharSequence?)
{
	if(!text.isNullOrBlank())
	{
		GlobalScope.launch(Dispatchers.IO) {
			text.toString().toQrBitmap()?.let {
				withContext(Dispatchers.Main) {
					setImageBitmap(it)
				}
			}
		}
	}
}

@BindingAdapter(
	value = ["coil", "placeholder", "error", "placeholderTint", "android:tint", "circleCrop", "imageRadius", "downScale", "showLoading", "crossfade"],
	requireAll = false
)
fun ImageView.loadImageWithCoil(
	obj: Any?,
	placeholder: Any? = null,
	errorDrawable: Any? = null,
	placeholderTint: Any? = null,
	tint: Any? = null,
	circleCrop: Boolean? = false,
	imageRadius: Float? = 0f,
	downScale: Boolean? = null,
	showLoading: Boolean? = false,
	isCrossFade: Boolean? = false
)
{
	GlobalScope.launch(Dispatchers.IO) {
		ImageRequest.Builder(context).apply {
			when(obj)
			{
				is String -> data(obj as String?)
				is Uri -> data(obj as Uri?)
				is Drawable -> data(obj as Drawable?)
				is Bitmap -> data(obj as Bitmap?)
				is Int -> data(obj as Int?)
				is File -> data(obj as File?)
				else -> data(obj)
			}

			if(imageRadius != null && imageRadius > 0f)
				transformations(RoundedCornersTransformation(imageRadius))
			else if(circleCrop == true)
				transformations(CircleCropTransformation())

			listener(onStart = {
				if (showLoading == true && placeholder == null) setImageDrawable(CircularProgressDrawable(context).apply {
					setColorSchemeColors(R.attr.colorPrimary.toColor(context))
					centerRadius = max(min(layoutParams.width, layoutParams.height), 12.dp).toFloat()
					strokeWidth = centerRadius / 5
					start()
				})
				else if (drawable == null) {
					when (placeholder) {
						is Int -> set(placeholder)
						is Drawable -> set(placeholder)
					}

					if (drawable != null) {
						when (placeholderTint) {
							is Int -> applyTintToDrawable(placeholderTint.toColorStateList(context))
							is ColorStateList -> applyTintToDrawable(placeholderTint)
						}
					}
				}
			}, onError = { _: ImageRequest, throwable: Throwable ->
				when (val error = errorDrawable ?: placeholder) {
					is Int -> setImageDrawable(ContextCompat.getDrawable(context, error))
					is Drawable -> setImageDrawable(error)
				}

				if (drawable != null) {
					when (placeholderTint) {
						is Int -> applyTintToDrawable(placeholderTint.toColorStateList(context))
						is ColorStateList -> applyTintToDrawable(placeholderTint)
					}
				}

				throwable.message.logError()
			})

			target { drawable ->
				if(getDrawable() != drawable)
				{
					doOnLayout {
						load(
							if (downScale == true) drawable.bitmap.downScale()
							else drawable.bitmap
						) {
							crossfade(isCrossFade == true)
							listener(onSuccess = { _: ImageRequest, _: ImageResult.Metadata ->
								when (tint) {
									is Int -> applyTintToDrawable(tint.toColorStateList(context))
									is ColorStateList -> applyTintToDrawable(tint)
								}
							}, onError = { _: ImageRequest, throwable: Throwable ->
								when (val error = errorDrawable ?: placeholder) {
									is Int -> setImageDrawable(ContextCompat.getDrawable(context, error))
									is Drawable -> setImageDrawable(error)
								}

								if (getDrawable() != null) {
									when (placeholderTint) {
										is Int -> applyTintToDrawable(placeholderTint.toColorStateList(context))
										is ColorStateList -> applyTintToDrawable(placeholderTint)
									}
								}

								throwable.message.logError()
							})
						}
					}
				}
			}

			allowHardware(false)
		}.build().let { request ->
			ImageLoader(context).enqueue(request)
		}
	}
}

@BindingAdapter(
	value = ["android:src", "placeholder", "placeholderTint", "circleCrop", "cornerRadius", "crossFade"
		, "showLoading", "autoResize", "signature"
	], requireAll = false
)
fun ImageView.loadImage(
	obj: Any?,
	placeholder: Any? = null,
	placeholderTint: Any? = null,
	circleCrop: Boolean? = false,
	@Px imageCornerRadius: Float? = 0f,
	isCrossFade: Boolean? = false,
	showLoading: Boolean? = false,
	isAutoResize: Boolean? = false
	, signature: String? = null
)
{
	Glide.with(this)
		.asBitmap()
		.load(when (obj) {
			is String -> obj
			is Uri -> obj
			is Drawable -> obj
			is Bitmap -> obj
			is Int -> obj
			is File -> obj
			else -> obj
		})
		.apply {
			val options = RequestOptions()
			
			when(scaleType)
			{
				ImageView.ScaleType.CENTER_CROP -> options.centerCrop()
				ImageView.ScaleType.CENTER_INSIDE -> options.centerInside()
				ImageView.ScaleType.FIT_CENTER -> options.fitCenter()
				else -> {}
			}
			
			if(showLoading == true && placeholder == null)
				setImageDrawable(CircularProgressDrawable(context).apply {
					setColorSchemeColors(R.attr.colorPrimary.toColor(context))
					centerRadius = max(min(layoutParams.width, layoutParams.height), 12.dp).toFloat()
					strokeWidth = centerRadius / 5
					start()
				})
			else if(drawable == null)
			{
				when(placeholder)
				{
					is Int ->
					{
						set(placeholder)
						options.error(placeholder)
					}
					is Drawable ->
					{
						set(placeholder)
						options.error(placeholder)
					}
				}

				if(drawable != null)
				{
					when(placeholderTint)
					{
						is Int -> applyTintToDrawable(placeholderTint.toColorStateList(context))
						is ColorStateList -> applyTintToDrawable(placeholderTint)
					}
				}
			}

			if(isCrossFade == true)
				transition(BitmapTransitionOptions.withCrossFade())

			if(circleCrop == true)
				options.circleCrop()
			else if(imageCornerRadius != null && imageCornerRadius > 0f)
				options.transform(RoundedCorners(imageCornerRadius.toInt()))

			if(!signature.isNullOrBlank())
				options.signature(ObjectKey(signature))
			
			apply(options)
			
			if(isAutoResize == true)
			{
				into(object: CustomTarget<Bitmap>() {
					override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?)
					{
						if(this@loadImage is AspectRatioImageView)
						{
							ratio = resource.width.toFloat() to resource.height.toFloat()
						}
						
						set(resource)
					}
					
					override fun onLoadCleared(placeholder: Drawable?)
					{
					}
				})
			}
			else into(this@loadImage)
		}
}

@BindingAdapter("base64Image")
fun ImageView.loadBase64Image(string: String?)
{
	GlobalScope.launch(Dispatchers.IO) {
		string?.bitmap?.let { bitmap ->
			doOnLayout {
				GlobalScope.launch(Dispatchers.Main) { setImageBitmap(bitmap) }
			}
		}
	}
}

fun ImageView.applyTintToDrawable(@ColorInt color: Int, tintMode: PorterDuff.Mode? = PorterDuff.Mode.SRC_IN)
{
	drawable.getTintedDrawable(color, tintMode).let {
		if (drawable !== it)
			setImageDrawable(it)
	}
}

fun ImageView.applyTintToDrawable(tintList: ColorStateList?, tintMode: PorterDuff.Mode? = PorterDuff.Mode.SRC_IN)
{
	drawable.getTintedDrawable(tintList, tintMode).let {
		if (drawable !== it)
			setImageDrawable(it)
	}
}