package com.ownapp.core.support.app

import android.content.Context

interface Shareable
{
	val shareTitle: String?
	val shareUrl: String?

	fun getShareMessage(context: Context): String
}