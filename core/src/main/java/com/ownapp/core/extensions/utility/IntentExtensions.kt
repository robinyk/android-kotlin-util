package com.ownapp.core.extensions.utility

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ownapp.core.R
import com.ownapp.core.extensions.resource.toColor
import com.ownapp.core.model.App
import com.ownapp.core.model.Facebook
import com.ownapp.core.model.Instagram
import com.ownapp.core.model.Youtube
import com.ownapp.core.support.app.Shareable
import com.ownapp.core.util.helper.CustomTabsPackageHelper
import com.ownapp.core.util.permission.PermissionUtil

/**
 * Updated by Robin on 2020/12/16
 */

//private const val AppPackage.Facebook.name = "com.facebook.katana"
//private const val PACKAGE_Twiiter = "com.twitter.android"
//private const val PACKAGE_INSTAGRAM = "com.instagram.android"
//private const val PACKAGE_YOUTUBE = "com.google.android.youtube"

fun Context.openPlayStore(packageName: String = this.packageName)
{
	try
	{
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
	}
	catch (e: ActivityNotFoundException)
	{
		startActivity(Intent(Intent.ACTION_VIEW
			, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
		)
	}
}

fun Context.openLocation(latitude: Double?, longitude: Double?, locationName: String? = "")
{
	startActivity(Intent(
		Intent.ACTION_VIEW, Uri.parse("geo: $latitude, $longitude?q= $latitude, $longitude(${locationName})"))
	)
}

fun Context.openEmail(email: String?, subject: String? = "")
{
	Intent(Intent.ACTION_SEND).apply {
		type = "text/plain"
		putExtra(Intent.EXTRA_EMAIL, arrayOf(email.orEmpty()))
		putExtra(Intent.EXTRA_SUBJECT, subject.orEmpty())
		putExtra(Intent.EXTRA_TEXT, "")
	}.let {
		startActivity(Intent.createChooser(it, getString(R.string.text_send_email)))
	}

}

fun Activity.openContact(contact: String?)
{
	if(PermissionUtil.check(this, PermissionUtil.CALL_PHONE))
		startActivity(Intent(Intent.ACTION_CALL).setData(Uri.parse("tel: " + contact.orEmpty())))
}

fun Context.openPdfUrl(url: String?, fallback: Context.(String?) -> Unit = { _: String? ->
	startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
})
{
	if(!url.isNullOrBlank())
		openWebsite("http://docs.google.com/gview?embedded=true&url=${url}", fallback)
	else toast("Pdf url is null/blank => $url")
}

inline fun <reified T: Context> T.openWebsite(
	url: String?, fallback: T.(String?) -> Unit = { _: String? ->
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
	}
)
{
	CustomTabsPackageHelper.getPackageNameToUse(this).let { packageName ->
		if(packageName == null)
		{
			("Failed to launch website with CustomTabsPackageHelper" to packageName).logError()
			fallback(url)
		}
		else
		{
			CustomTabsIntent.Builder()
				.setShareState(CustomTabsIntent.SHARE_STATE_DEFAULT)
				.setDefaultColorSchemeParams(
					CustomTabColorSchemeParams.Builder()
						.setToolbarColor(android.R.attr.colorPrimary.toColor(this))
						.build()
				).setShowTitle(true)
				.build()
				.run {
					intent.setPackage(packageName)
					launchUrl(this@openWebsite, Uri.parse(url))
				}
		}
	}
}

fun Context.openFacebook(url: String?)
{
	try
	{
		if(packageManager?.getApplicationInfo(Facebook.packageName, 0)?.enabled == true)
			startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://facewebmodal/f?href=${Uri.parse(url.orEmpty())}")))
		else
			openApp(Facebook.packageName, url.orEmpty())
	}
	catch(ignored: PackageManager.NameNotFoundException)
	{
		openApp(Facebook.packageName, url.orEmpty())
	}
}

fun Context.openYoutube(url: String?) = openApp(Youtube.packageName, url.orEmpty())
fun Context.openInstagram(url: String?) = openApp(Instagram.packageName, url.orEmpty())

fun Fragment.shareTo(app: App, shareable: Shareable) = shareTo(app.packageName, shareable)
fun Fragment.shareTo(packageName: String, shareable: Shareable)
{
	try
	{
		if(requireActivity().isAppInstalled(packageName))
		{
			startActivity(Intent(Intent.ACTION_SEND).apply {
				type = "text/plain"

				setPackage(packageName)
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
				putExtras(bundleOf(
					Intent.EXTRA_TEXT to shareable.getShareMessage(requireContext())
					, Intent.EXTRA_TITLE to shareable.shareTitle
//						, Intent.EXTRA_STREAM to FileProvider.getUriForFile(activity, "${activity.packageName}.fileprovider", Glide.with(activity).asFile().load(post.previewUrl).submit().get())
				))
//				putExtra(Intent.EXTRA_REPLACEMENT_EXTRAS, Bundle().apply { putBundle(packageName, extras) })
			})
		}
		else requireActivity().openPlayStore(packageName)
	}
	catch(e: Exception)
	{
		e.logException()
	}
}

/**
 * start third party App
 *
 * *If App Installed
 */
fun Context.openApp(packageName: String, url: String)
{
	if(isAppInstalled(packageName))
		startActivity(packageManager.getLaunchIntentForPackage(packageName))
	else try
	{
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(packageName))
	}
	catch(e: ActivityNotFoundException)
	{
		e.message.logError()
		startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
	}
}

/**
 * Check if an App is Installed on the user device.
 */
fun Context.isAppInstalled(packageName: String): Boolean
{
	return try {
		packageManager.getApplicationInfo(packageName, 0)
		true
	} catch (ignore: Exception) {
		false
	}
}

inline fun <reified T: Activity> Context.resetActivity(extras: Bundle.() -> Unit = {})
{
	startActivity(
		Intent().setClass(this, T::class.java).putExtras(Bundle().apply(extras)).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
	)
}

inline fun <reified T: Activity> Context.startActivity(extras: Bundle.() -> Unit = {})
{
	startActivity(Intent().setClass(this, T::class.java).putExtras(Bundle().apply(extras)))
}

inline fun <reified T: Activity> Activity.startActivityForResult(requestCode: Int, extras: Bundle.() -> Unit = {})
{
	startActivityForResult(
		Intent().setClass(this, T::class.java).putExtras(Bundle().apply(extras)), requestCode
	)
}

inline fun <reified T: Activity> Context.resetActivity(vararg extras: Pair<String, Any?>)
{
	startActivity(
		Intent().setClass(this, T::class.java).putExtras(bundleOf(*extras)).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
	)}

inline fun <reified T: Activity> Context.startActivity(vararg extras: Pair<String, Any?>)
{
	startActivity(Intent().setClass(this, T::class.java).putExtras(bundleOf(*extras)))
}

inline fun <reified T: Activity> Activity.startActivityForResult(requestCode: Int, vararg extras: Pair<String, Any?>)
{
	startActivityForResult(
		Intent().setClass(this, T::class.java).putExtras(bundleOf(*extras)), requestCode
	)
}

fun Activity.pickImage(
	code: Int
	, allowMultiple: Boolean = false
	, title: String = getString(R.string.text_select_picture)
)
{
	Intent(Intent.ACTION_OPEN_DOCUMENT)
		.addCategory(Intent.CATEGORY_OPENABLE)
		.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, allowMultiple)
		.setType("image/*")
		.let { startActivityForResult(Intent.createChooser(it, title), code) }
}
