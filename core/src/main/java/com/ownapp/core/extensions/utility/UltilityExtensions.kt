package com.ownapp.core.extensions.utility

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.core.view.doOnLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ownapp.core.extensions.back
import com.ownapp.core.extensions.inputMethodManager
import com.ownapp.core.extensions.resource.encode
import com.ownapp.core.extensions.resource.isDecimalNumber
import com.ownapp.core.extensions.resource.isIntegerNumber
import com.ownapp.core.extensions.resource.lowerCased
import com.ownapp.core.extensions.rootView
import com.ownapp.core.extensions.toActivity
import com.ownapp.core.support.json.BooleanDeserializer
import com.ownapp.core.support.json.LiveDataTypeAdapterFactory
import com.ownapp.core.view.pager.SimpleViewPager2Adapter
import kotlinx.coroutines.*
import java.io.IOException
import java.io.Reader
import java.math.BigDecimal
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.text.DecimalFormat
import java.util.*
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.roundToInt

/**
 * Updated by Robin on 2020/12/20
 */

val Float.roundedOffString: String
    get() = DecimalFormat("##.##").format(this)

fun Float.roundOff(@IntRange(from = 0) decimalPlace: Int): Float
{
    return BigDecimal(this.toString()).setScale(decimalPlace, BigDecimal.ROUND_HALF_UP).toFloat()
}

fun Any?.toIntOrZero(): Int = toString().toIntOrNull() ?: 0
fun Any?.toIntOr(value: Int?): Int? = toString().toIntOrNull() ?: value
fun Any?.toLongOrZero(): Long = toString().toLongOrNull() ?: 0
fun Any?.toLongOr(value: Long?): Long? = toString().toLongOrNull() ?: value

val Any.commaNumberString: String
    get() = when(this)
    {
        is String -> try
        {
            when
            {
                isIntegerNumber -> String.format(Locale.getDefault(), "%,d", this.toInt())
                isDecimalNumber -> String.format(Locale.getDefault(), "%,d", this.toFloat().roundToInt())
                else -> throw(Exception("Not a valid String to convert to comma number"))
            }
        }
        catch(e: Exception)
        {
            e.logException()
            0.commaNumberString
        }

        is Float -> String.format(Locale.getDefault(), "%,d", roundToInt())
        is Double -> String.format(Locale.getDefault(), "%,d", roundToInt())
        is Int -> String.format(Locale.getDefault(), "%,d", this)
        else -> 0.commaNumberString
    }

val Any.commaFloatString: String?
    get() = try
    {
        when(this)
        {
            is String -> when
            {
                isIntegerNumber -> String.format(Locale.getDefault(), "%,.2f", this.toInt())
                isDecimalNumber -> String.format(Locale.getDefault(), "%,.2f", this.toFloat())
                else ->
                {
                    "Not a valid String to convert to comma float".log(value = this)
                    null
                }
            }

            is Float, is Double -> String.format(Locale.getDefault(), "%,.2f", this)
            is Int -> String.format(Locale.getDefault(), "%,.2f", this.toFloat())
            else -> null
        }
    }
    catch(e: Exception)
    {
        e.logException()
        null
    }

fun Any.getIntOrNull(): Int? = toString().getIntOrNull()

fun Any?.toBoolean(): Boolean = when(this)
{
    is Boolean -> this == true
    is Int -> this == 1
    is String -> HashSet(listOf("true", "1", "yes")).contains(this.lowerCased)
    else -> false
}

val Int.dp
    get() = toFloat().dp.roundToInt()

val Float.dp
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

val Int.sp
    get() = toFloat().sp.roundToInt()

val Float.sp
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics)

fun <R> CoroutineScope.executeAsyncTask(
    onPreExecute: () -> Unit, doInBackground: () -> R, onPostExecute: (R) -> Unit
) = launch {
    onPreExecute() // runs in Main Thread
    val result = withContext(Dispatchers.IO) {
        doInBackground() // runs in background thread without blocking the Main Thread
    }
    onPostExecute(result) // runs in Main Thread
}

fun <T> Collection<T>?.deepEqualTo(
    other: Collection<T>?
    , compare: (T, T)-> Boolean = { fromThis, fromOther -> fromThis == fromOther}
): Boolean
{
    if(this == null && other == null)
        return true

    if(this == null)
        return false

    if(other == null)
        return false

    // check collections aren't same
    if (this !== other)
    {
        // fast check of sizes
        if (this.size != other.size)
            return false

        val areNotEqual = this.asSequence()
            .zip(other.asSequence())
            // check this and other contains same elements at position
            .map { (fromThis, fromOther) -> compare(fromThis, fromOther) }
            // searching for first negative answer
            .contains(false)

        if (areNotEqual)
            return false
    }

    // collections are same or they are contains same elements with same order
    return true
}

