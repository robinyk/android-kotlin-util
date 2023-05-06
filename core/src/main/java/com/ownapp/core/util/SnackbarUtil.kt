package com.ownapp.core.util

import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import com.google.android.material.snackbar.BaseTransientBottomBar.Duration
import com.google.android.material.snackbar.Snackbar
import com.ownapp.core.extensions.resource.toString

/**
 * Updated by Robin on 2020/12/4
 */

object SnackbarUtil
{
    var snackbar: Snackbar? = null
        private set
    var isShown: Boolean = false
        get() = snackbar?.isShown ?: false
        private set

    private val Snackbar.textView: TextView?
        get() = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView?

    fun build(view: View, @StringRes stringRes: Int, @Duration duration: Int = Snackbar.LENGTH_LONG): Snackbar?
    {
        return build(view, stringRes.toString(view.context), duration)
    }

    fun build(view: View, text: String?, @Duration duration: Int = Snackbar.LENGTH_LONG): Snackbar?
    {
        if (isShown)
            snackbar?.dismiss()

        snackbar = Snackbar.make(view, text.toString(), duration)
            .setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE)

        snackbar?.textView?.maxLines = 5

        return snackbar
    }

    fun show(view: View, @StringRes stringRes: Int, @Duration duration: Int = Snackbar.LENGTH_LONG): Snackbar?
    {
        return show(view, stringRes.toString(view.context), duration)
    }

    fun show(view: View, text: String?, @Duration duration: Int = Snackbar.LENGTH_LONG): Snackbar?
    {
        return build(view, text, duration)?.apply { show() }
    }
}