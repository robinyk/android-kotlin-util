package com.ownapp.core.util

import android.text.TextUtils
import androidx.annotation.AttrRes
import com.ownapp.core.extensions.resource.isDecimalNumber
import com.ownapp.core.extensions.resource.isIntegerNumber
import com.ownapp.core.extensions.utility.commaFloatString
import com.ownapp.core.extensions.view.prettyChineseCount
import com.ownapp.core.extensions.view.prettyCount

/**
 * Updated by Robin on 2020/12/11
 */

object ValueUtil
{
    /**
     * Validate [Object] variable and return a non-null String
     *
     * @param obj [Object] to validate.
     * @return Non-null String value.
     */
    @JvmStatic
    fun getString(obj: Any?): String = if(obj != null) (obj as? String)?.toString() ?: obj.toString() else ""
    
    @JvmStatic
    fun toPrettyCount(obj: Any?): String = obj?.prettyCount ?: "0"
    
    @JvmStatic
    fun toPrettyChineseCount(obj: Any?): String = obj?.prettyChineseCount ?: "0"
    
    @JvmStatic
    fun toPrice(obj: Any?): String = toPrice(obj, null)
    
    @JvmStatic
    fun toPrice(obj: Any?, currency: String?): String = StringBuilder().run {
        if(!currency.isNullOrBlank())
            append("$currency ")
    
        append(obj?.commaFloatString ?: 0f.commaFloatString)
        toString()
    }
    
    @JvmStatic
    fun isEmpty(obj: Any?) = obj?.let { TextUtils.isEmpty(it.toString()) } ?: true

    @JvmStatic
    fun isNotEmpty(obj: Any?) = !isEmpty(obj)

    @JvmStatic
    fun isListEmpty(list: List<*>?) = list.isNullOrEmpty()

    @JvmStatic
    fun isInteger(obj: Any?) = obj.toString().isIntegerNumber

    @JvmStatic
    fun isDecimal(obj: Any?) = obj.toString().isDecimalNumber

    @JvmStatic
    fun isZero(obj: Any?) = when(obj)
    {
        is Int -> obj.toInt() == 0
        is Long -> obj.toLong() == 0L
        is Float -> obj.toFloat() == 0f
        is Double -> obj.toDouble() == 0.0
        else -> false
    }

    @JvmStatic
    fun isNotZero(obj: Any?) = !isZero(obj)

    @JvmStatic
    fun isZeroOrLess(obj: Any?) = when(obj)
    {
        is Int -> obj.toInt() <= 0
        is Long -> obj.toLong() <= 0
        is Float -> obj.toFloat() <= 0
        is Double -> obj.toDouble() <= 0
        else -> false
    }
}