package com.ownapp.core.extensions.view

import android.graphics.Typeface
import android.text.*
import android.text.style.ClickableSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.view.View

/**
 * Updated by Robin on 2020/12/4
 */

/**
 * Change the text to bold
 * @param range The range of text to change the bold
 */
fun CharSequence.toBoldSpan(range: IntRange = IntRange(0, length)): SpannableString =
	toStyleSpan(Typeface.BOLD, IntRange(range.first, range.last))

/**
 * Change the text to italic
 * @param range The range of text to change the italic
 */
fun CharSequence.toItalicSpan(range: IntRange = IntRange(0, length)): SpannableString =
	toStyleSpan(Typeface.ITALIC, IntRange(range.first, range.last))

/**
 * Change the text to bold italic
 * @param range The range of text to change the bold italic
 */
fun CharSequence.toBoldItalicSpan(range: IntRange = IntRange(0, length)): SpannableString =
	toStyleSpan(Typeface.BOLD_ITALIC, IntRange(range.first, range.last))

/**
 * Change the text to normal text
 * @param range The range of text to change the normal style
 */
fun CharSequence.toNormalSpan(range: IntRange = IntRange(0, length)): SpannableString =
	toStyleSpan(Typeface.NORMAL, IntRange(range.first, range.last))

/**
 * Set the span to a styled one from Typeface
 * @param style one of [Typeface.NORMAL] etc..
 */
fun CharSequence.toStyleSpan(style: Int, range: IntRange = IntRange(0, length)): SpannableString {
	return SpannableString(this).apply {
		setSpan(StyleSpan(style), range.first, range.last, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
	}
}

/**
 * Add a strike through to the text of the specified range in the text
 * @param range The range of text to add strike through to
 */
fun CharSequence.toStrikeThroughSpan(range: IntRange = IntRange(0, length)): SpannableString {
	return SpannableString(this).apply {
		setSpan(StrikethroughSpan(), range.first, range.last, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
	}
}

/**
 * Add color and click event to the text of the specified range in the text
 * @param range Range of target text
 */
fun CharSequence.toClickSpan(range: IntRange = IntRange(0, length), color: Int? = null, isUnderlineText: Boolean = false
	, clickListener: (View) -> Unit
): SpannableString {
	return SpannableString(this).apply {
		val clickableSpan = object : ClickableSpan() {
			override fun onClick(view: View) {
				clickListener(view)
			}

			override fun updateDrawState(ds: TextPaint) {
				color?.let { ds.color =  it }
				ds.isUnderlineText = isUnderlineText
			}
		}
		setSpan(clickableSpan, range.first, range.last, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
	}
}

/**
 * Method to simplify the code needed to apply spans on a specific sub string.
 */
inline fun SpannableStringBuilder.withSpan(
	vararg spans: Any
	, action: SpannableStringBuilder.() -> Unit
): SpannableStringBuilder
{
	val from = length
	action()

	for (span in spans)
	{
		setSpan(span, from, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
	}

	return this
}