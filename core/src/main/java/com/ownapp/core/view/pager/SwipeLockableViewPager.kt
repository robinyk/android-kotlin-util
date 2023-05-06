package com.ownapp.core.view.pager

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * Updated by Robin on 2020/12/4
 */

class SwipeLockableViewPager(context: Context, attrs: AttributeSet): ViewPager(context, attrs)
{
    var isSwipeEnabled = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (isSwipeEnabled) {
            true -> super.onTouchEvent(event)
            false -> false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return when (isSwipeEnabled) {
            true -> super.onInterceptTouchEvent(event)
            false -> false
        }
    }
}