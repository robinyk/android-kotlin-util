package com.ownapp.blacksmith

import android.content.Context
import android.os.Looper
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import com.ownapp.blacksmith.Forger.forge
import com.ownapp.blacksmith.Forger.unforge
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Keep
interface SafeValue<T: Any>
{
	val safeValue: T
}

@Keep
abstract class CrateValue<T: Any>
{
	// protected abstract var holder: T?
	abstract var value: T?
	
	override operator fun equals(other: Any?): Boolean = this.value == other
	override fun hashCode(): Int = value?.hashCode() ?: 0
	
	open fun clear()
	{
		value = null
	}
}

@Keep
sealed class Crate<T: Any>(protected val context: Context, protected val key: String, protected val default: T?): CrateValue<T>()
{
	// override var holder: T? = key.load(default)
	
	override var value: T? = context.load(key, default)
		get() = context.load(key, default)
		set(value)
		{
			if(field != value)
			{
				field = value
				context.save(key, value)
			}
		}
	
	operator fun getValue(thisRef: Any?, property: KProperty<*>): T? = value
	
	operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?)
	{
		this.value = value
	}
	
	override fun toString(): String = value.toString()
}

@Keep
class IntCrate(context: Context, key: String, default: Int = 0): Crate<Int>(context, key, default), SafeValue<Int>, Comparable<Int>
{
	override val safeValue: Int
		get() = value ?: default ?: -1
	
	override fun compareTo(other: Int): Int = value?.compareTo(other) ?: 0
	
	operator fun inc(): IntCrate
	{
		return if(value != null)
			this.apply { value!!.inc() }
		else this
	}
	
	operator fun dec(): IntCrate
	{
		return if(value != null)
			this.apply { value!!.dec() }
		else this
	}
}

@Keep
class LongCrate(context: Context, key: String, default: Long = 0L): Crate<Long>(context, key, default), SafeValue<Long>, Comparable<Long>
{
	override val safeValue: Long
		get() = value ?: default ?: -1L

	override fun compareTo(other: Long): Int = value?.compareTo(other) ?: 0

	operator fun inc(): LongCrate
	{
		return if(value != null)
			this.apply { value!!.inc() }
		else this
	}

	operator fun dec(): LongCrate
	{
		return if(value != null)
			this.apply { value!!.dec() }
		else this
	}
}

@Keep
class BooleanCrate(context: Context, key: String, default: Boolean? = null): Crate<Boolean>(context, key, default), SafeValue<Boolean>
{
	override val safeValue: Boolean
		get() = value == true
}

@Keep
open class StringCrate(context: Context, key: String, default: String? = null): Crate<String>(context, key, default), SafeValue<String>
{
	override val safeValue: String
		get() = value.orEmpty()
}

@Keep
open class ForgedCrate(context: Context, key: String): StringCrate(context, key, "")
{
	override var value: String? = context.load(key, default).unforge()
		set(value)
		{
			if(field != value)
			{
				field = value
				context.save(key, value.forge())
			}
		}
}

@Keep
class TokenCrate(context: Context, key: String): ForgedCrate(context, key)
{
	override var value: String? = context.load(key, "").unforge()
		set(value)
		{
			if(field != value)
				field = value
		}
	
	// fun load() { value = context.load(key, "").unforge() }
	fun save(newValue: String? = value)
	{
		value = newValue.orEmpty()
		context.save(key, value.forge())
	}
	
	override fun clear() = save("")
}