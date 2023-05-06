package com.ownapp.core.view.fragment.recycler

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ownapp.core.R
import com.ownapp.core.binding.viewBinding
import com.ownapp.core.databinding.ContentRefreshablePagedRecyclerBinding
import com.ownapp.core.extensions.utility.debug
import com.ownapp.core.extensions.utility.launchDelay
import com.ownapp.core.extensions.view.setSnapListener
import com.ownapp.core.extensions.view.smoothSnapToPosition
import com.ownapp.core.extensions.view.stop
import com.ownapp.core.view.fragment.FragmentOnBackPressListener
import com.ownapp.core.view.recycler.generic.GenericRecyclerItem
import com.ownapp.core.view.recycler.generic.GenericRecyclerAdapter
import kotlinx.coroutines.launch


abstract class RefreshablePagedRecyclerFragment<T: GenericRecyclerItem>
	: Fragment(R.layout.content_refreshable_paged_recycler)
	, FragmentOnBackPressListener
{
	//**--------------------------------------------------------------------------------------------------
	//*      Variable
	//---------------------------------------------------------------------------------------------------*/
	protected val binder by viewBinding(ContentRefreshablePagedRecyclerBinding::bind)
	abstract val viewModel: PagedListViewModel<T>

	// Class
	abstract var adapter: GenericRecyclerAdapter<T>
	open val layoutManager: RecyclerView.LayoutManager by lazy {
		LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
	}

	// View
	val swipeRefreshContainer
		get() = binder.swipeRefreshContainer

	val recyclerView
		get() = binder.contentPagedRecycler.recyclerView

	val shimmerContainer
		get() = binder.contentPagedRecycler.shimmerContainer

	// Value
	open val refreshDelayInMillis: Long = 1000L
	open val snapToTopOnBack = false
	open val refreshOnBack = false
	var isRefreshed = true


	//**--------------------------------------------------------------------------------------------------
	//*      Initialize
	//---------------------------------------------------------------------------------------------------*/
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		viewModel.fragment = this
		binder.lifecycleOwner = viewLifecycleOwner
		binder.viewModel = viewModel

		recyclerView.layoutManager = layoutManager
		recyclerView.adapter = adapter
		recyclerView.pagerSnapHelper = recyclerView.setSnapListener()

		viewModel.list.observe(viewLifecycleOwner) {
			viewLifecycleOwner.lifecycleScope.launch {
				shimmerContainer.stop()
				
				onListUpdate(it)
				
				recyclerView.isShowEmpty = true
				viewModel.isLoading = false
			}
		}

		viewModel.initialize()
	}

	open fun onListUpdate(list: List<T>?)
	{
		"${this::class.simpleName} onListUpdate".debug()
		
		if(swipeRefreshContainer.isRefreshing)
		{
			viewLifecycleOwner.lifecycleScope.launchDelay(refreshDelayInMillis) {
				swipeRefreshContainer.isRefreshing = false
			}

			adapter.submitList(list)
		}
		else if(!list.isNullOrEmpty())
			adapter.add(list)
	}


	//**--------------------------------------------------------------------------------------------------
	//*      Implement
	//---------------------------------------------------------------------------------------------------*/
	override fun onBackPressed(): Boolean
	{
		if(snapToTopOnBack && recyclerView.snapPosition > 5)
		{
			"${this::class.simpleName} Snap position ${recyclerView.snapPosition}".debug()
			"${this::class.simpleName} Smooth snap to top".debug()
			isRefreshed = false
			recyclerView.smoothSnapToPosition(0)
			return true
		}
		else if(refreshOnBack && !isRefreshed)
		{
			"${this::class.simpleName} Swipe refresh on back pressed".debug()

			isRefreshed = true
			swipeRefreshContainer.isRefreshing = true
			viewModel.reset()
			return true
		}

		return false
	}
}