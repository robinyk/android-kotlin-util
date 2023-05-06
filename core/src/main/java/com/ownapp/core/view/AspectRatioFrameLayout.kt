package com.ownapp.core.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.view.children
import androidx.databinding.BindingAdapter
import com.ownapp.core.R
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.util.ViewUtil
import com.ownapp.core.view.AspectRatioFrameLayout.BindingAdapters.setMaxRatio
import com.ownapp.core.view.AspectRatioFrameLayout.BindingAdapters.setMinRatio
import com.ownapp.core.view.AspectRatioFrameLayout.BindingAdapters.setRatio
import kotlin.math.max
import kotlin.math.min

/**
 * Created by hristijan on 4/15/19 to long live and prosper !
 */
/**
 * Aspect Ratio Frame Layout, Here to Set the Width Height Based on Aspect Ratio
 */
class AspectRatioFrameLayout: FrameLayout
{
	object BindingAdapters
	{
		@BindingAdapter("ratio")
		@JvmStatic fun AspectRatioFrameLayout.setRatio(value: String?)
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
		@JvmStatic fun AspectRatioFrameLayout.setMinRatio(value: String?)
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
		@JvmStatic fun AspectRatioFrameLayout.setMaxRatio(value: String?)
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

	constructor(context: Context): super(context)
	{
		init(context, null)
	}

	constructor(context: Context, attrs: AttributeSet): super(context, attrs)
	{
		init(context, attrs)
	}
	
	constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)
	{
		init(context, attrs)
	}

	private fun init(context: Context, attrs: AttributeSet?)
	{
		measureAllChildren = true
		
		if(attrs != null)
		{
			context.theme.obtainStyledAttributes(attrs, R.styleable.AspectRatioFrameLayout, 0, 0).run {
				if(hasValue(R.styleable.AspectRatioFrameLayout_ratio))
					setRatio(getString(R.styleable.AspectRatioFrameLayout_ratio))
				
				if(hasValue(R.styleable.AspectRatioFrameLayout_minRatio))
					setMinRatio(getString(R.styleable.AspectRatioFrameLayout_minRatio))
				
				if(hasValue(R.styleable.AspectRatioFrameLayout_maxRatio))
					setMaxRatio(getString(R.styleable.AspectRatioFrameLayout_maxRatio))
				
				recycle()
			}
		}
		
		children.forEach { it.forceLayout() }
	}

	/**
	 * Method Which Runs on Measure time
	 *
	 * We OverRidden it Because of setting the width height based on the aspect ratio
	 */
	// override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	// {
	// 	super.onMeasure(widthMeasureSpec, heightMeasureSpec)
	//
	// 	val widthMode = MeasureSpec.getMode(widthMeasureSpec)
	// 	val heightMode = MeasureSpec.getMode(heightMeasureSpec)
	// 	val widthSize = MeasureSpec.getSize(widthMeasureSpec)
	// 	val heightSize = MeasureSpec.getSize(heightMeasureSpec)
	//	
	// 	if (widthMode == MeasureSpec.EXACTLY && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED))
	// 	{
	// 		setMeasuredDimension(widthSize, (widthSize.toDouble() / ratio.first * ratio.second).toInt())
	// 	}
	// 	else if (heightMode == MeasureSpec.EXACTLY && (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED))
	// 	{
	// 		setMeasuredDimension((heightSize.toDouble() / ratio.second * ratio.first).toInt(), heightSize)
	// 	}
	// 	else
	// 	{
	// 		super.setMeasuredDimension(widthMeasureSpec, heightMeasureSpec)
	// 	}
	// }
	
	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec)
		
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
			
			measureChildren(MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.getMode(widthMode)),
				MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.getMode(heightMode)))
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
			
			measureChildren(MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.getMode(widthMode)),
				MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.getMode(heightMode)))
		}
	}
}