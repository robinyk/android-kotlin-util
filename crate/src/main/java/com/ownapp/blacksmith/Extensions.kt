package com.ownapp.blacksmith

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.Keep
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ownapp.blacksmith.Forger.forge

private lateinit var sharedPreferences: SharedPreferences

private val spec = KeyGenParameterSpec.Builder(
		MasterKey.DEFAULT_MASTER_KEY_ALIAS
		, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
	)
	.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
	.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
	.setKeySize(256)
	.build()

private fun getEncryptedSharedPreferences(context: Context): SharedPreferences
{
	return EncryptedSharedPreferences.create(
		context,
		context.packageName.forge(),
		MasterKey.Builder(context).setKeyGenParameterSpec(spec).build(),
		EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
		EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
	)
}

@Keep
fun <T: Any> Context.save(key: String, value: T?)
{
	if(!::sharedPreferences.isInitialized)
		sharedPreferences = getEncryptedSharedPreferences(this)

	sharedPreferences.edit {
		when(value)
		{
			is Long ->  putLong(key, value)
			is Int -> putInt(key, value)
			is Float ->  putFloat(key, value)
			is Boolean -> putBoolean(key, value)
			is String ->  putString(key, value)
			else ->  putString(key, value.toString())
		}
	}
}

@Keep
@Suppress("UNCHECKED_CAST")
fun <T: Any> Context.load(key: String, defaultValue: T? = null): T?
{
	if(!::sharedPreferences.isInitialized)
		sharedPreferences = getEncryptedSharedPreferences(this)

	with(sharedPreferences) {
		return when(defaultValue)
		{
			is Long -> getLong(key, defaultValue)
			is Int -> getInt(key, defaultValue)
			is Float -> getFloat(key, defaultValue)
			is Boolean -> getBoolean(key, defaultValue)
			is String -> getString(key, defaultValue)
			else -> getString(key, defaultValue.toString())
		}  as? T? ?: defaultValue
	}
}