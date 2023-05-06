package com.ownapp.core.extensions.utility

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration
import com.google.android.material.snackbar.Snackbar
import com.ownapp.core.R
import com.ownapp.core.annotation.ResourceType
import com.ownapp.core.extensions.nightMode
import com.ownapp.core.extensions.resource.getResourceTypeName
import com.ownapp.core.extensions.resource.toColor
import com.ownapp.core.extensions.resource.toString
import com.ownapp.core.extensions.rootView
import com.ownapp.core.util.SnackbarUtil
import com.ownapp.core.util.helper.Toaster
import timber.log.Timber
import java.lang.StringBuilder

/**
 * Updated by Robin on 2020/12/11
 */

//**--------------------------------------------------------------------------------------------------
//*      Log
//---------------------------------------------------------------------------------------------------*/
fun Any?.debug() =  Timber.d("$this")
fun Any?.info() = Timber.i("$this")
fun Any?.warn() =  Timber.w("$this")
fun Any?.error() = Timber.e("$this")
fun Exception.log() = Timber.e(this)

fun Pair<String?, Any?>.debug() = Timber.d("$first => ${second.toString()}")
fun Pair<String?, Any?>.info() = Timber.i("$first => ${second.toString()}")

fun debug(vararg pairs: Pair<String?, Any?>) = pairs.forEach { it.debug() }
fun info(vararg pairs: Pair<String?, Any?>) = pairs.forEach { it.info() }

fun debug(tag: String, vararg pairs: Pair<String?, Any?>) = with(StringBuilder()) {
	appendLine(tag)
	pairs.forEach { appendLine("${it.first} => ${it.second.toString()}") }
	Timber.d(toString())
}

fun info(tag: String, vararg pairs: Pair<String?, Any?>) = with(StringBuilder()) {
	appendLine(tag)
	pairs.forEach { appendLine("${it.first} => ${it.second.toString()}") }
	Timber.i(toString())
}


fun Any?.log() = debug()
fun Any?.log(prefix: String) = Timber.i("$prefix => ${this?.toString() ?: "null"}")
fun String.log(value: Any?) = Timber.i("$this => ${value?.toString() ?: "null"}")
fun Any?.logError() = Timber.e(this?.toString() ?: "null")
fun Any?.logWarn() = Timber.w(this?.toString() ?: "null")
fun Exception.logException() = log()

fun Pair<String, Any?>.logError() = when
{
//	second.toString().isBlank() -> first.log()
	first.isBlank() -> second.log()
	else -> Timber.e("$first => ${second?.toString() ?: "null"}")
}

fun Pair<String, Any?>.log() = when
{
//	second.toString().isBlank() -> first.log()
	first.isBlank() -> second.debug()
	else -> Timber.i("$first => ${second?.toString() ?: "null"}")
}

//fun Map<String, Any?>.log() = forEach { it.log() }

fun log(vararg values: Any?) = values.forEach { it.log() }
fun log(vararg pairs: Pair<String, Any?>) = pairs.forEach { it.log() }
fun log(tag: String, vararg values: Any?) = values.forEach { (tag to it).log() }
fun log(tag: String, vararg pairs: Pair<String, Any?>) = with(StringBuilder(tag)) {
	pairs.forEach { append("\n${it.first} => ${it.second?.toString() ?: "null"}") }
	Timber.d(toString())
}


//**--------------------------------------------------------------------------------------------------
//*      Toast
//---------------------------------------------------------------------------------------------------*/
fun Fragment.toast(obj: Any?) = context?.toast(obj) ?: "Context is null from ${this::class.simpleName}".logError()
fun Context.toast(obj: Any?) = obj.toast(this)

fun Any?.toast(context: Context)
{
	when(this)
	{
		is String -> Toaster.show(context, this.toString())
		is Int ->
		{
			if(getResourceTypeName(context) == ResourceType.STRING) Toaster.show(context, this)
			else Toaster.show(context, this.toString())
		}
	}
}


//**--------------------------------------------------------------------------------------------------
//*      Snack
//---------------------------------------------------------------------------------------------------*/
fun Fragment.snack(obj: Any?) = context?.snack(obj) ?: "Context is null from ${this::class.simpleName}".logError()

fun Context.snack(obj: Any?, @Duration duration: Int = Snackbar.LENGTH_LONG)
{
	obj.snack(this, duration)
}

fun View.snack(obj: Any?, @Duration duration: Int = Snackbar.LENGTH_LONG)
{
	obj.snack(this, duration)
}

fun Any?.snack(context: Context, @Duration duration: Int = Snackbar.LENGTH_LONG)
{
	context.rootView?.let {
		when(this)
		{
			is String -> snack(it, duration)
			is Int -> snack(it, duration)
			else -> toString().snack(it, duration)
		}
	} ?: "Snack failed: Can't find android.R.id.content".logError()
}

fun Any?.snack(view: View?, @Duration duration: Int = Snackbar.LENGTH_LONG)
{
	if(view != null)
	{
		when(this)
		{
			is String -> SnackbarUtil.show(view, this, duration)
			is Int ->
			{
				if(getResourceTypeName(view.context) == ResourceType.STRING) SnackbarUtil.show(view, this, duration)
				else SnackbarUtil.show(view, this.toString(), duration)
			}
		}
	}
}

fun Snackbar.withButton(obj: Any?, @ColorInt textColor: Int?, action: () -> Unit)
{
	when(obj)
	{
		is String -> setAction(obj) { action() }
		is Int -> setAction(obj) { action() }
		else -> setAction(obj.toString()) { action() }
	}

	if(textColor != null)
		setActionTextColor(textColor)
}


