package com.ownapp.core.extensions.view

import android.view.ViewGroup
import androidx.annotation.Px
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.*
import com.ownapp.core.extensions.resource.getEntryName
import com.ownapp.core.extensions.utility.cast
import com.ownapp.core.extensions.utility.info
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.view.recycler.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Created by Robin on 6/12/2020.
 */

val <VH> RecyclerView.Adapter<VH>.isEmpty: Boolean where VH: RecyclerView.ViewHolder
	get() = itemCount == 0


val <VH> RecyclerView.Adapter<VH>.isNotEmpty: Boolean where VH: RecyclerView.ViewHolder
	get() = !isEmpty

fun SnapHelper.getSnapPosition(recyclerView: RecyclerView): Int
{
	val layoutManager = recyclerView.layoutManager ?: return RecyclerView.NO_POSITION
	val snapView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
	return layoutManager.getPosition(snapView)
}

fun RecyclerView.getSnapPosition(snapHelper: SnapHelper?): Int
{
	val layoutManager = layoutManager ?: return RecyclerView.NO_POSITION
	val snapView = snapHelper?.findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
	return layoutManager.getPosition(snapView)
}

fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START, performClick: Boolean = false)
{
	object: LinearSmoothScroller(context) {
		override fun getVerticalSnapPreference(): Int = snapMode
		override fun getHorizontalSnapPreference(): Int = snapMode

		override fun onStop()
		{
			super.onStop()

			if (performClick)
				findViewHolderForAdapterPosition(position)?.itemView?.performClick()
		}
	}.let {
		it.targetPosition = position
		layoutManager?.startSmoothScroll(it)
	}
}
/**
 * @param visibleThreshold  The minimum number of items to have below your current scroll position before loading more.
 * @param resetLoadingState  Reset endless scroll listener when performing a new search
 * @param onScrolledToBottom    OnScrolledListener for RecyclerView scrolled
 */
@BindingAdapter(value = ["resetLoadingState", "onScrolledToBottom", "visibleThreshold", "isNestedScroll"], requireAll = false)
fun RecyclerView.setScrollLoadCallback(
	resetLoadingState: Boolean
	, onScrolledToBottom: OnScrolledListener
	, visibleThreshold: Int?
	, isNestedScroll: Boolean = false
) {
	layoutManager?.let { layoutManager ->
		GlobalScope.launch {
			var isNestedFound = false

			if(isNestedScroll)
			{
				var view = parent

				while(view is ViewGroup && !isNestedFound)
				{
					(view as? NestedScrollView)?.let { nestedScrollView ->
						"{setScrollLoadCallback} Found NestedScrollView -> ${nestedScrollView.entryName}".log()

						nestedScrollView.setOnScrollChangeListener(
							RecyclerViewNestedScrollCallback.Builder(layoutManager)
								.resetLoadingState(resetLoadingState)
								.onScrolledListener(onScrolledToBottom)
								.build()
						)

						isNestedFound = true
					} ?: run { view = view.parent }
				}
			}

			if(!isNestedScroll || !isNestedFound)
			{
				val threshold = visibleThreshold ?: when(layoutManager)
				{
					is StaggeredGridLayoutManager -> 2
					is GridLayoutManager -> 2
					is LinearLayoutManager -> 5
					else -> 5
				}
				
				clearOnScrollListeners()
				addOnScrollListener(
					RecyclerViewScrollCallback.Builder(threshold, layoutManager)
						.resetLoadingState(resetLoadingState)
						.onScrolledListener(onScrolledToBottom)
						.build()
				)
			}
		}
	}
}

/**
 * @param orientation 0 for LinearLayout.HORIZONTAL and 1 for LinearLayout.VERTICAL
 */
@BindingAdapter("divider")
fun RecyclerView.addDividerItemDecoration(orientation: Int)
{
	addItemDecoration(DividerItemDecoration(context, orientation))
	invalidateItemDecorations()
}

/**
 * Set default margin to items
 * @param margin space in [Px]
 */
@BindingAdapter("spacing")
fun RecyclerView.addSpacingItemDecoration(@Px margin: Float)
{
	if (margin > 0f)
	{
		addItemDecoration(SpacingItemDecoration(margin.toInt()))
		invalidateItemDecorations()
	}
}

fun RecyclerView.setPagerSnapListener(
	onSnapped: (position: Int) -> Unit = {}
	, @SnapPagerScrollListener.Type type: Int = SnapPagerScrollListener.ON_SETTLED
	, notifyOnInit: Boolean = false
): PagerSnapHelper
{
	return setPagerSnapListener(object: SnapPagerScrollListener.OnChangeListener
	{
		override fun onSnapped(position: Int) = onSnapped(position)
	}, type, notifyOnInit)
}

fun RecyclerView.setPagerSnapListener(
	listener: SnapPagerScrollListener.OnChangeListener?
	, @SnapPagerScrollListener.Type type: Int = SnapPagerScrollListener.ON_SETTLED
	, notifyOnInit: Boolean = false
): PagerSnapHelper
{
	PagerSnapHelper().let {
		if(listener != null)
		{
			clearOnScrollListeners()
			onFlingListener = null
			addOnScrollListener(SnapPagerScrollListener(it, type, notifyOnInit, listener))
			it.attachToRecyclerView(this)
		}
		
		return it
	}
}

fun RecyclerView.setSnapListener(
	onSnapped: (position: Int) -> Unit = {}
	, @SnapPagerScrollListener.Type type: Int = SnapPagerScrollListener.ON_SCROLL
	, notifyOnInit: Boolean = false
): PagerSnapHelper
{
	return setSnapListener(object: SnapPagerScrollListener.OnChangeListener
	{
		override fun onSnapped(position: Int) = onSnapped(position)
	}, type, notifyOnInit)
}

fun RecyclerView.setSnapListener(
	listener: SnapPagerScrollListener.OnChangeListener
	, @SnapPagerScrollListener.Type type: Int = SnapPagerScrollListener.ON_SCROLL
	, notifyOnInit: Boolean = false
): PagerSnapHelper
{
	PagerSnapHelper().let {
		clearOnScrollListeners()
		addOnScrollListener(SnapPagerScrollListener(it, type, notifyOnInit, listener))
		return it
	}
}

fun RecyclerView.clearItemDecoration()
{
	for(i in 0 until itemDecorationCount)
	{
		removeItemDecoration(getItemDecorationAt(i))
	}
}