package com.ownapp.core.extensions.view

import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.graphics.Paint
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.format.DateUtils
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.databinding.BindingAdapter
import com.ownapp.core.annotation.ResourceType
import com.ownapp.core.extensions.getColorCompat
import com.ownapp.core.extensions.resource.getDiscountPercentage
import com.ownapp.core.extensions.resource.getPercentage
import com.ownapp.core.extensions.resource.getResourceTypeName
import com.ownapp.core.extensions.resource.toColorStateList
import com.ownapp.core.extensions.utility.*
import com.ownapp.core.util.DateTimeUtil.parseDate
import com.ownapp.core.util.DateTimeUtil.parseDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.math.ln
import kotlin.math.pow

/**
 * Updated by Robin on 2021/1/21
 */


val TextView.textString: String get() = this.text.toString()

@BindingAdapter("onClickSpan", "spanClickRegex", "spanTextColor", "spanTextUnderline", requireAll = false)
fun TextView.setClickSpan(
	listener: View.OnClickListener
	, regex: String? = null
	, textColor: Int? = null
	, isUnderline: Boolean = false
)
{
	addTextChangedListener(object: TextWatcher {
		override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
		{
		}
		
		override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
		{
		}
		
		override fun afterTextChanged(s: Editable?)
		{
			if(!text.isNullOrBlank())
			{
				var range = IntRange(0, text.length)
				// val color = when(textColor)
				// {
				// 	is ColorStateList -> setBackgroundColor(textColor.getEnabledColorOrDefault())
				// 	is Int -> when(textColor.getResourceTypeName(context))
				// 	{
				// 		ResourceType.COLOR -> setBackgroundColor(textColor)
				// 		ResourceType.ATTRIBUTE -> setBackgroundColor(textColor.toColorStateList(context).defaultColor)
				// 		else -> null
				// 	}
				// 	else -> null
				// }
				
				if(!regex.isNullOrBlank() && text.contains(regex))
					range = IntRange(text.indexOf(regex), text.indexOf(regex) + regex.length)
				
				removeTextChangedListener(this)
				text = text.toClickSpan(range, textColor, isUnderline) { listener.onClick(it) }
				addTextChangedListener(this)
			}
		}
	})
}


/**
 * Enabled TextView multiline and disable user to enter new blank Line
 *
 * @param maxLines Default is 5
 */
fun TextView.multiLineTreatment(maxLines: Int = 5)
{
	isSingleLine = true
	setHorizontallyScrolling(false)
	this.maxLines = maxLines
}

@BindingAdapter("trim")
fun TextView.trimText(isTrue: Boolean? = true)
{
	if(isTrue == true && text != null)
		text = text.trim()

	addTextChangedListener(object: TextWatcher
	{
		override fun afterTextChanged(p0: Editable?)
		{
			if(isTrue == true && text != null)
			{
				removeTextChangedListener(this)
				text = text.trim()
				addTextChangedListener(this)
			}
		}

		override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
		{
		}

		override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int)
		{
		}
	})
}

// @BindingAdapter("prefixText", "withSpace", requireAll = false)
fun TextView.setPrefix(prefix: String?, withSpace: Boolean = true)
{
	if(!prefix.isNullOrBlank() && !text.contains(prefix))
	{
		text = if(withSpace)
			String.format("%s %s", prefix, text.toString())
		else String.format("%s%s", prefix, text.toString())
	}
}

// @BindingAdapter("suffixText", "withSpace", requireAll = false)
fun TextView.setSuffix(suffix: String?, withSpace: Boolean = true)
{
	if(!suffix.isNullOrBlank() && !text.contains(suffix))
	{
		text = if(withSpace)
			String.format("%s %s", text.toString(), suffix)
		else String.format("%s%s", text.toString(), suffix)
	}
}

@BindingAdapter("prefixText", "withSpace", requireAll = false)
fun TextView.setPrefixListener(prefix: String?, withSpace: Boolean = true)
{
	setPrefix(prefix)
	doAfterTextChanged { setPrefix(prefix, withSpace) }
	
	// addTextChangedListener(object: TextWatcher {
	// 	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
	// 	{
	//
	// 	}
	//
	// 	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
	// 	{
	//
	// 	}
	//
	// 	override fun afterTextChanged(editable: Editable?)
	// 	{
	// 		removeTextChangedListener(this)
	// 		setPrefix(prefix, withSpace)
	// 	}
	// })
}