infix fun <T> Collection<T>.deepEqualToIgnoreOrder(other: Collection<T>): Boolean
{
    // check collections aren't same
    if (this !== other) {
        // fast check of sizes
        if (this.size != other.size) return false
        val areNotEqual = this.asSequence()
            // check other contains next element from this
            .map { it in other }
            // searching for first negative answer
            .contains(false)
        if (areNotEqual) return false
    }
    // collections are same or they are contains same elements
    return true
}

inline fun <reified T: Any?, U: Any?> Map<T, U>?.toParameters(): String
{
    return if(!this.isNullOrEmpty())
        entries.asSequence()
            .map { (key, value) -> "${key.toString()}=${value.toString()}" }
            .joinToString("&")
    else ""
}

inline fun <reified T: Any?, U: Any?> Map<T, U>?.toEncodedParameters(): String
{
    return if(!this.isNullOrEmpty())
        entries.asSequence()
            .map { (key, value) -> "${key.toString().encode}=${value.toString().encode}" }
            .joinToString("&")
    else ""
}

fun Map<*, *>.parse(obj: Any?): String = parseOrNull(obj).orEmpty()

fun Map<*, *>.parseOrNull(obj: Any?): String?
{
    return when
    {
        obj == null -> null
        containsValue(obj) -> getKeyOrNull(value = obj)
        containsKey(obj) -> getValueOrNull(key = obj)
        else -> null
    }
}

fun Map<*, *>.getValueOrNull(key: Any): String? = entries.find { (k, _) -> k == key }?.let { it.value.toString() }
fun Map<*, *>.getKeyOrNull(value: Any): String? = entries.find { (_, v) -> v == value }?.let { it.key.toString() }


@Suppress("DEPRECATION")
fun AlertDialog.compatShow(context: Context)
{
    GlobalScope.launch(Dispatchers.Main) {
        window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
        window?.decorView?.systemUiVisibility = context.toActivity()?.window?.decorView?.systemUiVisibility ?: 0
        show()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }
}

val <T> T.prettyJson: String?
    get() = try
    {
        GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .registerTypeAdapterFactory(LiveDataTypeAdapterFactory())
            .registerTypeAdapter(Boolean::class.java, BooleanDeserializer())
            .create()
            .toJson(this, object: TypeToken<T>(){}.type)
    }
    catch(e: Exception)
    {
        e.message.logError()
        toString()
    }

inline fun <reified T> Gson.fromJson(json: Reader?): T? = fromJson(json, object: TypeToken<T>(){}.type)
inline fun <reified T> Gson.fromJson(json: String?): T? = fromJson(json, object: TypeToken<T>(){}.type)

fun LifecycleOwner.blockMultipleClick(view: View?, timeMillis: Long = 500)
{
    view?.run {
        if(isClickable)
        {
            isClickable = false
            lifecycleScope.launchDelay(timeMillis) { isClickable = true }
        }
    }
}

fun LifecycleOwner.blockMultipleClick(item: MenuItem?, timeMillis: Long = 500)
{
    item?.run {
        if(isEnabled)
        {
            isEnabled = false
            lifecycleScope.launchDelay(timeMillis) { isEnabled = true }
        }
    }
}

fun MenuItem?.blockMultipleClick(lifecycleOwner: LifecycleOwner, timeMillis: Long = 500)
{
    this?.run {
        if(isEnabled)
        {
            isEnabled = false
            lifecycleOwner.lifecycleScope.launchDelay(timeMillis) { isEnabled = true }
        }
    }
}

fun Context.showKeyboard()
{
    rootView?.run {
        doOnLayout {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                toActivity()?.window?.insetsController?.show(WindowInsets.Type.ime())
                    ?: it.windowInsetsController?.show(WindowInsets.Type.ime())
                    ?: context.inputMethodManager.toggleSoftInputFromWindow(
                        windowToken, InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY
                    )
            }
            else
            {
                context.inputMethodManager.toggleSoftInputFromWindow(
                    windowToken, InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY
                )
            }
        }
    } ?: "Can't find view: android.R.id.content".logError()
}

fun Context.hideKeyboard()
{
    rootView?.run {
        doOnLayout {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                toActivity()?.window?.insetsController?.hide(WindowInsets.Type.ime())
                    ?: windowInsetsController?.hide(WindowInsets.Type.ime())
                    ?: inputMethodManager.uglyHideKeyboard()
            }
            else windowToken?.let {
                inputMethodManager.hideSoftInputFromWindow(it, 0)
            } ?: inputMethodManager.uglyHideKeyboard()
        }
    } ?: run {
        "Can't find view: android.R.id.content".logError()
        inputMethodManager.uglyHideKeyboard()
    }
}

fun Activity.hideKeyboard()
{
    rootView?.run {
        doOnLayout {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            {
                window?.insetsController?.hide(WindowInsets.Type.ime())
                    ?: windowInsetsController?.hide(WindowInsets.Type.ime())
                    ?: inputMethodManager.uglyHideKeyboard()
            }
            else windowToken?.let {
                inputMethodManager.hideSoftInputFromWindow(it, 0)
            } ?: inputMethodManager.uglyHideKeyboard()
        }
    } ?: run {
        "Can't find view: android.R.id.content".logError()
        inputMethodManager.uglyHideKeyboard()
    }
}

