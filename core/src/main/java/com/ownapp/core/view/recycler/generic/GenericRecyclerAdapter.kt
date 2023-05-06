package com.ownapp.core.view.recycler.generic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.ownapp.core.R
import com.ownapp.core.extensions.utility.debug
import com.ownapp.core.extensions.utility.prettyJson
import com.ownapp.core.util.SnackbarUtil

open class GenericRecyclerAdapter<T: GenericRecyclerItem>: ListAdapter<T, GenericViewHolder>
{
	private lateinit var viewBinder: (LayoutInflater, ViewGroup, Boolean) -> ViewDataBinding
	private lateinit var viewBinding: (parent: ViewGroup, viewType: Int) -> ViewDataBinding

	constructor(
		viewBinder: (LayoutInflater, ViewGroup, Boolean) -> ViewDataBinding
	): super(AsyncDifferConfig.Builder<T>(GenericDiffUtil()).build())
	{
		this.viewBinder = viewBinder
	}
	
	constructor(
		viewBinding: (parent: ViewGroup, viewType: Int) -> ViewDataBinding
	): super(AsyncDifferConfig.Builder<T>(GenericDiffUtil()).build())
	{
		this.viewBinding = viewBinding
	}
	
	interface ItemLifecycleListener<T: GenericRecyclerItem>
	{
		fun onItemCreated(holder: GenericViewHolder)
		fun onItemAttached(holder: GenericViewHolder)
		fun onItemDetached(holder: GenericViewHolder)
	}

	var onClickListener: GenericRecyclerItem.OnClickListener? = null
	var onBindListener: GenericRecyclerItem.OnBindListener? = null
	var itemLifecycleListener: ItemLifecycleListener<T>? = null

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder
	{
		(if(this::viewBinding.isInitialized)
			viewBinding(parent, viewType)
		else viewBinder(LayoutInflater.from(parent.context), parent, false)
		).let {
			return GenericViewHolder(it).apply {
				it.lifecycleOwner = this
				markCreated()
				itemLifecycleListener?.onItemCreated(this)
			}
		}
	}

	override fun onBindViewHolder(holder: GenericViewHolder, position: Int)
	{
		getItemAt(position)?.let { item ->
			item.holder = holder
			item.adapterPosition = position
			item.isFirst = position == 0
			item.isLast = position == currentList.size - 1

			if(item.onClickListener == null && onClickListener != null)
				item.onClickListener = onClickListener

			onBindListener?.onItemBind(item, position)
			holder.bind(item)
		} ?: error("Cannot find item in ${this::class.simpleName} at position $position")
	}

	override fun onViewAttachedToWindow(holder: GenericViewHolder)
	{
		super.onViewAttachedToWindow(holder)
		holder.markAttach()
		itemLifecycleListener?.onItemAttached(holder)
	}

	override fun onViewDetachedFromWindow(holder: GenericViewHolder)
	{
		super.onViewDetachedFromWindow(holder)
		holder.markDetach()
		itemLifecycleListener?.onItemDetached(holder)
	}
	
	override fun getItemId(position: Int): Long = currentList.getOrNull(position)?.identifier.toString().toLongOrNull() ?: position.toLong()
	
	fun getItemAt(position: Int): T? = currentList.getOrNull(position)
	
	fun onItemClick(onItemClick: (item: T, view: View) -> Unit = { _, _ -> })
	{
		onClickListener = object: GenericRecyclerItem.OnClickListener
		{
			@Suppress("UNCHECKED_CAST")
			override fun onItemClick(item: GenericRecyclerItem, view: View)
			{
				onItemClick(item as T, view)
			}
		}
	}

	fun onItemBind(onItemBind: (item: T, position: Int) -> Unit = { _, _ -> })
	{
		onBindListener = object: GenericRecyclerItem.OnBindListener
		{
			@Suppress("UNCHECKED_CAST")
			override fun onItemBind(item: GenericRecyclerItem, position: Int)
			{
				onItemBind(item as T, position)
			}
		}
	}
	
	fun itemLifecycleListener(
		onItemCreated: (holder: GenericViewHolder) -> Unit = { _ -> }
		, onItemAttached: (holder: GenericViewHolder) -> Unit = { _ -> }
		, onItemDetached: (holder: GenericViewHolder) -> Unit = { _ -> }
	)
	{
		itemLifecycleListener = object: ItemLifecycleListener<T>
		{
			override fun onItemCreated(holder: GenericViewHolder) = onItemCreated(holder)
			override fun onItemAttached(holder: GenericViewHolder) = onItemAttached(holder)
			override fun onItemDetached(holder: GenericViewHolder) = onItemDetached(holder)
		}
	}
	
	fun setLifecycleDestroyed() = currentList.forEach { it.holder?.markDestroyed() }

	// @Deprecated("This having some issue. Use back original submitList instead", replaceWith = ReplaceWith("submitList"))
	// fun submit(
	// 	list: List<T>?
	// 	, predicate: (T, T) -> Boolean = { x, y -> x.identifier.toString() == y.identifier.toString() }
	// )
	// {
	// 	if(!currentList.deepEqualTo(list) { x, y -> predicate(x, y) })
	// 		replace(list)
	// 	else add(list)
	// }
	
	fun replace(list: List<T>?)
	{
		super.submitList(list?.toMutableList() ?: mutableListOf())
		notifyDataSetChanged()
	}

	fun add(list: List<T>?)
	{
		if(!list.isNullOrEmpty())
		{
			currentList.toMutableList().run {
				addAll(list)
				super.submitList(this)
			}

			notifyDataSetChanged()
		}
	}
	
	fun addItem(item: T, position: Int = -1)
	{
		currentList.toMutableList().run {
			if(position > 0)
				add(position, item)
			else add(item)

			super.submitList(this)
		}
	}
	
	fun removeItem(position: Int) = currentList.getOrNull(position)?.let { removeItem(it) }
	
	fun removeItem(item: T)
	{
		if(currentList.any { it == item })
		{
			currentList.toMutableList().let { list ->
				list.find { it == item }?.let {
					val position = list.indexOf(it)

					list.remove(it)
					list.filter { my -> my.adapterPosition > position }.forEach { my ->
						my.adapterPosition--
					}

					super.submitList(list)
				}
			}
		}
	}

//	fun removeItemWithUndo(position: Int, view: View)
//	{
//		val deleted = currentList.removeAt(position)
//		notifyItemRemoved(position)
//		notifyItemRangeChanged(position, itemCount)
//
//		SnackbarUtil.build(view, R.string.text_item_removed)
//			?.setAction(R.string.text_undo) {
//				currentList.add(position, deleted)
//				notifyItemInserted(position)
//			}?.show()
//	}

	fun clear() = submitList(null)

	fun notifyItemChanged(item: T) = notifyItemChanged(currentList.indexOf(item))
}