@BindingAdapter("suffixText", "withSpace", requireAll = false)
fun TextView.addSuffixListener(suffix: String?, withSpace: Boolean = true)
{
	setSuffix(suffix)
	doAfterTextChanged { setSuffix(suffix, withSpace) }
	
	// addTextChangedListener(object: TextWatcher {
	// 	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
	// 	{
	//
	// 	}
	//
	// 	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
	// 	{
	//
	// 	}
	//
	// 	override fun afterTextChanged(editable: Editable?)
	// 	{
	// 		removeTextChangedListener(this)
	// 		setSuffix(suffix, withSpace)
	// 	}
	// })
}

/**
 * UnderLine the TextView.
 */
@BindingAdapter("underline")
fun TextView.underLine(enable: Boolean = true)
{
	if(enable)
	{
		paint.flags = paint.flags or Paint.UNDERLINE_TEXT_FLAG
		paint.isAntiAlias = true
	}
	else
	{
		paintFlags = paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
	}
}

/**
 * StrikeThrough Line in TextView.
 */
@BindingAdapter("strikeThrough")
fun TextView.strikeThroughSpan(enable: Boolean = true)
{
	if(enable)
	{
		paint.flags = paint.flags or Paint.STRIKE_THRU_TEXT_FLAG
		paint.isAntiAlias = true
	}
	else
	{
		paintFlags = paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
	}
}

/**
 * Set price with comma decimal and currency prefix
 *
 * @param value [Any]
 * @param currency [String?]
 */
@BindingAdapter(value = ["price", "currency"], requireAll = false)
fun TextView.setPrice(value: Any?, currency: String? = null)
{
	GlobalScope.launch(Dispatchers.Main) {
		StringBuilder().run {
			value?.commaFloatString.let {
				if(it != null)
				{
					if(!currency.isNullOrBlank())
						append("$currency ")

					append(it.orEmpty())
				}
				else append(value.toString())
			}
			text = toString()
		}
	}
}

@BindingAdapter(value = ["percentageOf", "fromValue"], requireAll = true)
fun TextView.setPercentage(value: Float, fromValue: Float)
{
	text = value.getPercentage(fromValue).roundedOffString
}

@BindingAdapter(value = ["discount", "original"], requireAll = true)
fun TextView.setDiscountPercentage(value: Float, fromValue: Float)
{
	text = value.getDiscountPercentage(fromValue).roundedOffString
}

@BindingAdapter("parseDateTime")
fun TextView.parseDateTime(date: String?)
{
	text = date?.parseDateTime().orEmpty()
}

@BindingAdapter("parseDate")
fun TextView.parseDate(date: String?)
{
	text = date?.parseDate().orEmpty()
}

@BindingAdapter("relativeTime", "showTime", requireAll = false)
fun TextView.setSimpleRelativeTime(timeInMillis: Long, showTime: Boolean = false)
{
	text = if(!showTime)
		DateUtils.getRelativeTimeSpanString(
			timeInMillis.toTimeZonedMillis()
			, now
			, DateUtils.SECOND_IN_MILLIS
			, DateUtils.FORMAT_ABBREV_RELATIVE
		)
	else
		DateUtils.getRelativeDateTimeString(
			context
			, timeInMillis.toTimeZonedMillis()
			, DateUtils.SECOND_IN_MILLIS
			, DateUtils.WEEK_IN_MILLIS
			, DateUtils.FORMAT_ABBREV_RELATIVE
		)
}

/**
 * Set different color for substring TextView.
 */
fun TextView.setColorOfSubstring(substring: String, @ColorInt color: Int)
{
	try
	{
		val spannable = android.text.SpannableString(text)
		val start = text.indexOf(substring)
		spannable.setSpan(
			ForegroundColorSpan(color),
			start,
			start + substring.length,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
		)
		text = spannable
	}
	catch (e: Exception)
	{
		"Exception in setColorOfSubstring, text=$text, substring=$substring =>${e.message}".logError()
	}
}

fun TextView.setTextColorId(@ColorRes id: Int) = setTextColor(context.getColorCompat(id))

/**
 * Setting new text to TextView with something like fade to alpha animation
 *
 * @property text - text to set to TextView
 * @property duration - animation final duration
 */
