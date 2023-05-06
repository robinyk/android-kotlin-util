package com.ownapp.core.extensions.resource

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.core.graphics.ColorUtils
import com.ownapp.core.R
import java.util.*
import kotlin.math.roundToInt

/**
 * Updated by Robin on 2020/12/4
 */

/**
 * Generates a random opaque color
 * Note that this is mainly for testing
 * Should you require this method often, consider
 * rewriting the method and storing the [Random] instance
 * rather than generating one each time
 */
inline val randomColor: Int
	get() = Color.rgb(Random().nextInt(256), Random().nextInt(256), Random().nextInt(256))

/**
 * @receiver @receiver:ColorInt Int
 * @return Boolean
 */
val @receiver:ColorInt Int.isDark: Boolean
	get() = ColorUtils.calculateLuminance(this) < 0.5

fun @receiver:ColorInt Int.isColorDark(minDarkness: Float = 0.5f): Boolean = ColorUtils.calculateLuminance(this) < minDarkness

@ColorInt
private inline fun Int.colorFactor(rgbFactor: (Int) -> Float): Int {
	val (red, green, blue) = intArrayOf(Color.red(this), Color.green(this), Color.blue(this))
		.map { rgbFactor(it).toInt() }
	return Color.argb(Color.alpha(this), red, green, blue)
}

@ColorInt
fun Int.adjustAlpha(factor: Float): Int =
	Color.argb((Color.alpha(this) * factor).roundToInt()
		, Color.red(this), Color.green(this), Color.blue(this))

@ColorInt
fun Int.lighten(@FloatRange(from = 0.0, to = 1.0) factor: Float = 0.1f): Int = colorFactor {
	(it * (1f - factor) + 255f * factor)
}

@ColorInt
fun Int.darken(@FloatRange(from = 0.0, to = 1.0) factor: Float = 0.1f): Int = colorFactor {
	it * (1f - factor)
}

@ColorInt
fun Int.colorToBackground(@FloatRange(from = 0.0, to = 1.0) factor: Float = 0.1f): Int =
	if (isDark) darken(factor) else lighten(factor)

@ColorInt
fun Int.colorToForeground(@FloatRange(from = 0.0, to = 1.0) factor: Float = 0.1f): Int =
	if (isDark) lighten(factor) else darken(factor)

/**
 * Method to get Title text color for an app,
 * Just pass the color and it will calculate new color according to
 * provided color
 *
 * @param color pass the int color, i.e., you can send it from color resource file.
 * @return int color
 */
@ColorInt
fun @receiver:ColorInt Int.getTitleTextColor(): Int
{
	val darkness = 1 - (0.299 * Color.red(this) + 0.587 * Color.green(this) + 0.114 * Color.blue(this)) / 255
	return if (darkness < 0.35)
		getDarkerColor(0.25f)
	else Color.WHITE
}

/**
 * Method to get body text color for an app,
 * Just pass the color and it will calculate new color according to
 * provided color
 *
 * @receiver [ColorInt]
 * @return [ColorInt]
 */
@ColorInt
fun @receiver:ColorInt Int.getBodyTextColor(): Int = getTitleTextColor().adjustAlpha(0.7f)

/**
 * Method to get Darker color from provided color
 *
 * @receiver [ColorInt]
 * @param transparency set transparency between 0.0f to 1.0f
 * @return [ColorInt]
 */
@ColorInt
fun @receiver:ColorInt Int.getDarkerColor(@FloatRange(from = 0.0, to = 1.0) transparency: Float): Int
{
	FloatArray(3).let { hsv ->
		Color.colorToHSV(this, hsv)
		hsv[2] *= transparency
		return Color.HSVToColor(hsv)
	}
}

fun Context.getColors(@ColorInt color: Int, @ColorInt disabledColor: Int = getAttrColor(R.attr.colorSurface)): ColorStateList
{
	return ColorStateList(
		arrayOf(
			intArrayOf(android.R.attr.state_enabled)
			, intArrayOf(-android.R.attr.state_enabled)
		),
		intArrayOf(color, disabledColor.adjustAlpha(0.12f))
	)
}

fun Context.getTextColors(@ColorInt color: Int, @ColorInt disabledColor: Int = getAttrColor(R.attr.colorOnSurface)): ColorStateList
{
	return ColorStateList(
		arrayOf(
			intArrayOf(android.R.attr.state_enabled)
			, intArrayOf()
		),
		intArrayOf(color, disabledColor.adjustAlpha(0.38f))
	)
}

fun Context.getSelectableColors(@ColorInt selectedColor: Int, @ColorInt defaultColor: Int = getAttrColor(R.attr.colorSurface)): ColorStateList
{
	return ColorStateList(
		arrayOf(
			intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected)
			, intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
			, intArrayOf(android.R.attr.state_enabled)
			, intArrayOf()
		),
		intArrayOf(selectedColor, selectedColor, defaultColor, defaultColor.adjustAlpha(0.12f))
	)
}

fun Context.getSelectableTextColors(@ColorInt color: Int, @ColorInt disabledColor: Int = getAttrColor(R.attr.colorOnSurface)): ColorStateList
{
	return ColorStateList(
		arrayOf(
			intArrayOf(android.R.attr.state_enabled, android.R.attr.state_selected)
			, intArrayOf(android.R.attr.state_enabled, android.R.attr.state_checked)
			, intArrayOf(android.R.attr.state_enabled)
			, intArrayOf()
		),
		intArrayOf(color, color, color.adjustAlpha(0.87f), disabledColor.adjustAlpha(0.33f))
	)
}