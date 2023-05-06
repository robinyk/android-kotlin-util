package com.ownapp.core.extensions.resource

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.annotation.*
import androidx.core.content.ContextCompat
import com.ownapp.core.extensions.*
import com.ownapp.core.extensions.getEntryName
import com.ownapp.core.annotation.ResourceType
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.utility.logException
import java.lang.Integer.max
import kotlin.math.min

val Int.appendOrdinal: String
	get() = "$this" +
			if(this % 100 in 11..13)
				"th"
			else
				when(this % 10)
				{
					1 -> "st"
					2 -> "nd"
					3 -> "rd"
					else -> "th"
				}

fun @receiver:AnyRes Int.getEntryName(context: Context): String? = context.getEntryName(this)

fun Int.isResource(context: Context): Boolean
{
	try
	{
		return getResourceTypeName(context) != null
	}
	catch(e: Exception)
	{
		"Failed to get resource => ${e.message}".logError()
	}

	return false
}

@ResourceType
fun @receiver:AnyRes Int.getResourceTypeName(context: Context): String?
{
	try
	{
		return context.resources.getResourceTypeName(this)
	}
	catch(e: Exception)
	{
		"Failed to get resource => ${e.message}".logError()
	}

	return null
}

fun @receiver:StringRes Int.toString(context: Context): String
{
	if(getResourceTypeName(context) == ResourceType.STRING)
		return context.getString(this)

	"Invalid resources string => $this".logError()
	return ""
}

fun @receiver:ColorRes @receiver:AttrRes
Int.toColor(context: Context): Int
{
	return when(getResourceTypeName(context))
	{
		ResourceType.COLOR -> context.getColorCompat(this)
		ResourceType.ATTRIBUTE -> context.getAttrColor(this)
		else ->
		{
			"Failed to convert $this to color".logError()
			this
		}
	}
}

fun Int.toColorStateList(context: Context, state: IntArray? = intArrayOf(android.R.attr.state_enabled)): ColorStateList
{
	return when(getResourceTypeName(context))
	{
		ResourceType.COLOR -> ColorStateList.valueOf(context.getColorCompat(this))
		ResourceType.ATTRIBUTE ->  context.getAttrColorStateList(this, state)
		else ->
		{
			"Failed to convert $this to ColorStateList".logError()
			ColorStateList.valueOf(Color.TRANSPARENT)
		}
	}
}

fun @receiver:DimenRes @receiver:AttrRes
Int.toDimensionPixel(context: Context): Int?
{
	return when(getResourceTypeName(context))
	{
		ResourceType.DIMENSION -> context.resources.getDimensionPixelSize(this)
		ResourceType.ATTRIBUTE -> context.getAttrDimen(this)
		else ->
		{
			"Failed to convert {$this} to DP".logError()
			null
		}
	}
}

fun @receiver:DrawableRes @receiver:AttrRes
Int.toDrawable(context: Context): Drawable?
{
	return when(getResourceTypeName(context))
	{
		ResourceType.DRAWABLE -> ContextCompat.getDrawable(context, this)
		ResourceType.ATTRIBUTE -> context.getAttrDrawable(this)
		else ->
		{
			"Failed to convert {$this} to Drawable Type".logError()
			null
		}
	}
}

/**
 * Restricts [Int] to be within a [min] and a [max] value
 */
fun Int.clamp(min: Int, max: Int): Int = max(min, min(max, this))

fun Float.getPercentage(divideBy: Float): Float = this * 100 / divideBy
fun Float.getDiscountPercentage(divideBy: Float): Float = 100 - this.getPercentage(divideBy)