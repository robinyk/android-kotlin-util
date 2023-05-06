package com.ownapp.core.extensions.utility

import android.app.Activity
import android.os.Bundle
import android.os.Parcelable
import androidx.fragment.app.Fragment
import java.io.Serializable

/**
 * Updated by Robin on 2020/12/15
 */

inline fun <reified T : Parcelable> Bundle.putParcelableCollection(key: String, value: Collection<T>) = 
	putParcelableArray(key, value.toTypedArray())

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Parcelable> Bundle.getParcelableMutableList(key: String): MutableList<T> = 
	(getParcelableArray(key) as Array<T>).toMutableList()

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Parcelable> Bundle.getParcelableMutableSet(key: String): MutableSet<T> = 
	(getParcelableArray(key) as Array<T>).toMutableSet()

///**
// * Saves all bundle args based on their respective types.
// *
// */
//fun bundleOf(vararg parameters: Pair<String, Any?>): Bundle
//{
//	return Bundle().apply {
//		parameters.asSequence().forEach {
//			it.second.let { value ->
//				when(value)
//				{
//					null -> putSerializable(it.first, null)
//					is Boolean -> putBoolean(it.first, value)
//					is Byte -> putByte(it.first, value)
//					is Char -> putChar(it.first, value)
//					is Short -> putShort(it.first, value)
//					is Int -> putInt(it.first, value)
//					is Long -> putLong(it.first, value)
//					is Float -> putFloat(it.first, value)
//					is Double -> putDouble(it.first, value)
//					is String -> putString(it.first, value)
//					is CharSequence -> putCharSequence(it.first, value)
//					is Parcelable -> putParcelable(it.first, value)
//					is Serializable -> putSerializable(it.first, value)
//					is BooleanArray -> putBooleanArray(it.first, value)
//					is ByteArray -> putByteArray(it.first, value)
//					is CharArray -> putCharArray(it.first, value)
//					is DoubleArray -> putDoubleArray(it.first, value)
//					is FloatArray -> putFloatArray(it.first, value)
//					is IntArray -> putIntArray(it.first, value)
//					is LongArray -> putLongArray(it.first, value)
//					is Array<*> ->
//					{
//						@Suppress("UNCHECKED_CAST")
//						when
//						{
//							value.isArrayOf<Parcelable>() -> putParcelableArray(it.first, value as Array<out Parcelable>)
//							value.isArrayOf<CharSequence>() -> putCharSequenceArray(it.first, value as Array<out CharSequence>)
//							value.isArrayOf<String>() -> putStringArray(it.first, value as Array<out String>)
//							else -> "Unsupported bundle component (${value.javaClass})".logError()
//						}
//					}
//					is ShortArray -> putShortArray(it.first, value)
//					is Bundle -> putBundle(it.first, value)
//					else -> "Unsupported bundle component (${value.javaClass})".logError()
//				}
//			}
//		}
//	}
//}

inline fun <reified T: Any> Activity.extra(key: String, default: T? = null, crossinline block: (T?) -> Unit = {}) = lazy {
	intent?.extras?.get(key).let {
		(if(it is T) it else default).run {
			block(this)
			this
		}
	}
}

inline fun <reified T : Any> Fragment.extra(key: String, default: T? = null, crossinline block: (T?) -> Unit = {}) = lazy {
	arguments?.get(key).let {
		(if(it is T) it else default).run {
			block(this)
			this
		}
	}
}

inline fun <reified T : Any> Activity.safeExtra(key: String, default: T, crossinline block: (T?) -> Unit = {}): Lazy<T> = lazy {
	intent?.extras?.get(key).let {
		(if(it is T) it else default).run {
			block(this)
			this
		}
	}
}

inline fun <reified T : Any> Fragment.safeExtra(key: String, default: T, crossinline block: (T?) -> Unit = {}): Lazy<T> = lazy {
	arguments?.get(key).let {
		(if(it is T) it else default).run {
			block(this)
			this
		}
	}
}