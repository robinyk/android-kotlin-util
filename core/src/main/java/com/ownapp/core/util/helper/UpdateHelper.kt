package com.ownapp.core.util.helper

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.install.InstallState
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.ownapp.core.R
import com.ownapp.core.extensions.resource.toColor
import com.ownapp.core.extensions.rootView
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.logException
import com.ownapp.core.extensions.utility.parse
import kotlin.system.exitProcess

/**
 * Updated by Robin on 2020/12/4
 */

/**
 * Helper for auto update in Google PlayStore. Still in development, use at own risk
 *
 * Require: implementation 'com.google.android.play:core-ktx:1.8.1'
 *
 * @see AppUpdateManager
 */
object UpdateHelper
{
	//**--------------------------------------------------------------------------------------------------
	//*      Variable
	//---------------------------------------------------------------------------------------------------*/
	private const val UPDATE_REQUEST_CODE = 2327
	private val tag by lazy { this::class.java.simpleName }
	private var installStateUpdatedListener: InstallStateUpdatedListener? = null

	private val updateAvailabilityMap = mapOf(
		UpdateAvailability.UNKNOWN to "Unknown"
		, UpdateAvailability.UPDATE_NOT_AVAILABLE to "Not available"
		, UpdateAvailability.UPDATE_AVAILABLE to "Available"
		, UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS to "Triggered update in progress"
	)

