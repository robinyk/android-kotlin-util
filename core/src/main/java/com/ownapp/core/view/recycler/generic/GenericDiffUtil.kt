package com.ownapp.core.view.recycler.generic

import androidx.recyclerview.widget.DiffUtil

class GenericDiffUtil<T: GenericRecyclerItem>: DiffUtil.ItemCallback<T>()
{
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean
    {
        return if(oldItem.identifier != null && newItem.identifier != null)
            oldItem.identifier.toString() == newItem.identifier.toString()
        else
            oldItem == newItem

    }

    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean
    {
        return if(oldItem.identifier != null && newItem.identifier != null)
            oldItem.identifier.toString() == newItem.identifier.toString()
        else
            oldItem.toString() == newItem.toString()
    }
}