//**--------------------------------------------------------------------------------------------------
//*      Dialog
//---------------------------------------------------------------------------------------------------*/
inline fun Context.simpleDialogBuilder(
	message: Any? = null
	, @StringRes positiveButtonStringRes: Int = R.string.text_ok
	, crossinline action: () -> Unit = {}
): MaterialAlertDialogBuilder
{
	return MaterialAlertDialogBuilder(this).apply {
		when(message)
		{
			is Int -> setMessage(message.toString(this@simpleDialogBuilder))
			is String -> setMessage(message.toString())
		}

		setPositiveButton(positiveButtonStringRes) { dialog, _ ->
			action()
			dialog.dismiss()
		}
		setCancelable(false)
	}
}

inline fun Context.simpleAlert(
	message: Any?
	, @StringRes positiveButtonStringRes: Int = R.string.text_ok
	, crossinline action: () -> Unit = {}
): AlertDialog
{
	return simpleDialogBuilder(message, positiveButtonStringRes, action).create().apply {
		setOnShowListener {
			getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
				{
					setTextAppearance(R.style.TextAppearance_MaterialComponents_Body2)
					isAllCaps = true
				}

				if(nightMode == Configuration.UI_MODE_NIGHT_NO)
					setTextColor(R.attr.alertDialogPositiveButtonColor.toColor(this@simpleAlert))
			}
		}
	}
}

inline fun Context.simpleCancelableAlert(
	message: Any?
	, @StringRes positiveButtonStringRes: Int = R.string.text_ok
	, crossinline action: () -> Unit = {}
): AlertDialog
{
	return simpleDialogBuilder(message, positiveButtonStringRes, action).apply {
		setNegativeButton(R.string.text_cancel) { _, _ -> }
	}.create().apply {
		setOnShowListener {
			getButton(AlertDialog.BUTTON_POSITIVE)?.apply {
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
				{
					setTextAppearance(R.style.TextAppearance_MaterialComponents_Body2)
					isAllCaps = true
				}

				if(nightMode == Configuration.UI_MODE_NIGHT_NO)
					setTextColor(R.attr.alertDialogPositiveButtonColor.toColor(this@simpleCancelableAlert))
			}

			getButton(AlertDialog.BUTTON_NEGATIVE)?.apply {
				if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N)
				{
					setTextAppearance(R.style.TextAppearance_MaterialComponents_Body2)
					isAllCaps = true
				}

				if(nightMode == Configuration.UI_MODE_NIGHT_NO)
					setTextColor(R.attr.alertDialogNegativeButtonColor.toColor(this@simpleCancelableAlert))
			}
		}
	}
}

inline fun Context.simpleConfirmAlert(message: Any?, crossinline action: () -> Unit = {}): AlertDialog
{
	return simpleCancelableAlert(message, R.string.text_confirm, action)
}

inline fun Context.showAlert(message: Any?, @StringRes positiveButtonStringRes: Int = R.string.text_ok, crossinline action: () -> Unit = {})
{
	simpleAlert(message, positiveButtonStringRes, action).compatShow(this)
}

inline fun Context.showCancelableAlert(message: Any?, @StringRes positiveButtonStringRes: Int = R.string.text_ok, crossinline action: () -> Unit = {})
{
	simpleCancelableAlert(message, positiveButtonStringRes, action).compatShow(this)
}

inline fun Context.showConfirmAlert(message: Any?, crossinline action: () -> Unit = {})
{
	simpleConfirmAlert(message, action).compatShow(this)
}

inline fun Fragment.simpleCancelableAlert(
	message: Any?
	, @StringRes positiveButtonStringRes: Int = R.string.text_ok
	, crossinline action: () -> Unit = {}
) = requireContext().simpleCancelableAlert(message, positiveButtonStringRes, action)

inline fun Fragment.showAlert(message: Any?, @StringRes positiveButtonStringRes: Int = R.string.text_ok, crossinline action: () -> Unit = {})
{
	requireActivity().showAlert(message, positiveButtonStringRes, action)
}

inline fun Fragment.showConfirmAlert(message: Any?, crossinline action: () -> Unit = {})
{
	requireActivity().showConfirmAlert(message, action)
}

inline fun Fragment.showCancelableAlert(message: Any?, @StringRes positiveButtonStringRes: Int = R.string.text_ok, crossinline action: () -> Unit = {})
{
	requireContext().showCancelableAlert(message, positiveButtonStringRes, action)
}

/**
 * Want your user to choose Single thing from a bunch? call showSinglePicker and provide your options to choose from
 */
inline fun Context.showSinglePicker(
	choices: Array<String>
	, crossinline onResponse: (index: Int) -> Unit
	, checkedItemIndex: Int = -1
): AlertDialog = MaterialAlertDialogBuilder(this)
	.setSingleChoiceItems(choices, checkedItemIndex) { dialog, which ->
		onResponse(which)
		dialog.dismiss()
	}.show()

/**
 * Want your user to choose Multiple things from a bunch? call showMultiPicker and provide your options to choose from
 */
fun Context.showMultiPicker(
	choices: Array<String>
	, onResponse: (index: Int, isChecked: Boolean) -> Unit
	, checkedItems: BooleanArray? = null
): AlertDialog = MaterialAlertDialogBuilder(this)
	.setMultiChoiceItems(choices, checkedItems) { _, which, isChecked ->
		onResponse(which, isChecked)
	}.setPositiveButton(R.string.text_confirm, null)
	.show()
