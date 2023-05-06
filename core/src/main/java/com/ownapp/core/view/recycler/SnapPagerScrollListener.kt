package com.ownapp.core.view.recycler

import androidx.annotation.IntDef
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ownapp.core.extensions.utility.debug
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.view.getSnapPosition


/**
 * Created by Robin on 4/1/2021.
 */
class SnapPagerScrollListener(
	private val snapHelper: PagerSnapHelper
	, @Type private val type: Int
	, private val notifyOnInit: Boolean
	, private val listener: OnChangeListener
): RecyclerView.OnScrollListener()
{
	@IntDef(ON_SCROLL, ON_SETTLED)
	annotation class Type
	interface OnChangeListener
	{
		fun onSnapped(position: Int)
	}

	private var snapPosition: Int

	// Methods
	override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int)
	{
		debug("SnapPagerScrollListener onScrolled"
			,"type" to type
			, "hasItemPosition()" to hasItemPosition()
			, "recyclerView" to recyclerView.layoutManager
		
		)
		
		super.onScrolled(recyclerView, dx, dy)
		if(type == ON_SCROLL || !hasItemPosition())
		{
			notifyListenerIfNeeded(snapHelper.getSnapPosition(recyclerView))
		}
	}

	override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int)
	{
		debug("SnapPagerScrollListener onScrollStateChanged"
			,"type" to type
			, "newState" to newState
			, "recyclerView" to recyclerView.layoutManager
		)
		
		super.onScrollStateChanged(recyclerView, newState)
		if(type == ON_SETTLED && newState == RecyclerView.SCROLL_STATE_IDLE)
		{
			notifyListenerIfNeeded(snapHelper.getSnapPosition(recyclerView))
		}
	}

	fun notifyListenerIfNeeded(newSnapPosition: Int)
	{
		debug("SnapPagerScrollListener notifyListenerIfNeeded"
			, "snapPosition" to snapPosition
			, "newSnapPosition" to newSnapPosition
			, "notifyOnInit" to notifyOnInit
			, "hasItemPosition" to hasItemPosition()
		)
		
		if(snapPosition != newSnapPosition)
		{
			if(notifyOnInit || hasItemPosition())
				listener.onSnapped(newSnapPosition)

			snapPosition = newSnapPosition
		}
	}

	private fun hasItemPosition(): Boolean
	{
		return snapPosition != RecyclerView.NO_POSITION
	}

	companion object
	{
		// Constants
		const val ON_SCROLL = 0
		const val ON_SETTLED = 1
	}

	// Constructor
	init
	{
		snapPosition = RecyclerView.NO_POSITION
	}
}