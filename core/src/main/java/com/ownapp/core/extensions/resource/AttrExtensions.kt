package com.ownapp.core.extensions.resource

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import java.lang.Exception

/**
 * Updated by Robin on 2020/12/4
 */

fun Context.getAttrString(@AttrRes attrId: Int): String?
{
	obtainStyledAttributes(TypedValue().data, intArrayOf(attrId)).let {
		return try { it.getString(0) }
		catch(e: Exception) { null }
		finally { it.recycle() }
	}
}

fun Context.getAttrBoolean(@AttrRes attrId: Int): Boolean
{
	TypedValue().let {
		theme.resolveAttribute(attrId, it, true)
		return it.data != -1
	}
}

fun Context.getAttrDimen(@AttrRes attrId: Int): Int
{
	obtainStyledAttributes(TypedValue().data, intArrayOf(attrId)).let {
		return try
		{
			it.getDimensionPixelSize(0, 0)
		}
		finally
		{
			it.recycle()
		}
	}
}

fun Context.getAttrDrawable(@AttrRes attrId: Int): Drawable?
{
	TypedValue().let {
		theme.resolveAttribute(attrId, it, true)
		return ContextCompat.getDrawable(this, it.resourceId)
	}
}

@ColorInt
fun Context.getAttrColor(@AttrRes attrId: Int): Int
{
	obtainStyledAttributes(TypedValue().data, intArrayOf(attrId)).run {
		return getColor(0, 0).also { recycle() }
	}
}

fun Context.getAttrColorStateList(@AttrRes attrId: Int, state: IntArray? = intArrayOf(android.R.attr.state_enabled)): ColorStateList
{
	obtainStyledAttributes(null, intArrayOf(attrId)).run {
		return try
		{
			if(state == null)
				getColorStateList(0) ?: ColorStateList.valueOf(Color.TRANSPARENT)
			else
			{
				getColorStateList(0)?.let {
					ColorStateList.valueOf(it.getColorForState(state, it.defaultColor))
				} ?: ColorStateList.valueOf(Color.TRANSPARENT)
			}
		}
		finally
		{
			recycle()
		}
	}
}

fun Context.getStyleableColorStateList(attributes: TypedArray, @StyleableRes index: Int): ColorStateList?
{
	if(attributes.hasValue(index))
	{
		attributes.getResourceId(index, 0).let {
			if (it != 0)
			{
				AppCompatResources.getColorStateList(this, it)?.run {
					return this
				}
			}
		}
	}

	return attributes.getColorStateList(index)
}