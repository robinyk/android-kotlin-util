package com.ownapp.core.util.helper

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import androidx.browser.customtabs.CustomTabsService
import com.ownapp.core.extensions.utility.logError
import java.util.*

/**
 * Updated by Robin on 2020/12/4
 */

/**
 * Helper for [androidx.browser.customtabs.CustomTabsIntent] to
 * get available package name for chrome
 *
 * <queries>
 *     <intent>
 *         <action android:name=
 *             "android.support.customtabs.action.CustomTabsService" />
 *     </intent>
 * </queries>
 */
object CustomTabsPackageHelper
{
    private const val STABLE_PACKAGE = "com.android.chrome"
    private const val BETA_PACKAGE = "com.chrome.beta"
    private const val DEV_PACKAGE = "com.chrome.dev"
    private const val LOCAL_PACKAGE = "com.google.android.apps.chrome"
    private var packageNameToUse: String? = null

    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     *
     *
     * This is **not** threadsafe.
     *
     * @param context [Context] to use for accessing [PackageManager].
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    fun getPackageNameToUse(context: Context): String?
    {
        if (packageNameToUse != null)
        {
            return packageNameToUse
        }
        val pm = context.packageManager

        // Get default VIEW intent handler.
        val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"))
        val defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0)
        var defaultViewHandlerPackageName: String? = null

        if (defaultViewHandlerInfo != null)
        {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName
        }

        // Get all apps that can handle VIEW intents.
        val resolvedActivityList = pm.queryIntentActivities(activityIntent, 0)
        val packagesSupportingCustomTabs: MutableList<String?> = ArrayList()
        for (info in resolvedActivityList)
        {
            val serviceIntent = Intent()
            serviceIntent.action = CustomTabsService.ACTION_CUSTOM_TABS_CONNECTION
            serviceIntent.setPackage(info.activityInfo.packageName)

            if (pm.resolveService(serviceIntent, 0) != null)
            {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName)
            }
        }

        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty())
        {
            packageNameToUse = null
        }
        else if (packagesSupportingCustomTabs.size == 1)
        {
            packageNameToUse = packagesSupportingCustomTabs[0]
        }
        else if (!TextUtils.isEmpty(defaultViewHandlerPackageName) && !hasSpecializedHandlerIntents(context, activityIntent) && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName))
        {
            packageNameToUse = defaultViewHandlerPackageName
        }
        else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE))
        {
            packageNameToUse = STABLE_PACKAGE
        }
        else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE))
        {
            packageNameToUse = BETA_PACKAGE
        }
        else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE))
        {
            packageNameToUse = DEV_PACKAGE
        }
        else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE))
        {
            packageNameToUse = LOCAL_PACKAGE
        }
        return packageNameToUse
    }

    /**
     * Used to check whether there is a specialized handler for a given intent.
     *
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private fun hasSpecializedHandlerIntents(context: Context, intent: Intent): Boolean
    {
        try
        {
            val pm = context.packageManager
            val handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER)

            if (handlers.size == 0)
            {
                return false
            }

            for (resolveInfo in handlers)
            {
                val filter = resolveInfo.filter ?: continue
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue
                if (resolveInfo.activityInfo == null) continue
                return true
            }
        }
        catch (e: RuntimeException)
        {
            "Runtime exception while getting specialized handlers".logError()
        }
        return false
    }

    /**
     * @return All possible chrome package names that provide custom tabs feature.
     */
    val packages: List<String>
        get() = listOf("", STABLE_PACKAGE, BETA_PACKAGE, DEV_PACKAGE, LOCAL_PACKAGE)
}