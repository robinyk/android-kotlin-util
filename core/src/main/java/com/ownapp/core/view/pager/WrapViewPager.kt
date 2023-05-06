package com.ownapp.core.view.pager

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

/**
 * Updated by Robin on 2020/12/4
 */

class WrapViewPager: ViewPager
{
	constructor(context: Context): super(context)
	constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
	{
		val measureSpec: Int

		var height = 0
		for (i in 0 until childCount)
		{
			val child = getChildAt(i)
			child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
			val h = child.measuredHeight
			if (h > height) height = h
		}

		measureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

		super.onMeasure(widthMeasureSpec, measureSpec)
	}
}