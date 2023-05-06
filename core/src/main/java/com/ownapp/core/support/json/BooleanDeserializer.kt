package com.ownapp.core.support.json

import androidx.annotation.Keep
import com.google.gson.*
import com.ownapp.core.extensions.resource.lowerCased
import java.lang.reflect.Type

/**
 * Updated by Robin on 2021/1/15
 */

class BooleanDeserializer: JsonSerializer<Boolean>, JsonDeserializer<Boolean>
{
    override fun serialize(src: Boolean?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement
    {
        return JsonPrimitive(src == true)
    }
    
    @Throws(JsonParseException::class)
    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext?): Boolean
    {
        with(json.asJsonPrimitive) {
            return when
            {
                isBoolean -> asBoolean
                isNumber -> asNumber.toInt() == 1
                isString -> HashSet(listOf("true", "1", "yes")).contains(asString.lowerCased)
                else -> false
            }
        }
    }
}