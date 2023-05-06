package com.ownapp.core.util.helper

import android.app.Activity
import android.content.Context
import android.text.Layout
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AlignmentSpan
import android.widget.Toast
import androidx.annotation.IntDef
import androidx.annotation.StringRes
import com.ownapp.core.extensions.utility.debug
import com.ownapp.core.extensions.utility.log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


/**
 * Updated by Robin on 2020/12/4
 */

object Toaster
{
    //**--------------------------------------------------------------------------------------------------
    //*      Enum
    //---------------------------------------------------------------------------------------------------*/
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(Duration.SHORT, Duration.LONG)
    annotation class Duration
    {
        companion object
        {
            const val SHORT = Toast.LENGTH_SHORT
            const val LONG = Toast.LENGTH_LONG
        }
    }
    
    
    //**--------------------------------------------------------------------------------------------------
    //*      Variable
    //---------------------------------------------------------------------------------------------------*/
    // Static
    private var contextWeakReference: WeakReference<Context>? = null
    private var mToast: Toast? = null


    //**--------------------------------------------------------------------------------------------------
    //*      Caller
    //---------------------------------------------------------------------------------------------------*/
    fun show(context: Context, @StringRes stringRes: Int, @Duration duration: Int = Duration.SHORT)
    {
        show(context, context.getString(stringRes), duration)
    }

    fun show(context: Context, text: Any?, @Duration duration: Int = Duration.SHORT)
    {
        if(text.toString().isNotBlank())
            showText(context, text.toString(), duration)
    }

    
    //**--------------------------------------------------------------------------------------------------
    //*      Private
    //---------------------------------------------------------------------------------------------------*/
    private fun showText(context: Context, text: String, @Duration length: Int)
    {
        GlobalScope.launch(Dispatchers.Main) {
            val lastContext = contextWeakReference?.get()

            if (lastContext == null || lastContext !== context)
            {
                contextWeakReference = WeakReference(context)
                mToast = Toast(contextWeakReference?.get())
            }

            mToast?.cancel()

            mToast = Toast.makeText(contextWeakReference?.get()?.applicationContext
                , SpannableString(text).apply {
                    setSpan(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0,
                        text.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
                }, length)

            ("Toast" to text).debug()

            (context as? Activity)?.let {
                it.runOnUiThread { mToast?.show() }
            } ?: mToast?.show()
        }
    }
}