fun TextView.setTextWithAnimation(text: String, duration: Long)
{
	val stepDuration = duration / 2
	this.animate()
		.alpha(0f)
		.setDuration(stepDuration)
		.withEndAction {
			this.text = text
			this.animate()
				.alpha(1f)
				.setDuration(stepDuration)
				.start()
		}.start()
}

/**
 * Setting new text to TextView with something like width transition animation
 *
 * @property text - text to set to TextView
 * @property animDuration - animation final duration
 */
fun TextView.setTextWithTransition(text: String, animDuration: Long)
{
	val with = this.width
	val thisText = this
	val textLayoutParams = this.layoutParams
	ValueAnimator.ofInt(with, 0).apply {
		addUpdateListener { valueAnimator ->
			val value = valueAnimator.animatedValue as Int
			val layoutParams: ViewGroup.LayoutParams = textLayoutParams
			layoutParams.width = value
			thisText.layoutParams = layoutParams
		}
		doOnEnd {
			thisText.text = text
			thisText.measure(0, 0)
			ValueAnimator.ofInt(0, thisText.measuredWidth).apply {
				addUpdateListener { valueAnimator ->
					val value = valueAnimator.animatedValue as Int
					val layoutParams: ViewGroup.LayoutParams = textLayoutParams
					layoutParams.width = value
					thisText.layoutParams = layoutParams
				}
				duration = animDuration
				interpolator = AccelerateDecelerateInterpolator()
			}.start()
		}
		duration = duration
		interpolator = AccelerateDecelerateInterpolator()
	}.start()
}

fun TextView.setClickableSpan(text: String, clickListener: (View) -> Unit)
{
	if(textString.contains(text))
	{
		movementMethod = LinkMovementMethod.getInstance()
		setText(text.toClickSpan(
			IntRange(textString.indexOf(text), text.length), clickListener = clickListener
		), TextView.BufferType.SPANNABLE)
	}
}

@BindingAdapter("prettyCount")
fun TextView.setPrettyCount(value: Any?)
{
	text = value.prettyCount
}

@BindingAdapter("prettyChineseCount")
fun TextView.setPrettyChineseCount(value: Any?)
{
	text = value.prettyChineseCount
}

val Any?.prettyCount: String
	get()
	{
		try
		{
			val count = this?.toLongOr(null) ?: return "0"
			
			if (count < 1000)
				return count.toString()
			
			(ln(count.toDouble()) / ln(1000.0)).toInt().let {
				return String.format("%.1f%c", count / 1000.0.pow(it.toDouble()), "kMBTPE"[it - 1])
			}
		}
		catch(e: Exception)
		{
			e.printStackTrace()
			return "0"
		}
	}

val Any?.prettyChineseCount: String
	get() = if(this == null || this.toIntOr(null) == null)
		"0"
	else try
	{
		val sb = StringBuffer()
		
		val b0 = BigDecimal("1000")
		val b1 = BigDecimal("10000")
		val b2 = BigDecimal("100000000")
		val b3 = BigDecimal(this.toString())
		
		var formattedNum = "" //输出结果
		var unit = "" //单位
		
		if(b3.toString().length < b0.toString().length)
		{
			sb.append(b3.toString())
		}
		else if(b3.toString().length >= b0.toString().length && b3.toString().length < b1.toString().length)
		{
			formattedNum = b3.divide(b0).toString()
			unit = "千"
		}
		else if(b3.toString().length >= b1.toString().length && b3.toString().length < b2.toString().length)
		{
			formattedNum = b3.divide(b1).toString()
			unit = "万"
		}
		else if(b3.toString().length >= b2.toString().length)
		{
			formattedNum = b3.divide(b2).toString()
			unit = "亿"
		}
		
		if(formattedNum.isNotBlank())
		{
			var i = formattedNum.indexOf(".")
			
			if(i == -1)
			{
				sb.append(formattedNum).append(unit)
			}
			else
			{
				i++
				val v = formattedNum.substring(i, i + 1)
				
				if(v != "0")
					sb.append(formattedNum.substring(0, i + 1)).append(unit)
				else
					sb.append(formattedNum.substring(0, i - 1)).append(unit)
			}
		}
		
		if(sb.isBlank())
			"0"
		else
			sb.toString()
	}
	catch(e: Exception)
	{
		e.printStackTrace()
		"0"
	}