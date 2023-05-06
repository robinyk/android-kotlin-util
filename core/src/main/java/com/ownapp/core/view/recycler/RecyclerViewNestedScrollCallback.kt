package com.ownapp.core.view.recycler

import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.ownapp.core.extensions.utility.debug
import com.ownapp.core.extensions.utility.log

class RecyclerViewNestedScrollCallback(private val layoutManager: RecyclerView.LayoutManager)
	: NestedScrollView.OnScrollChangeListener
{
	// The minimum amount of items to have below your current scroll position
	// before loading more.
	// The current offset index of data you have loaded
	private var currentPage = 1
	// The total number of items in the dataset after the last load
	private var previousTotalItemCount = 0
	// True if we are still waiting for the last set of data to load.
	private var loading = true
	// Sets the starting page index
	private val startingPageIndex = 1

	private lateinit var layoutManagerType: LayoutManagerType
	private lateinit var onScrolledListener: OnScrolledListener

	constructor(builder: Builder) : this(builder.layoutManager) {
		this.layoutManagerType = builder.layoutManagerType
		this.onScrolledListener = builder.onScrolledListener
		if (builder.resetLoadingState) {
			resetState()
		}
	}

	// This happens many times a second during a scroll, so be wary of the code you place here.
	// We are given a few useful parameters to help us work out if we need to load some more data,
	// but first we check if we are waiting for the previous load to finish.
	override fun onScrollChange(v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)
	{
		val totalItemCount = layoutManager.itemCount

		// If the total item count is zero and the previous isn't, assume the
		// list is invalidated and should be reset back to initial state
		if (totalItemCount < previousTotalItemCount) {
			this.currentPage = this.startingPageIndex
			this.previousTotalItemCount = totalItemCount
			if (totalItemCount == 0) {
				this.loading = true
			}
		}

		// If it’s still loading, we check to see if the dataset count has
		// changed, if so we conclude it has finished loading and update the current page
		// number and total item count.
		if (loading && totalItemCount > previousTotalItemCount) {
			loading = false
			previousTotalItemCount = totalItemCount
		}

		if (!loading
			&& v.getChildAt(v.childCount - 1) != null
			&& scrollY > oldScrollY
			&& scrollY >= (v.getChildAt(v.childCount - 1).measuredHeight - v.measuredHeight)
		) {
			// If it isn’t currently loading, we check to see if we have breached
			// the visibleThreshold and need to reload more data.
			// If we do need to reload some more data, we execute onLoadMore to fetch the data.
			// threshold should reflect how many total columns there are too

			currentPage++
			onScrolledListener.onScrolledToBottom(currentPage)
			loading = true

			debug("NestedScrollView onScrolledToBottom, load new page" to currentPage)
		}
	}

	// Call this method whenever performing new searches
	private fun resetState() {
		this.currentPage = this.startingPageIndex
		this.previousTotalItemCount = 0
		this.loading = true
	}

	class Builder(internal val layoutManager: RecyclerView.LayoutManager) {
		internal var layoutManagerType = LayoutManagerType.LINEAR
		internal lateinit var onScrolledListener: OnScrolledListener
		internal var resetLoadingState: Boolean = false

		fun onScrolledListener(value: OnScrolledListener): Builder {
			onScrolledListener = value
			return this
		}

		fun resetLoadingState(value: Boolean): Builder {
			resetLoadingState = value
			return this
		}

		fun build(): RecyclerViewNestedScrollCallback {
			layoutManagerType = RecyclerViewUtil.computeLayoutManagerType(layoutManager)
			return RecyclerViewNestedScrollCallback(this)
		}
	}

}
