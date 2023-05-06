package com.ownapp.core.support.json

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.ownapp.core.extensions.utility.toBoolean
import java.io.IOException
import java.lang.reflect.ParameterizedType

class LiveDataTypeAdapterFactory: TypeAdapterFactory
{
	@Suppress("UNCHECKED_CAST")
	override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>?
	{
		if(type.rawType != LiveData::class.java && type.rawType != MutableLiveData::class.java)
			return null
		
		// Assumes that LiveData is never used as raw type but is always parameterized
		val valueType = (type.type as ParameterizedType).actualTypeArguments[0]
		
		// Get the adapter for the LiveData value type `T`
		// Cast TypeAdapter to simplify adapter code below
		val valueAdapter = gson.getAdapter(TypeToken.get(valueType)) as TypeAdapter<T>
		
		// Is safe due to `type` check at start of method
		return object: TypeAdapter<LiveData<T>>()
		{
			@Throws(IOException::class)
			override fun write(out: JsonWriter?, liveData: LiveData<T>)
			{
				// Directly write out LiveData value
				valueAdapter.write(out, liveData.value)
			}
			
			@Throws(IOException::class)
			override fun read(`in`: JsonReader?): LiveData<T>
			{
				return MutableLiveData(if(valueType == Boolean::class.javaObjectType)
					// Get String instead if want to retrieve false-boolean value because
					// reading String will always success, if we read Int here will get error
					(gson.getAdapter(object: TypeToken<String>(){})).read(`in`).toBoolean() as T
				else valueAdapter.read(`in`)
				)
			}
		} as TypeAdapter<T>
	}
}
