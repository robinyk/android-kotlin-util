// package com.ownapp.blacksmith
//
// import android.content.Context
// import android.content.SharedPreferences
// import android.security.keystore.KeyGenParameterSpec
// import android.security.keystore.KeyProperties
// import androidx.annotation.Keep
// import androidx.security.crypto.EncryptedSharedPreferences
// import androidx.security.crypto.MasterKey
// import com.ownapp.blacksmith.Forger.forge
// import com.ownapp.core.extensions.utility.log
// import com.ownapp.core.extensions.utility.logError
// import dagger.Module
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Inject
// import javax.inject.Singleton
//
// // @Module
// // @InstallIn(SingletonComponent::class)
// @Singleton
// class ForgedPreference @Inject constructor(@ApplicationContext context: Context)
// {
// 	// @Inject @ApplicationContext lateinit var context: Context
// 	protected val ps by lazy { getEncryptedSharedPreferences(context) }
//
// 	// init
// 	// {
// 	// 	"asdhjaslkdasjlk".log()
// 	// }
//
// 	private val spec = KeyGenParameterSpec.Builder(
// 			MasterKey.DEFAULT_MASTER_KEY_ALIAS
// 			, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
// 		)
// 		.setBlockModes(KeyProperties.BLOCK_MODE_GCM)
// 		.setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
// 		.setKeySize(256)
// 		.build()
//
// 	private val masterKey = MasterKey.Builder(context).setKeyGenParameterSpec(spec).build()
//
// 	private fun getEncryptedSharedPreferences(context: Context): SharedPreferences
// 	{
// 		return EncryptedSharedPreferences.create(
// 			context,
// 			context.packageName.forge(),
// 			masterKey,
// 			EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
// 			EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
// 		)
// 	}
//
// 	fun <T: Any> save(key: String, value: T?)
// 	{
// 		// if(!::sharedPreferences.isInitialized)
// 		// Companion.sharedPreferences = this.ps
//
// 		ps.edit().let {
// 			when(value)
// 			{
// 				is Int -> it.putInt(key, value)
// 				is Long ->  it.putLong(key, value)
// 				is Float ->  it.putFloat(key, value)
// 				is Boolean ->  it.putBoolean(key, value)
// 				is String ->  it.putString(key, value)
// 				else ->  it.putString(key, value.toString())
// 			}
//
// 			it.apply()
// 		}
// 	}
//
// 	@Suppress("UNCHECKED_CAST")
// 	fun <T: Any> load(key: String, defaultValue: T? = null): T?
// 	{
// 		// if(!::sharedPreferences.isInitialized)
// 		// 	sharedPreferences = getEncryptedSharedPreferences(context)
//
// 		ps.let {
// 			return when(defaultValue)
// 			{
// 				is Boolean -> it.getBoolean(key, defaultValue) as? T ?: defaultValue
// 				is Int -> it.getInt(key, defaultValue) as? T ?: defaultValue
// 				is Float -> it.getFloat(key, defaultValue) as? T? ?: defaultValue
// 				is Long -> it.getLong(key, defaultValue) as? T? ?: defaultValue
// 				is String -> it.getString(key, defaultValue) as? T? ?: defaultValue
// 				else -> it.getString(key, defaultValue.toString()) as? T? ?: defaultValue
// 			}
// 		}
// 	}
//
// 	companion object
// 	{
// 		private lateinit var sharedPreferences: SharedPreferences
//
// 		@Keep
// 		fun <T: Any> String.save(value: T?)
// 		{
// 			// if(!::sharedPreferences.isInitialized)
// 				// Companion.sharedPreferences = this.ps
//
// 			sharedPreferences.edit().let {
// 				when(value)
// 				{
// 					is Int -> it.putInt(this, value)
// 					is Long ->  it.putLong(this, value)
// 					is Float ->  it.putFloat(this, value)
// 					is Boolean ->  it.putBoolean(this, value)
// 					is String ->  it.putString(this, value)
// 					else ->  it.putString(this, value.toString())
// 				}
//
// 				it.apply()
// 			}
// 		}
//
// 		@Keep
// 		@Suppress("UNCHECKED_CAST")
// 		fun <T: Any> String.load(defaultValue: T? = null): T?
// 		{
// 			// if(!::sharedPreferences.isInitialized)
// 			// 	sharedPreferences = getEncryptedSharedPreferences(context)
//
// 			sharedPreferences.let {
// 				return when(defaultValue)
// 				{
// 					is Boolean -> it.getBoolean(this, defaultValue) as? T ?: defaultValue
// 					is Int -> it.getInt(this, defaultValue) as? T ?: defaultValue
// 					is Float -> it.getFloat(this, defaultValue) as? T? ?: defaultValue
// 					is Long -> it.getLong(this, defaultValue) as? T? ?: defaultValue
// 					is String -> it.getString(this, defaultValue) as? T? ?: defaultValue
// 					else -> it.getString(this, defaultValue.toString()) as? T? ?: defaultValue
// 				}
// 			}
// 		}
// 	}
// }