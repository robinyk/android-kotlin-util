package com.ownapp.core.extensions.utility

import android.content.Context
import androidx.preference.PreferenceManager

/**
 * Updated by Robin on 2020/12/4
 */

private val Context.sharedPreferences
//	get() = getSharedPreferences("2Nm6lKiDKa", Context.MODE_PRIVATE)
	get() = PreferenceManager.getDefaultSharedPreferences(this)

fun <T: Any> Context.save(key: String, value: T?)
{
	sharedPreferences.edit().apply {
		when(value)
		{
			is Int -> putInt(key, value)
			is Long -> putLong(key, value)
			is Float -> putFloat(key, value)
			is Boolean -> putBoolean(key, value)
			is String -> putString(key, value)
			else -> putString(key, value.toString())
		}
	}.apply()
}

@Suppress("UNCHECKED_CAST")
fun <T: Any> Context.load(key: String, defaultValue: T? = null): T? = sharedPreferences.let {
	when(defaultValue)
	{
		is Boolean -> it.getBoolean(key, defaultValue) as? T ?: defaultValue
		is Int -> it.getInt(key, defaultValue) as? T ?: defaultValue
		is Float -> it.getFloat(key, defaultValue) as? T? ?: defaultValue
		is Long -> it.getLong(key, defaultValue) as? T? ?: defaultValue
		is String -> it.getString(key, defaultValue) as? T? ?: defaultValue
		else -> it.getString(key, defaultValue.toString()) as? T? ?: defaultValue
	}
}

@Deprecated(message = "Use save instead", replaceWith = ReplaceWith("save"))
fun Context.saveString(key: String, value: Any? = "") = sharedPreferences
	.edit()
	.putString(key, value.toString())
	.apply()

@Deprecated(message = "Use load instead", replaceWith = ReplaceWith("load"))
fun Context.loadString(key: String): String = sharedPreferences
	.getString(key, "").orEmpty()