package com.ownapp.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import com.google.android.material.theme.overlay.MaterialThemeOverlay

class DisabledScrollView: NestedScrollView
{
	object BindingAdapters
	{
		@BindingAdapter("isScrollable")
		@JvmStatic fun DisabledScrollView.setScrollable(isEnabled: Boolean)
		{
			isScrollable = isEnabled
		}
	}
	
	var isScrollable = false

	constructor(context: Context): this(context, null)

	constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

	constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
			: super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, 0), attrs, defStyleAttr)

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(ev: MotionEvent): Boolean
	{
		return if(ev.action == MotionEvent.ACTION_DOWN)
			isScrollable && super.onTouchEvent(ev)
		else super.onTouchEvent(ev)
	}

	override fun onInterceptTouchEvent(ev: MotionEvent): Boolean
	{
		return isScrollable && super.onInterceptTouchEvent(ev)
	}
}