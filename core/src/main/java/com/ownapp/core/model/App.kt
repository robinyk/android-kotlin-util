package com.ownapp.core.model

/**
 * Created by Robin on 29/3/2021.
 */


sealed class App(val packageName: String)
object Facebook: App("com.facebook.katana")
object Twitter: App("com.twitter.android")
object Instagram: App("com.instagram.android")
object Youtube: App("com.google.android.youtube")
object Whatsapp: App("com.whatsapp")

object Wechat: App("com.tencent.mm")
{
	const val moment = "com.tencent.mm.ui.tools.ShareToTimeLineUI"
}
