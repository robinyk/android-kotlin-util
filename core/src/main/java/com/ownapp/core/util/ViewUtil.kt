package com.ownapp.core.util

import android.graphics.PorterDuff
import android.text.TextUtils.TruncateAt
import android.view.View
import com.ownapp.core.extensions.utility.log
import kotlin.math.min

/**
 * Updated by Robin on 2020/12/4
 */

object ViewUtil
{
    fun parsePorterDuffMode(value: Int, defaultMode: PorterDuff.Mode = PorterDuff.Mode.SRC_IN): PorterDuff.Mode
    {
        return when (value)
        {
            3 -> PorterDuff.Mode.SRC_OVER
            5 -> PorterDuff.Mode.SRC_IN
            9 -> PorterDuff.Mode.SRC_ATOP
            14 -> PorterDuff.Mode.MULTIPLY
            15 -> PorterDuff.Mode.SCREEN
            16 -> PorterDuff.Mode.ADD
            else -> defaultMode
        }
    }

    fun parseEllipsize(value: Int, defaultMode: TruncateAt = TruncateAt.END): TruncateAt
    {
        return when (value)
        {
            1 -> TruncateAt.START
            2 -> TruncateAt.MIDDLE
            3 -> TruncateAt.END
            4 -> TruncateAt.MARQUEE
            else -> defaultMode
        }
    }

    fun measureDimension(desiredSize: Int, measureSpec: Int): Int
    {
        var result: Int
        val specMode = View.MeasureSpec.getMode(measureSpec)
        val specSize = View.MeasureSpec.getSize(measureSpec)

        if (specMode == View.MeasureSpec.EXACTLY)
        {
            result = specSize
        }
        else
        {
            result = desiredSize

            if (specMode == View.MeasureSpec.AT_MOST)
                result = min(result, specSize)
        }

        if (result < desiredSize)
            "View is too small, content might get cut. Desired = $desiredSize, result = $result".log()

        return result
    }
}