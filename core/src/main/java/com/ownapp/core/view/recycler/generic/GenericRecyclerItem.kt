package com.ownapp.core.view.recycler.generic

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.room.Ignore
import kotlinx.parcelize.IgnoredOnParcel

abstract class GenericRecyclerItem
{
    interface OnClickListener
    {
        fun onItemClick(item: GenericRecyclerItem, view: View)
    }

    interface OnBindListener
    {
        fun onItemBind(item: GenericRecyclerItem, position: Int)
    }

    @get:Ignore @IgnoredOnParcel
    open val identifier: Any?
        get() = null

    @Transient @Ignore @IgnoredOnParcel
    var holder: GenericViewHolder? = null

    @get:Ignore @IgnoredOnParcel
    open val binding: ViewDataBinding?
        get() = holder?.binding
    
    @Transient @Ignore @IgnoredOnParcel
    open var onClickListener: OnClickListener? = null

    @Transient @Ignore @IgnoredOnParcel
    open var adapterPosition: Int = -1

    @Transient @Ignore @IgnoredOnParcel
    open var isFirst: Boolean = false
    @Transient @Ignore @IgnoredOnParcel
    open var isLast: Boolean = false
    
    open fun initialize() {}
}