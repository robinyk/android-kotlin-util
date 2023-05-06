package com.ownapp.core.support.network.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
open class OwnappLegacyResponse
{
    @SerializedName("result") val resultMessage: String? = null
    @SerializedName("data") open val `data`: Data? = null
    
    @Keep
    open class Data
    {
        @SerializedName("Status") open val status: Int? = null
        @SerializedName("Message") open val message: String? = null
        @SerializedName("total_page") val totalPage: Int = 0
    }
}