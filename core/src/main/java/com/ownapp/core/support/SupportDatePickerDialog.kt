package com.ownapp.core.support

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.ownapp.core.R

/**
 * Updated by Robin on 2020/12/4
 */

/**
 * Support [DatePickerDialog] for working around Samsung 5 [java.util.IllegalFormatConversionException] bug.
 * <p>
 * > Fatal Exception: java.util.IllegalFormatConversionException: %d can't format java.lang.String arguments
 * <p>
 * Created by Tobias Schürg
 * Based on http://stackoverflow.com/a/31855744/570168
 */
class SupportDatePickerDialog: DatePickerDialog
{
    companion object
    {
        /**
         * Wraps the [Context] to use the holo theme to avoid stupid bug on Samsung devices.
         */
        fun fixContext(context: Context): Context
        {
            return if (isBrokenSamsungDevice())
                ContextThemeWrapper(context, R.style.Ownapp_Theme_DatePickerDialog_Holo)
            else
                context
        }

        /**
         * Affected devices:
         * - Samsung 5.0
         * - Samsung 5.1
         *
         * @return true if device is affected by this bug.
         */
        fun isBrokenSamsungDevice() = Build.MANUFACTURER.equals("samsung", ignoreCase = true)
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    constructor(context: Context): super(fixContext(context))

    @RequiresApi(api = Build.VERSION_CODES.N)
    constructor(context: Context, @StyleRes themeResId: Int): super(fixContext(context), themeResId)

    constructor(context: Context, listener: OnDateSetListener?, year: Int, month: Int, dayOfMonth: Int)
            : super(fixContext(context), listener, year, month, dayOfMonth)

    constructor(context: Context, @StyleRes themeResId: Int, listener: OnDateSetListener?, year: Int, monthOfYear: Int, dayOfMonth: Int)
            : super(fixContext(context), themeResId, listener, year, monthOfYear, dayOfMonth)
}