fun Fragment.showKeyboard(context: Context? = this.context): Boolean
{
    context?.inputMethodManager?.let { imm ->
        view?.rootView?.windowToken?.let {
            imm.toggleSoftInputFromWindow(it, InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)
        } ?: imm.toggleSoftInputFromWindow(view?.rootView?.windowToken, InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS)
    } ?: activity?.showKeyboard()
    
    return context != null
}

fun Fragment.hideKeyboard(context: Context? = this.context): Boolean
{
    context?.inputMethodManager?.let { imm ->
        view?.rootView?.windowToken?.let {
            imm.hideSoftInputFromWindow(it, 0)
        } ?: imm.uglyHideKeyboard()
    } ?: activity?.hideKeyboard()

    return context != null
}


val InputMethodManager.keyboardHeight: Int
    get() = InputMethodManager::class.java.getMethod("getInputMethodWindowVisibleHeight").invoke(this) as Int

fun InputMethodManager.uglyHideKeyboard()
{
    try
    {
        if (keyboardHeight > 0)
            toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }
    catch (e: Exception)
    {
        e.logException()
    }
}

@ColorInt
fun ColorStateList.getEnabledColorOrDefault(@ColorInt default: Int = defaultColor): Int
{
    return getColorForState(intArrayOf(android.R.attr.state_enabled), default)
}

fun ColorStateList.getEnabledColorListOrDefault(@ColorInt default: Int = defaultColor): ColorStateList
{
    return ColorStateList.valueOf(getColorForState(intArrayOf(android.R.attr.state_enabled), default))
}

/**
 * Created by hristijan on 3/1/19 to long live and prosper !
 */

/**
 * Converts Boolean to Int, if true then 1 else 0
 */
fun Boolean.toInt(): Int = if (this) 1 else 0

/**
 * Toggle the Boolean Value, if it's true then it will become false ....
 */
fun Boolean.toggle() = !this

// TCP/HTTP/DNS (depending on the port, 53=DNS, 80=HTTP, etc.)
@RequiresPermission(Manifest.permission.INTERNET)
fun isInternetAvailable(): Boolean
{
    return try
    {
        val timeOutMs = 1500
        val socket = Socket()
        val socketAddress: SocketAddress = InetSocketAddress("8.8.8.8", 53)

        socket.connect(socketAddress, timeOutMs)
        socket.close()

        true
    }
    catch (e: IOException)
    {
        false
    }
}

/**
 * Use to treated [@receiver [T]] as an expression and the compiler will force us to specify all cases.
 */
val <T> T.exhaustive: T
    get() = this

/**
 * try the code in [runnable], If it runs then its perfect if its not, It won't crash your app.
 */
inline fun tryOrIgnore(runnable: () -> Unit) = try {
    runnable()
} catch (e: Exception) {
    e.message.logError()
}

inline fun <T> tryOrNull(block: () -> T): T? = try {
    block()
} catch (e: Exception) {
    e.message.logError()
    null
}

inline fun <T> tryOrElse(defaultValue: T, block: () -> T): T = tryOrNull(block) ?: defaultValue
inline fun tryOrElse(defaultBlock: () -> Unit = {}, block: () -> Unit) = try {
    block()
} catch (e: Exception) {
    e.message.logError()
    defaultBlock()
}

inline fun <reified T> Any?.cast() = this as? T
inline fun <reified T> Any.force() = this as T

fun generateUniqueNumber(): Int = (Date().time / 1000L % Int.MAX_VALUE).toInt()

inline fun CoroutineScope.launchDelay(
    timeMillis: Long
    , context: CoroutineContext = EmptyCoroutineContext
    , start: CoroutineStart = CoroutineStart.DEFAULT
    , crossinline block: suspend CoroutineScope.() -> Unit
): Job = launch(context, start) {
    delay(timeMillis)

    if(isActive)
        block()
}

fun ViewPager.getFragments() = adapter?.cast<com.ownapp.core.view.pager.PagerAdapter>()?.fragments

inline fun <reified T: Fragment> ViewPager.getCurrentFragment() = adapter?.getFragment<T>(currentItem)

inline fun <reified T: Fragment> PagerAdapter.getFragment(position: Int): T? =
    cast<com.ownapp.core.view.pager.PagerAdapter>()
        ?.fragments?.getOrNull(position)
        .cast<T>()

fun ViewPager2.back() = currentFragment?.back()

val ViewPager2.fragments
    get() = adapter?.cast<SimpleViewPager2Adapter>()?.fragments

inline val ViewPager2.currentFragment
    get() = fragments?.getOrNull(currentItem)

inline fun <reified T: Fragment> ViewPager2.getFragmentAs(position: Int): T? = fragments?.getOrNull(position).cast<T>()
inline fun <reified T: Fragment> ViewPager2.getCurrentFragmentAs() = getFragmentAs<T>(currentItem)


fun <T> Collection<T>.toSimpleArray() = toString().filterNot { "[]".indexOf(it) >= 0 }