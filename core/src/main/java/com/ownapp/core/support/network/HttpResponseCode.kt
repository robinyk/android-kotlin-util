package com.ownapp.core.support.network

import androidx.room.Ignore
import com.fasterxml.jackson.annotation.JsonIgnore

/**
 * Updated by Robin on 2021/2/2
 */

interface HttpResponseCode
{
	@get:Ignore @set:Ignore
	@get:JsonIgnore @set:JsonIgnore
	var code: Int
}
