package com.ownapp.core.util.helper

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.lifecycle.LiveData
import com.ownapp.core.extensions.connectivityManager
import com.ownapp.core.extensions.isNetworkConnected

/**
 * Updated by Robin on 2020/12/4
 */

class InternetDetector(private val context: Context): LiveData<Boolean>() {
	private var intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
	private var connectivityManager = context.connectivityManager as ConnectivityManager
	private var networkCallback: NetworkCallback

	init {
		networkCallback = NetworkCallback(this)
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE])
	override fun onActive() {
		super.onActive()
		updateConnection()
		when {
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> connectivityManager.registerDefaultNetworkCallback(networkCallback)
			else -> {
				val builder = NetworkRequest.Builder()
					.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
					.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
					.addTransportType(NetworkCapabilities.TRANSPORT_VPN)
				connectivityManager.registerNetworkCallback(builder.build(), networkCallback)
			}
		}
	}

	override fun onInactive() {
		super.onInactive()
		connectivityManager.unregisterNetworkCallback(networkCallback)
	}


	private val networkReceiver = object : BroadcastReceiver() {
		@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE])
		override fun onReceive(context: Context, intent: Intent) {
			updateConnection()
		}
	}

	@RequiresPermission(allOf = [Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE])
	private fun updateConnection() {
		postValue(context.isNetworkConnected())
	}

	@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
	class NetworkCallback(private val liveData: InternetDetector) : ConnectivityManager.NetworkCallback() {
		override fun onAvailable(network: Network) {
			liveData.postValue(true)
		}

		override fun onLost(network: Network) {
			liveData.postValue(false)
		}
	}
}