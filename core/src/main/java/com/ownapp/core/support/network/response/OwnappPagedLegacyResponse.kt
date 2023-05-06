package com.ownapp.core.support.network.response

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
open class OwnappPagedLegacyResponse<T>: OwnappLegacyResponse()
{
    @SerializedName("resultset") val list: List<T>? = null
    val result: T?
        get() = list?.firstOrNull()
}