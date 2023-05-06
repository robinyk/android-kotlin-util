package com.ownapp.core.view.recycler

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

object RecyclerViewUtil
{
    fun computeLayoutManagerType(layoutManager: RecyclerView.LayoutManager?): LayoutManagerType
    {
        return when (layoutManager)
        {
            is StaggeredGridLayoutManager -> LayoutManagerType.STAGGERED_GRID
            is GridLayoutManager -> LayoutManagerType.GRID
            is LinearLayoutManager -> LayoutManagerType.LINEAR
            else -> LayoutManagerType.DEFAULT
        }
    }

    fun computeVisibleThreshold(
        layoutManager: RecyclerView.LayoutManager?
        , layoutManagerType: LayoutManagerType
        , visibleThreshold: Int
    ): Int = when (layoutManagerType)
    {
        LayoutManagerType.STAGGERED_GRID -> (layoutManager as StaggeredGridLayoutManager).spanCount * visibleThreshold
        LayoutManagerType.GRID -> (layoutManager as GridLayoutManager).spanCount * visibleThreshold
        LayoutManagerType.LINEAR, LayoutManagerType.DEFAULT -> visibleThreshold
    }

    fun getLastVisibleItemPosition(
        layoutManager: RecyclerView.LayoutManager?
        , layoutManagerType: LayoutManagerType
    ): Int = when (layoutManagerType)
    {
        LayoutManagerType.STAGGERED_GRID ->
        {
            val lastVisibleItemPositions = (layoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
            getStaggeredLayoutLastVisibleItem(lastVisibleItemPositions)
        }
        LayoutManagerType.LINEAR, LayoutManagerType.GRID -> (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        LayoutManagerType.DEFAULT -> 0
    }

    private fun getStaggeredLayoutLastVisibleItem(lastVisibleItemPositions: IntArray): Int
    {
        var maxSize = 0

        for (i in lastVisibleItemPositions.indices)
        {
            if (i == 0)
            {
                maxSize = lastVisibleItemPositions[i]
            }
            else if (lastVisibleItemPositions[i] > maxSize)
            {
                maxSize = lastVisibleItemPositions[i]
            }
        }
    
        return maxSize
    }
}