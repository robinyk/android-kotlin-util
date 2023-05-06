package com.ownapp.core.extensions

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.DownloadManager
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.TelephonyManager
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.ownapp.core.extensions.utility.*

/**
 * Updated by Robin on 2020/12/4
 */

///**
// * get Android ID
// */
//val Context.getAndroidID: String?
//    @SuppressLint("HardwareIds")
//    get() {
//        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
//    }

inline val Context.applicationName: String
    get() = if(applicationInfo.labelRes == 0)
        applicationInfo.nonLocalizedLabel.toString()
    else getString(applicationInfo.labelRes)

@Suppress("DEPRECATION")
inline val Context.versionCode: Long
    get() = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
        packageManager.getPackageInfo(packageName, 0).longVersionCode
    else
        packageManager.getPackageInfo(packageName, 0).versionCode.toLong()

inline val Context.versionName: String
    get() = packageManager.getPackageInfo(packageName, 0).versionName

inline val Context.isDebuggable: Boolean
    get() = applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

inline val Context.isTablet: Boolean
    get() = this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE

inline val Context.activityManager
    get() = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager?

inline val Context.windowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

inline val Context.connectivityManager
    get() = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

inline val Context.inputMethodManager
    get() = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

inline val Context.clipboardManager
    get() = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

inline val Context.layoutInflater: LayoutInflater
    get() = LayoutInflater.from(this)

inline val Context.telephonyManager
    get() = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

inline val Context.downloadManager
    get() = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

inline val Context.locationManager
    get() = getSystemService(Context.LOCATION_SERVICE) as LocationManager?

fun Context.isNetworkConnected(): Boolean
{
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
    {
        connectivityManager?.getNetworkCapabilities(connectivityManager?.activeNetwork)?.run {
            return hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) || hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        }

        false
    }
    else
    {
        @Suppress("DEPRECATION")
        connectivityManager?.activeNetworkInfo?.isConnected == true
    }
}

inline val Context.isLocationEnabled: Boolean
    get() = locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false

inline val Context.nightMode: Int
    get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

inline val Context.rootView: View?
    get() = toActivity()?.contentRootView

inline val Context.screenRect: Rect
    @RequiresApi(Build.VERSION_CODES.R)
    get()
    {
        DisplayMetrics().apply { display?.getRealMetrics(this) }.let {
            return Rect(0, 0, it.widthPixels, it.heightPixels)
        }
    }

fun Context.toActivity(): Activity?
{
    var context = this

    while (context is ContextWrapper)
    {
        if (context is Activity)
            return context

        context = context.baseContext
    }

    "Failed to convert Context to Activity".log(value = this)
    return null
}


//**--------------------------------------------------------------------------------------------------
//*      Resource
//---------------------------------------------------------------------------------------------------*/
fun Context.getEntryName(@AnyRes resId: Int): String?
{
    return try
    {
        if (resId == View.NO_ID)
        {
            "$resId: View not found".logError()
            null
        }
        else
            resources.getResourceEntryName(resId)
    }
    catch(e: Resources.NotFoundException)
    {
        e.message.error()
        null
    }
}

/**
 * {@link ContextCompat#getColor(int)}.
 */
fun Context.getColorCompat(@ColorRes colorId: Int) = ContextCompat.getColor(this, colorId)

/**
 * {@link ContextCompat#getDrawable(int)}.
 */
fun Context.getDrawableCompat(@DrawableRes drawableId: Int) = ContextCompat.getDrawable(this, drawableId)


//**--------------------------------------------------------------------------------------------------
//*      Utility
//---------------------------------------------------------------------------------------------------*/
fun Context.isPermissionGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun Context.arePermissionsGranted(vararg permissions: String): Boolean =
    permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

/**
 * Check if the app is still running
 * @return [Boolean]
 */
inline val Context.isAppAlive: Boolean
    get() = activityManager?.runningAppProcesses?.map { it.processName == packageName }?.contains(true) ?: false

/**
 * Check if app is in background
 * @return [Boolean]
 */
inline val isAppInBackground: Boolean
    get() = ProcessLifecycleOwner.get().lifecycle.currentState == Lifecycle.State.CREATED

/**
 * Check if app is in foreground
 * @return [Boolean]
 */
inline val isAppInForeground: Boolean
    get() = ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

