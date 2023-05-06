package com.ownapp.core.view.fragment.recycler

import android.Manifest
import android.annotation.SuppressLint
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ownapp.core.R
import com.ownapp.core.extensions.isNetworkConnected
import com.ownapp.core.extensions.utility.info
import com.ownapp.core.extensions.utility.toast
import com.ownapp.core.support.network.response.OwnappPagedLegacyResponse
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class PagedListViewModel<T>: ViewModel()
{
	var fragment: Fragment? = null
	var list = MutableLiveData<List<T>?>()

	var isLoading: Boolean = false
	val isResetLoadingState = MutableLiveData(false)
	open val isLocal = false
	
	open fun initialize() = fetchPagedList(page = 1)

	open fun reset()
	{
		isResetLoadingState.value = true
		initialize()
		isResetLoadingState.value = false
	}

	open fun fetch(page: Int): Flow<OwnappPagedLegacyResponse<T>?> = flow { emit(null) }

	open fun fetchPagedList(page: Int)
	{
		if(!isLocal && fragment?.isNetworkConnected() == false)
		{
			fragment?.toast(R.string.error_no_internet)
			list.value = list.value
			return
		}
		
		if(!isLoading)
		{
			viewModelScope.launch {
				fetch(page)
					.onStart { isLoading = true }
					.onCompletion { isLoading = false }
					.retry()
					.collect { response ->
						when(response?.data?.status)
						{
							// Success
							1 -> onGetList(response.list)

							// Invalid Status
							else -> onGetList(null)
						}
					}
			}
		}
	}

	open fun onGetList(newList: List<T>?)
	{
		list.value = newList
	}
}