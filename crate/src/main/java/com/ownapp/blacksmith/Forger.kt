package com.ownapp.blacksmith

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Base64
import androidx.annotation.Keep
import timber.log.Timber

@Keep
object Forger
{
	init { System.loadLibrary("forger_ndk") }


	/**
	 * Our hook to the JNI hiding method.
	 * @receiver Text to hide (XOR key is hard-coded in the JNI app)
	 * @return A [Base64.encode] encoded value of (plainText XOR key)
	 */
	@Keep
	external fun String?.mold(): String

	/**
	 * Our hook to the JNI hiding method.
	 * @receiver [Base64]-encoded text to unhide (XOR key is hard-coded in the JNI app)
	 * @return A string with the original plaintext (cipherText XOR key)
	 */
	@Keep external fun String?.unmold(): String

	/**
	 * A more complicated way to do hiding and support error fallback for [mold]. Use this
	 * if [mold] result string doesn't hide completely.
	 * @receiver Text to hide (XOR key is hard-coded in the JNI app)
	 * @return A [Base64.encode] encoded value of (plainText XOR key)
	 */
	@Keep external fun String?.forge(): String

	/**
	 * A more complicated wayunmo to do unhiding and support error fallback for [unmold]. Use this
	 * if [unmold] result string doesn't unhide completely.
	 * @receiver [Base64]-encoded text to unhide (XOR key is hard-coded in the JNI app)
	 * @return A string with the original plaintext (cipherText XOR key)
	 */
	@Keep external fun String?.unforge(): String

	/**
	 * Log text and hidden text for checking.
	 * @receiver Context
	 * @param text Text to log
	 */
	@Keep
	fun Context.dismantle(text: String?)
	{
		if(applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0)
		{
			text.forge().let {
				debug("Forger begin to dismantle"
					, "Before" to text
					, "Forged" to it
					, "Check " to "$text == ${it.unforge()}"
				)
			}
		}
	}

	/**
	 * Fallback utility function for legacy hiding method, do not use this anymore
	 * @receiver [Base64]-encoded text to unhide (XOR key is hard-coded in the JNI app)
	 * @param [strings] texts to unhide (XOR key is hard-coded in the JNI app)
	 * @return A string with the original plaintext (cipherText XOR key)
	 */
	@Keep
	@Deprecated("Use forge() and unforge() instead")
	fun String?.reforge(vararg strings: String?): String
	{
		StringBuilder(this.unmold()).let {
			strings.forEach { text -> it.append(text.unmold()) }
			return it.toString()
		}
	}
	
	private fun debug(tag: String, vararg pairs: Pair<String?, Any?>) = with(java.lang.StringBuilder()) {
		appendLine(tag)
		pairs.forEach { appendLine("${it.first} => ${it.second.toString()}") }
		Timber.d(toString())
	}
}