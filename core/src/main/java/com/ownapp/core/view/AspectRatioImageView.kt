package com.ownapp.core.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.ownapp.core.R
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.logException
import com.ownapp.core.util.ViewUtil
import com.ownapp.core.view.AspectRatioImageView.BindingAdapters.setRatio
import com.ownapp.core.view.recycler.NestedRecyclerView
import kotlin.math.max
import kotlin.math.min

class AspectRatioImageView: AppCompatImageView
{
	object BindingAdapters
	{
		@BindingAdapter("ratio")
		@JvmStatic fun AspectRatioImageView.setRatio(value: String?)
		{
			if(!value.isNullOrBlank())
			{
				if(value.contains(":"))
				{
					value.split(":").let {
						ratio = (it.getOrNull(0)?.toFloatOrNull() ?: 0f) to (it.getOrNull(1)?.toFloatOrNull() ?: 0f)
					}
				}
				else "Ratio doesn't contain correct format => $value".logError()
			}
		}
		
		@BindingAdapter("minRatio")
		@JvmStatic fun AspectRatioImageView.setMinRatio(value: String?)
		{
			if(!value.isNullOrBlank())
			{
				if(value.contains(":"))
				{
					value.split(":").let {
						minRatio = (it.getOrNull(0)?.toFloatOrNull() ?: 0f) to (it.getOrNull(1)?.toFloatOrNull() ?: 0f)
					}
				}
				else "Ratio doesn't contain correct format => $value".logError()
			}
		}
		
		@BindingAdapter("maxRatio")
		@JvmStatic fun AspectRatioImageView.setMaxRatio(value: String?)
		{
			if(!value.isNullOrBlank())
			{
				if(value.contains(":"))
				{
					value.split(":").let {
						maxRatio = (it.getOrNull(0)?.toFloatOrNull() ?: 0f) to (it.getOrNull(1)?.toFloatOrNull() ?: 0f)
					}
				}
				else "Ratio doesn't contain correct format => $value".logError()
			}
		}
	}
	
	var ratio: Pair<Float, Float>? = null
		set(value)
		{
			if(field != value)
			{
				field = value
				requestLayout()
				invalidate()
			}
		}
	
	var minRatio: Pair<Float, Float>? = null
		set(value)
		{
			if(field != value)
			{
				field = value
				requestLayout()
				invalidate()
			}
		}
	
	var maxRatio: Pair<Float, Float>? = null
		set(value)
		{
			if(field != value)
			{
				field = value
				requestLayout()
				invalidate()
			}
		}
	
	constructor(context: Context): this(context, null)
	
	constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)
	
	constructor(context: Context, attrs: AttributeSet?, defStyleRes: Int)
			: super(MaterialThemeOverlay.wrap(context, attrs, 0, defStyleRes), attrs, defStyleRes)
	{
		try
		{
			initialize(attrs, defStyleRes)
		}
		catch(e: Exception)
		{
			e.logException()
		}
	}
	
	private fun initialize(attrs: AttributeSet?, defStyleRes: Int)
	{
		if(attrs != null)
		{
			context.theme.obtainStyledAttributes(attrs, R.styleable.AspectRatioImageView, 0, defStyleRes).run {
				if(hasValue(R.styleable.AspectRatioImageView_ratio))
					setRatio(getString(R.styleable.AspectRatioImageView_ratio))
				
				recycle()
			}
		}
	}
	
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		
		if(ratio != null || minRatio != null || maxRatio != null)
		{
			val widthMode = MeasureSpec.getMode(widthMeasureSpec)
			val heightMode = MeasureSpec.getMode(heightMeasureSpec)
			
			val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
			val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
			
			var calculatedWidth = ViewUtil.measureDimension(max(desiredWidth, measuredWidth), widthMeasureSpec)
			var calculatedHeight = ViewUtil.measureDimension(max(desiredHeight, measuredHeight), heightMeasureSpec)
			
			if (widthMode == MeasureSpec.EXACTLY && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED))
			{
				val ratioHeight = ratio?.let { (calculatedWidth.toDouble() / it.first * it.second).toInt() } ?: 0
				val maxRatioHeight = maxRatio?.let { (calculatedWidth.toDouble() / it.first * it.second).toInt() } ?: 0
				
				minRatio?.let { minimumHeight = (calculatedWidth.toDouble() / it.first * it.second).toInt() }
				
				if(ratioHeight != 0)
				{
					calculatedHeight = if(maxRatioHeight != 0)
						min(desiredHeight, maxRatioHeight)
					else ratioHeight
				}
				else if(maxRatioHeight != 0 && calculatedHeight > maxRatioHeight)
					calculatedHeight = maxRatioHeight
				
				setMeasuredDimension(calculatedWidth, calculatedHeight)
			}
			else if (heightMode == MeasureSpec.EXACTLY && (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED))
			{
				val ratioWidth = ratio?.let { (calculatedHeight.toDouble() / it.second * it.first).toInt() } ?: 0
				val maxRatioWidth = maxRatio?.let { (calculatedHeight.toDouble() / it.second * it.first).toInt() } ?: 0
				
				minRatio?.let { minimumWidth = (calculatedHeight.toDouble() / it.second * it.first).toInt() }
				
				if(ratioWidth != 0)
				{
					calculatedWidth = if(maxRatioWidth != 0)
						min(desiredWidth, maxRatioWidth)
					else ratioWidth
				}
				else if(maxRatioWidth != 0 && calculatedWidth > maxRatioWidth)
					calculatedWidth = maxRatioWidth
				
				setMeasuredDimension(calculatedWidth, calculatedHeight)
			}
		}
	}
}