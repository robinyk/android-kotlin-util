package com.ownapp.core.support.network


import com.google.gson.Gson
import com.ownapp.core.extensions.utility.fromJson
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.logException
import retrofit2.Response

/**
 * Updated by Robin on 2021/2/2
 */

inline fun <reified T> safeResponse(call: () -> Response<T?>?): T?
{
    try
    {
        call.invoke()?.let {
            if(it.isSuccessful)
            {
                return it.body().apply {
                    if(this is HttpResponseCode)
                        code = it.code()
                }
            }
            else if(it.errorBody() != null)
            {
                return Gson().fromJson<T?>(it.errorBody()?.charStream()).apply {
                    if(this is HttpResponseCode)
                        code = it.code()
                }
            }
        } ?: "Retrofit response is null: ${T::class.simpleName}".logError()
    }
    catch(e: Exception)
    {
        e.logException()
    }

    return null
}