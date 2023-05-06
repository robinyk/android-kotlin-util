package com.ownapp.core.extensions

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback
import com.ownapp.core.R
import com.ownapp.core.extensions.resource.getAttrColor
import com.ownapp.core.extensions.utility.cast
import com.ownapp.core.extensions.utility.logException

/**
 * Updated by Robin on 2020/12/4
 */

/** get a material container arc transform. */
internal fun getContentTransform(): MaterialContainerTransform
{
    return MaterialContainerTransform().apply {
        addTarget(android.R.id.content)
        duration = 450
        setPathMotion(MaterialArcMotion())
    }
}

/** apply material exit container transformation. */
fun Activity.applyExitMaterialTransform()
{
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    setExitSharedElementCallback(MaterialContainerTransformSharedElementCallback())
    window.sharedElementsUseOverlay = false
}

/** apply material entered container transformation. */
fun Activity.applyMaterialTransform(transitionName: String)
{
    window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
    ViewCompat.setTransitionName(findViewById(android.R.id.content), transitionName)

    // set up shared element transition
    setEnterSharedElementCallback(MaterialContainerTransformSharedElementCallback())
//    window.sharedElementEnterTransition = getContentTransform()
//    window.sharedElementReturnTransition = getContentTransform()
}

fun Activity.hideSystemUI()
{
    WindowCompat.setDecorFitsSystemWindows(window, false)
}

fun Activity.showSystemUI()
{
    WindowCompat.setDecorFitsSystemWindows(window, true)
}

val Activity.isTopActivity: Boolean
    get()
    {
        return (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager?)?.let {
             this::javaClass.name == it.runningAppProcesses?.get(0)?.processName
        } ?: false
    }

/**
 * Change screen brightness with [WindowManager.LayoutParams] or with [Float] value
 * @sample WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
 * @sample WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF
 * @sample WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
 * */
var Activity.screenBrightness: Float
    get() = window.attributes.screenBrightness
    set(value)
    {
        try
        {
            var brightness = value

            if (brightness >= 1f)
                brightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL

            window.attributes = window.attributes.apply { screenBrightness = brightness }
        }
        catch(e: Exception)
        {
            e.logException()
        }
    }

val Activity.contentRootView: View?
    get() = window?.decorView?.findViewById(android.R.id.content) ?: findViewById(android.R.id.content)

inline fun AppCompatActivity.replaceFragment(
    @IdRes fragmentLayoutId: Int
    , fragment: Fragment
    , removeFromBackstack: Boolean = false
    , enableSlide: Boolean = false
    , tag: String? = fragment::class.simpleName
    , block: (FragmentTransaction) -> FragmentTransaction = { it }
) = supportFragmentManager.replace(fragmentLayoutId, fragment, removeFromBackstack, enableSlide, tag, block)

inline fun AppCompatActivity.changeFragment(
    @IdRes fragmentLayoutId: Int
    , fragment: Fragment
    , removeFromBackstack: Boolean = false
    , enableSlide: Boolean = false
    , uniqueTag: Any? = ""
    , block: (FragmentTransaction) -> FragmentTransaction = { it }
) = supportFragmentManager.change(fragmentLayoutId, fragment, removeFromBackstack, enableSlide, uniqueTag, block)

fun Activity.setupStatusBarColor() = setupStatusBarColor(window)

fun Context.setupStatusBarColor(window: Window?)
{
    if(window != null)
    {
        when(nightMode)
        {
            Configuration.UI_MODE_NIGHT_NO  ->
                window.statusBarColor = getAttrColor(android.R.attr.colorPrimaryDark)

            Configuration.UI_MODE_NIGHT_YES ->
                window.statusBarColor = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    getAttrColor(android.R.attr.colorBackgroundFloating)
                else getAttrColor(R.attr.colorBackgroundFloating)
        }
    }
}

// val Activity.currentFragment: Fragment?
//     get() = cast<AppCompatActivity>()?.currentFragment
//
// val AppCompatActivity.currentFragment: Fragment?
//     get() = supportFragmentManager.fragments.firstOrNull { it.isVisible }