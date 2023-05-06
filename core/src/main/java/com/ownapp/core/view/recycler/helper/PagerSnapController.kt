package com.ownapp.core.view.recycler.helper

import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.ownapp.core.view.recycler.SnapPagerScrollListener

class PagerSnapController
{
	//**--------------------------------------------------------------------------------------------------
	//*      Class
	//---------------------------------------------------------------------------------------------------*/
	interface OnSnapListener
	{
		fun onSnapped(previewPosition: Int, currentPosition: Int)
	}
	
	
	//**--------------------------------------------------------------------------------------------------
	//*      Class
	//---------------------------------------------------------------------------------------------------*/
	var onPageSnapListener = object: SnapPagerScrollListener.OnChangeListener
	{
		override fun onSnapped(position: Int)
		{
			if(currentPosition != position || previousPosition == -1)
			{
				previousPosition = currentPosition
				currentPosition = position
				onSnapListener?.onSnapped(previousPosition, currentPosition)
			}
		}
	}
	
	var snapTarget: RecyclerView? = null
	var snapHelper: PagerSnapHelper = PagerSnapHelper()
	
	var onSnapListener: OnSnapListener? = null
		set(listener)
		{
			field = listener
			setup(snapTarget, snapType, notifySnapOnInit)
		}
	
	// Value
	var snapType = SnapPagerScrollListener.ON_SETTLED
	var notifySnapOnInit = false
	var isPageSnap = false
	
	var currentPosition = 0
	var previousPosition = 0
	
	
	//**--------------------------------------------------------------------------------------------------
	//*      Public
	//---------------------------------------------------------------------------------------------------*/
	fun setup(
		recyclerView: RecyclerView? = snapTarget
		, @SnapPagerScrollListener.Type snapType: Int = this.snapType
		, notifyOnInit: Boolean = notifySnapOnInit
	)
	{
		snapTarget = recyclerView
		this.snapType = snapType
		notifySnapOnInit = notifyOnInit
		
		previousPosition = if(notifyOnInit) -1 else 0
		
		snapTarget?.let { target ->
			clear()
			snapHelper = PagerSnapHelper()
			
			target.addOnScrollListener(SnapPagerScrollListener(
				snapHelper
				, snapType
				, notifyOnInit
				, onPageSnapListener
			))
			
			if(isPageSnap)
				snapHelper.attachToRecyclerView(target)
		}
	}
	
	fun clear()
	{
		snapTarget?.clearOnScrollListeners()
		
		if(isPageSnap)
			snapTarget?.onFlingListener = null
		
		currentPosition = 0
		previousPosition = -1
	}
	
	fun onSnapped(block: (previewPosition: Int, currentPosition: Int) -> Unit = { _, _ ->})
	{
		onSnapListener = object: OnSnapListener
		{
			override fun onSnapped(previewPosition: Int, currentPosition: Int)
			{
				block(previewPosition, currentPosition)
			}
		}
	}
}