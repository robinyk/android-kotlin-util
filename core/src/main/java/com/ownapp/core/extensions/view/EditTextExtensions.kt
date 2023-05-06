package com.ownapp.core.extensions.view

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.text.InputFilter
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.BindingAdapter
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ownapp.core.extensions.utility.cast
import com.ownapp.core.extensions.utility.getEnabledColorOrDefault
import com.ownapp.core.extensions.utility.logError
import java.lang.reflect.Field
import kotlin.math.max
import kotlin.math.min


/**
 * Updated by Robin on 2020/12/15
 */


val TextInputEditText.textString: String
	get() = this.text.toString()
val EditText.textString: String
	get() = this.text.toString()

fun EditText.setMaxLength(length: Int)
{
	filters = arrayOf(InputFilter.LengthFilter(length))
}

/**
 * A keyboard handler for EditText that filters input by regex
 */
inline fun <T : EditText> T.filterInputByRegex(regex: Regex, crossinline onTextChanged: (String) -> Unit = {}): T {
	doAfterTextChanged {
		val input = it?.toString() ?: ""
		val result = input.replace(regex, "")
		if (input != result) {
			val pos = this.selectionStart - (input.length - result.length)
			this.setText(result)
			this.setSelection(max(0, min(pos, result.length)))
		} else {
			onTextChanged.invoke(input)
		}
	}
	return this
}

/**
 * A keyboard handler for EditText that prohibits entering spaces, tabs, and so on
 */
inline fun <T : EditText> T.filterWhiteSpaces(crossinline onTextChanged: (String) -> Unit = {}) =
	filterInputByRegex("\\s".toRegex(), onTextChanged)

@SuppressLint("ClickableViewAccessibility")
fun EditText.disableCopyAndPaste()
{
	try {
		this.setOnLongClickListener { true }
		this.isLongClickable = false
		this.setOnTouchListener { _, event ->
			if (event.action == MotionEvent.ACTION_DOWN) {
				this.setInsertionDisabled()
			}
			false
		}
		this.setTextIsSelectable(false)
		this.customSelectionActionModeCallback = object: ActionMode.Callback {
			override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
				return false
			}

			override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
				return false
			}

			override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
				return false
			}

			override fun onDestroyActionMode(mode: ActionMode) {

			}
		}

	} catch (e: Exception) {
		e.printStackTrace()
	}
}

fun EditText.setInsertionDisabled() {
	try {
		val editorField = TextView::class.java.getDeclaredField("mEditor")
		editorField.isAccessible = true
		val editorObject = editorField.get(this)

		// if this view supports insertion handles
		@SuppressLint("PrivateApi") val editorClass = Class.forName("android.widget.Editor")
		val mInsertionControllerEnabledField = editorClass.getDeclaredField("mInsertionControllerEnabled")
		mInsertionControllerEnabledField.isAccessible = true
		mInsertionControllerEnabledField.set(editorObject, false)

		// if this view supports selection handles
		val mSelectionControllerEnabledField = editorClass.getDeclaredField("mSelectionControllerEnabled")
		mSelectionControllerEnabledField.isAccessible = true
		mSelectionControllerEnabledField.set(editorObject, false)
	} catch (e: Exception) {
		e.printStackTrace()
	}
}

fun EditText.setCursorColor(colorStateList: ColorStateList) = setCursorColor(colorStateList.getEnabledColorOrDefault())

fun EditText.setCursorColor(@ColorInt color: Int)
{
	try
	{
		// Get the cursor resource id
		var field: Field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
		field.isAccessible = true
		val drawableResId: Int = field.getInt(this)

		// Get the editor
		field = TextView::class.java.getDeclaredField("mEditor")
		field.isAccessible = true
		val editor: Any? = field.get(this)

		// Get the drawable and set a color filter
		val drawable = ContextCompat.getDrawable(context, drawableResId)

		drawable?.apply {
			colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
			val drawables = arrayOf<Drawable?>(this, this)

			// Set the drawables
			editor.apply {
				field = javaClass.getDeclaredField("mCursorDrawable")
				field.isAccessible = true
				field.set(this, drawables)
			}
		}
	}
	catch (e: Exception)
	{
		e.message.logError()
	}
}

fun EditText.setCursorDrawable(@DrawableRes resId: Int)
{
	try
	{
		TextView::class.java.getDeclaredField("mCursorDrawableRes").apply {
			isAccessible = true
			set(this, resId)
		}
	}
	catch (e: Exception)
	{
		e.message.logError()
	}
}

@BindingAdapter("android:text")
fun EditText.setTextWithCorrectSelection(charSequence: CharSequence?)
{
	setText(charSequence ?: "")
	setSelection(textString.length)
}

fun EditText.setSpannableTextWithCorrectSelection(charSequence: CharSequence)
{
	setText(charSequence, TextView.BufferType.SPANNABLE)
	setSelection(textString.length)
}

@BindingAdapter("helperText")
fun setHelperText(view: TextInputLayout, charSequence: CharSequence?)
{
	view.helperText = charSequence.toString()
}

@BindingAdapter("hint", "label", requireAll = true)
fun TextInputEditText.setFocusedLabelHint(hint: String?, label: String?)
{
	parent.parent.cast<TextInputLayout>()?.let {
		if(hasFocus() || !text.isNullOrBlank())
			it.hint = label
		else
			it.hint = hint
		
		setOnFocusChangeListener { _, hasFocus ->
			if(hasFocus || !text.isNullOrBlank())
				it.hint = label
			else
				it.hint = hint
		}
	}
}