	fun Activity.checkForUpdate(appUpdateManager: AppUpdateManager)
	{
		appUpdateManager.appUpdateInfo
			.addOnSuccessListener { appUpdateInfo ->
				 log(tag,
			 "Checking for update"
					 , "Availability => ${updateAvailabilityMap.parse(appUpdateInfo.updateAvailability())}"
				 )

				when(appUpdateInfo.updateAvailability())
				{
					UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS ->
					{
						appUpdateManager.startUpdateFlowForResult(
							appUpdateInfo, AppUpdateType.IMMEDIATE, this, UPDATE_REQUEST_CODE
						)
					}

					UpdateAvailability.UPDATE_AVAILABLE ->
					{
						log(tag,
					"IMMEDIATE -> ${appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)}"
							, "FLEXIBLE -> ${appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)}"
						)

						if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE))
							appUpdateManager.startUpdateFlowForResult(
								appUpdateInfo, AppUpdateType.IMMEDIATE, this, UPDATE_REQUEST_CODE
							)
						else if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE))
						{
							installStateUpdatedListener = InstallStateUpdatedListener { state ->
								onStateUpdate(this, state, appUpdateManager)
							}

							installStateUpdatedListener?.let { appUpdateManager.registerListener(it) }

							appUpdateManager.startUpdateFlowForResult(
								appUpdateInfo, AppUpdateType.FLEXIBLE, this, UPDATE_REQUEST_CODE
							)
						}
					}

					UpdateAvailability.UPDATE_NOT_AVAILABLE -> { }
					UpdateAvailability.UNKNOWN -> { }
				}

				if(appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED)
					snackOnCompleted(appUpdateManager)
			}
			.addOnCompleteListener {
				try
				{
					if(it.isComplete)
						(tag to "Finished update checking").log()
				}
				catch(e: Exception)
				{
					"$tag Error on update => ${e.message}".logError()
				}
			}
			.addOnFailureListener {
				(tag to it.message).logError()
			}
	}

	fun Activity.snackOnCompleted(appUpdateManager: AppUpdateManager)
	{
		rootView?.snackOnCompleted(appUpdateManager) ?: "Failed to find Activity.rootView".log()
	}

	fun View?.snackOnCompleted(appUpdateManager: AppUpdateManager)
	{
		(tag to "Download Completed").log()

		this?.let {
			Snackbar.make(it, R.string.msg_complete_update, Snackbar.LENGTH_INDEFINITE).apply {
				setAction(R.string.text_restart) {
					installStateUpdatedListener?.let { listener -> appUpdateManager.unregisterListener(listener) }
					appUpdateManager.completeUpdate()
				}
				setActionTextColor(R.attr.colorPrimary.toColor(context))
				show()
			}
		}
	}


	//**--------------------------------------------------------------------------------------------------
	//*      Override
	//---------------------------------------------------------------------------------------------------*/
	fun onStateUpdate(activity: Activity, state: InstallState, appUpdateManager: AppUpdateManager)
	{
		when(state.installStatus())
		{
			InstallStatus.DOWNLOADING ->
			{
				tag.log(value = "DOWNLOADING")
				//				val bytesDownloaded = state.bytesDownloaded()
				//				val totalBytesToDownload = state.totalBytesToDownload()
			}

			InstallStatus.DOWNLOADED -> activity.snackOnCompleted(appUpdateManager)

			InstallStatus.CANCELED, InstallStatus.FAILED, InstallStatus.INSTALLED ->
			{
				tag.log(value = "CANCELED/FAILED/INSTALLED")
				installStateUpdatedListener?.let { appUpdateManager.unregisterListener(it) }
			}
			else -> {}
		}
	}

	fun onResume(activity: Activity, appUpdateManager: AppUpdateManager)
	{
		appUpdateManager.appUpdateInfo
			.addOnSuccessListener { appUpdateInfo ->
				log(tag
					, "onResume -> Checking for update"
					, "Availability => ${appUpdateInfo.updateAvailability()}"
				)

				if(appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED)
					activity.snackOnCompleted(appUpdateManager)

				if(appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
					appUpdateManager.startUpdateFlowForResult(
						appUpdateInfo, AppUpdateType.IMMEDIATE, activity, UPDATE_REQUEST_CODE
					)
			}
	}

	fun onActivityResult(
		requestCode: Int
		, resultCode: Int
		, onSuccess: () -> Unit = {}
		, onCanceled: () -> Unit = onSuccess
		, onFailed: () -> Unit = onCanceled
	) {
		if(requestCode == UPDATE_REQUEST_CODE)
		{
			when(resultCode)
			{
				RESULT_OK -> onSuccess()
				RESULT_CANCELED -> onCanceled()
				ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> onFailed()
			}
		}
	}

	// Legacy
	object Legacy
	{
		//**--------------------------------------------------------------------------------------------------
		//*      Variable
		//---------------------------------------------------------------------------------------------------*/
		// Constant
		private const val STORE_APP_LINK = "market://details?id="
		private const val STORE_WEBSITE_LINK = "http://play.google.com/store/apps/details?id="

		// Value
		var isForceUpdate = false


		//**--------------------------------------------------------------------------------------------------
		//*      Private
		//---------------------------------------------------------------------------------------------------*/
		private fun versionCompare(latestVersion: String, currentVersion: String): Int
		{
			val latest = latestVersion.split("\\.").toTypedArray()
			val current = currentVersion.split("\\.").toTypedArray()
			var i = 0

			// set index to first non-equal ordinal or length of shortest version string
			while(i < latest.size && i < current.size && latest[i] == current[i])
			{
				i++
			}

			// compare first non-equal ordinal number
			if(i < latest.size && i < current.size)
			{
				val diff = Integer.valueOf(latest[i]).compareTo(Integer.valueOf(current[i]))
				return Integer.signum(diff)
			}

			// the strings are equal or one string is a substring of the other
			// e.g. "1.2.3" = "1.2.3" or "1.2.3" < "1.2.3.4"
			return Integer.signum(latest.size - current.size)
		}

		private fun update(activity: Activity)
		{
			activity.packageName.replace(".debug", "").let { packageName ->
				try
				{
					activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(STORE_APP_LINK + packageName)))
				}
				catch(e: ActivityNotFoundException)
				{
					activity.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(STORE_WEBSITE_LINK + packageName)))
					e.logException()
				}
			}

			exitProcess(0)
		}


		//**--------------------------------------------------------------------------------------------------
		//*      Public
		//---------------------------------------------------------------------------------------------------*/
		fun check(activity: Activity, currentVersion: String, latestVersion: String, onLatestVersion: () -> Unit = {})
		{
			"Version (current -> latest)".log(value = "$currentVersion > $latestVersion")

			if(versionCompare(latestVersion, currentVersion) > 0)
			{
				MaterialAlertDialogBuilder(activity)
						.setTitle(R.string.msg_version_new_available)
						.setMessage(R.string.msg_version_update)
						.setPositiveButton(R.string.text_update) { _: DialogInterface, _: Int ->
							update(activity)
						}
						.setNegativeButton(if(isForceUpdate) R.string.text_quit else R.string.text_cancel) { _: DialogInterface, _: Int ->
							if(isForceUpdate)
								exitProcess(1)
							else
								onLatestVersion()
						}.show()
			}
			else
			{
				onLatestVersion()
			}
		}
	}
}