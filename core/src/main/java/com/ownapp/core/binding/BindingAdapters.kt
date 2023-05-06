package com.ownapp.core.binding

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ownapp.core.extensions.resource.getEntryName
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.view.multiLineTreatment
import com.ownapp.core.view.QuantityView
import com.ownapp.core.view.recycler.OnScrolledListener
import com.ownapp.core.view.recycler.RecyclerViewNestedScrollCallback
import com.ownapp.core.view.recycler.RecyclerViewScrollCallback
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Updated by Robin on 2020/12/4
 */

/**
 * @param swipeRefreshLayout Bind swipeRefreshLayout with OnRefreshListener
 * @param onPulledToRefresh Listener for onRefresh when swiped
 */
@BindingAdapter("onPulledToRefresh")
fun setOnSwipeRefreshListener(swipeRefreshLayout: SwipeRefreshLayout, onPulledToRefresh: Runnable)
{
	swipeRefreshLayout.setOnRefreshListener { onPulledToRefresh.run() }
}

/**
 * Enable TextView multiline and disable user to enter new blank Line
 * @param view [TextView]
 * @param isEnabled [Boolean]
 */
@BindingAdapter("multiLineTreatment")
fun multiLineTreatment(view: View, isEnabled: Boolean?)
{
	if(isEnabled == true)
		(view as? TextView)?.multiLineTreatment()
}

@BindingAdapter("selected", "onBindingValueChangedListener", requireAll = false)
fun View.setSelected(isSelected: Boolean?, onBindingValueChangedListener: InverseBindingListener?)
{
	if(this.isSelected != isSelected)
	{
		this.isSelected = isSelected == true
		onBindingValueChangedListener?.onChange()
	}
}

@InverseBindingAdapter(attribute = "selected", event = "onBindingValueChangedListener")
fun View.getSelected(): Boolean = isSelected


@BindingAdapter("android:enabled")
fun setEnabled(view: View, isEnabled: Boolean?)
{
	view.isEnabled = isEnabled == true
	view.isFocusable = isEnabled == true
}


