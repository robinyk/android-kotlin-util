package com.ownapp.core.support.json

import androidx.annotation.Keep
import androidx.lifecycle.MutableLiveData
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.ownapp.core.extensions.resource.lowerCased
import java.lang.reflect.Type

class BooleanLiveDataDeserializer: JsonDeserializer<MutableLiveData<Boolean>>
{
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): MutableLiveData<Boolean>
    {
        with(json.asJsonPrimitive) {
            return MutableLiveData(
                when
                {
                    isBoolean -> asBoolean
                    isNumber -> asNumber.toInt() == 1
                    isString -> HashSet(listOf("true", "1", "yes")).contains(asString.lowerCased)
                    else -> false
                }
            )
        }
    }
}