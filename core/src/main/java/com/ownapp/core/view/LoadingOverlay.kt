package com.ownapp.core.view

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.View
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.annotation.FloatRange
import androidx.annotation.IntDef
import androidx.annotation.RawRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.ownapp.core.R
import com.ownapp.core.annotation.ResourceType
import com.ownapp.core.databinding.ViewLoadingOverlayBinding
import com.ownapp.core.extensions.layoutInflater
import com.ownapp.core.extensions.resource.getResourceTypeName
import com.ownapp.core.extensions.resource.getStyleableColorStateList
import com.ownapp.core.extensions.resource.toColor
import com.ownapp.core.extensions.utility.dp
import com.ownapp.core.extensions.utility.logException
import com.ownapp.core.extensions.view.applyTintToDrawable
import com.ownapp.core.extensions.view.gone
import com.ownapp.core.extensions.view.show
import com.ownapp.core.util.ViewUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Updated by Robin on 2020/12/4
 */

class LoadingOverlay: ConstraintLayout
{
	//**--------------------------------------------------------------------------------------------------
	//*     Enum
	//---------------------------------------------------------------------------------------------------*/
	@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
	@IntDef(Type.MATERIAL, Type.ANIMATION)
	annotation class Type
	{
		companion object
		{
			const val MATERIAL = 0
			const val ANIMATION = 1
		}
	}


	//**--------------------------------------------------------------------------------------------------
	//*     Variable
	//---------------------------------------------------------------------------------------------------*/
	companion object
	{
		// Value
		private const val MINIMUM_LOADING_MILLISECONDS = 500L // 0.5 seconds loading time
		private var runningTask = 0
		val isLoading: Boolean
			get() = runningTask > 0

		fun show(activity: Activity)
		{
			GlobalScope.launch(Dispatchers.Main) {
				try
				{
					runningTask++

					if(runningTask > 0)  // != 1
					{
						activity.findViewById<View>(R.id.loading_overlay_container)?.run {
							show()
							activity.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
						}
					}
				}
				catch(e: Exception)
				{
					e.logException()
				}
			}
		}

		fun hide(activity: Activity)
		{
			GlobalScope.launch(Dispatchers.Main) {
				delay(MINIMUM_LOADING_MILLISECONDS)

				try
				{
					if(runningTask > 0)
						runningTask--

					if(runningTask <= 0)
					{
						activity.findViewById<View>(R.id.loading_overlay_container)?.run {
							gone()
							activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
						}
					}
				}
				catch(e: Exception)
				{
					e.logException()
				}
			}
		}

		fun reset(activity: Activity)
		{
			runningTask = 0

			activity.findViewById<View>(R.id.loading_overlay_container)?.run {
				gone()
				activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
			}
		}

		fun Activity.showLoading() = show(this)
		fun Activity.hideLoading() = hide(this)
		fun Activity.resetLoading() = reset(this)
	}

	// Class
	private lateinit var binding: ViewLoadingOverlayBinding

	// Value
	@Type private var type = Type.MATERIAL

	private var tintList: ColorStateList? = null
		set(value)
		{
			if (field !== value)
			{
				field = value
				applyTint()
			}
		}

	private var tintMode: PorterDuff.Mode? = null
		set(value)
		{
			if (field != value)
			{
				field = value
				applyTint()
			}
		}

	var widthPercent = 0.5f
		set(@FloatRange(from = 0.0, to = 1.0) value)
		{
			if(field != value)
			{
				field = value

				ConstraintSet().run {
					clone(binding.loadingOverlayContainer)
					constrainPercentWidth(binding.loadingOverlayImageView.id, widthPercent)
					applyTo(binding.loadingOverlayContainer)
				}
			}
		}


	//**--------------------------------------------------------------------------------------------------
	//*     Constructor
	//---------------------------------------------------------------------------------------------------*/
	constructor(context: Context): this(context, null)

	constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.loadingOverlayStyle)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
			: super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes)
	{
		try
		{
			binding = ViewLoadingOverlayBinding.inflate(context.layoutInflater, this, true)
			initialize(attrs, defStyleAttr, defStyleRes)
		}
		catch(e: Exception)
		{
			e.logException()
		}
	}


	//**--------------------------------------------------------------------------------------------------
	//*     Initialize
	//---------------------------------------------------------------------------------------------------*/
	private fun initialize(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
	{
		if(attrs != null)
		{
			context.obtainStyledAttributes(attrs, R.styleable.LoadingOverlay, defStyleAttr, defStyleRes).run {
				type = getInt(R.styleable.LoadingOverlay_loadingType, Type.MATERIAL)

				getResourceId(R.styleable.LoadingOverlay_animationResourceId, -1).let { animationResourceId ->
					if(animationResourceId != -1)
						type = Type.ANIMATION

					when(type)
					{
						Type.MATERIAL -> binding.loadingOverlayImageView.let { imageView ->
							imageView.setImageDrawable(CircularProgressDrawable(context).apply {
								setColorSchemeColors(R.attr.colorPrimary.toColor(context))
								centerRadius = 32.dp.toFloat()
								strokeWidth = centerRadius / 5
								start()
							})
						}

						Type.ANIMATION -> setAnimationResource(animationResourceId)
					}
				}

				if (hasValue(R.styleable.LoadingOverlay_android_tint))
					tintList = context.getStyleableColorStateList(this, R.styleable.LoadingOverlay_android_tint)

				if(hasValue(R.styleable.LoadingOverlay_android_tintMode))
					tintMode = ViewUtil.parsePorterDuffMode(getInt(R.styleable.LoadingOverlay_android_tintMode, -1), PorterDuff.Mode.SRC_IN)

				widthPercent = getFloat(R.styleable.LoadingOverlay_widthPercent, if(type == Type.MATERIAL) 1f else widthPercent)

				recycle()
			}
		}
	}


	//**--------------------------------------------------------------------------------------------------
	//*     Private
	//---------------------------------------------------------------------------------------------------*/
	private fun applyTint() = binding.loadingOverlayImageView.applyTintToDrawable(tintList, tintMode)


	//**--------------------------------------------------------------------------------------------------
	//*     Setter
	//---------------------------------------------------------------------------------------------------*/
	/**
	 * Set the animation resource for Animation type
	 *
	 * @param resourceId Resource ID of the loading animation file From drawable(Animation)/raw(gif)
	 */
	fun setAnimationResource(@DrawableRes @RawRes resourceId: Int)
	{
		if(type != Type.ANIMATION)
			return

		try
		{
			when(resourceId.getResourceTypeName(context))
			{
				ResourceType.DRAWABLE ->
				{
					Glide.with(context)
						.load(resourceId)
						.into(binding.loadingOverlayImageView)
				}

				ResourceType.RAW ->
				{
					Glide.with(context)
						.asGif()
						.load(resourceId)
						.into(binding.loadingOverlayImageView)
				}
			}
		}
		catch(e: Exception)
		{
			e.logException()
		}
	}


	//**--------------------------------------------------------------------------------------------------
	//*     Override
	//---------------------------------------------------------------------------------------------------*/
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)

		val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
		val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

		val calculatedWidth = ViewUtil.measureDimension(max(desiredWidth, measuredWidth), widthMeasureSpec)
		val calculatedHeight = ViewUtil.measureDimension(max(desiredHeight, measuredHeight), heightMeasureSpec)

		setMeasuredDimension(calculatedWidth, calculatedHeight)

		measureChildren(
			MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.getMode(widthMeasureSpec))
			, MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.getMode(heightMeasureSpec))
		)
	}
}