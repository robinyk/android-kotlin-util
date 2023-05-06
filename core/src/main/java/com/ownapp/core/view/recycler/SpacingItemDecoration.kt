package com.ownapp.core.view.recycler

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.ownapp.core.extensions.utility.logError


class SpacingItemDecoration(private val spacing: Int): ItemDecoration()
{
    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State)
    {
        val layoutManager = parent.layoutManager
        val position = parent.getChildAdapterPosition(view)
        
        with(outRect) {
            when (layoutManager)
            {
                is GridLayoutManager ->
                {
                    val spanCount = layoutManager.spanCount // item position
                    val column: Int = position % spanCount // item column
    
                    outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
        
                    if(position < spanCount) // top edge
                        outRect.top = spacing
                    
                    outRect.bottom = spacing // item bottom
                }
    
                is StaggeredGridLayoutManager ->
                {
                    val spanCount = layoutManager.spanCount // item position
                    val column: Int = position % spanCount // item column
    
                    outRect.left = spacing - column * spacing / spanCount // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount // (column + 1) * ((1f / spanCount) * spacing)
    
                    if(position < spanCount) // top edge
                        outRect.top = spacing
    
                    outRect.bottom = spacing // item bottom
                }
                
                is LinearLayoutManager -> when(layoutManager.orientation)
                {
                    RecyclerView.VERTICAL ->
                    {
                        if (position == 0)
                            top = spacing

                        // left =  spacing
                        // right = spacing
                        bottom = spacing
                    }

                    RecyclerView.HORIZONTAL ->
                    {
                        if (position == 0)
                            left = spacing

                        right = spacing
                        // top =  spacing
                        // bottom = spacing
                    }

                    else -> {}
                }
                
                else -> "Error resolving display mode".logError()
            }
        }
    }
}