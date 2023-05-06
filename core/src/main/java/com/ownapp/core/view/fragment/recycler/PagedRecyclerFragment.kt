package com.ownapp.core.view.fragment.recycler

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ownapp.core.R
import com.ownapp.core.binding.viewBinding
import com.ownapp.core.databinding.ContentPagedRecyclerBinding
import com.ownapp.core.extensions.view.stop
import com.ownapp.core.view.recycler.generic.GenericRecyclerItem
import com.ownapp.core.view.recycler.generic.GenericRecyclerAdapter


abstract class PagedRecyclerFragment<T: GenericRecyclerItem>: Fragment(R.layout.content_paged_recycler)
{
	//**--------------------------------------------------------------------------------------------------
	//*      Variable
	//---------------------------------------------------------------------------------------------------*/
	protected val binder by viewBinding(ContentPagedRecyclerBinding::bind)
	abstract val viewModel: PagedListViewModel<T>

	// Class
	abstract var adapter: GenericRecyclerAdapter<T>
	open val layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)


	//**--------------------------------------------------------------------------------------------------
	//*      Initialize
	//---------------------------------------------------------------------------------------------------*/
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		binder.lifecycleOwner = viewLifecycleOwner
		binder.viewModel = viewModel

		binder.recyclerView.layoutManager = layoutManager
		binder.recyclerView.adapter = adapter

		viewModel.list.observe(viewLifecycleOwner) {
			binder.shimmerContainer.stop()

			if(adapter.currentList.isNullOrEmpty())
				adapter.submitList(it)
			else if(!it.isNullOrEmpty())
				adapter.add(it)

			binder.recyclerView.isShowEmpty = true
			viewModel.isLoading = false
		}

		viewModel.initialize()
	}
}