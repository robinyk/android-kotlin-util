package com.ownapp.core.annotation

/**
 * Updated by Robin on 2020/12/4
 */
@Retention(AnnotationRetention.BINARY)
annotation class DateTimeFormat
{
    companion object
    {
        const val YEAR = "yyyy"
        const val YEAR_SHORT = "yy"
        const val MONTH = "MM"
        const val MONTH_WORD = "MMM"
        const val MONTH_WORD_FULL = "MMMM"
        const val WEEK_IN_YEAR = "w"
        const val WEEK_IN_MONTH = "W"
        const val DAY_WORD_SHORT = "E"
        const val DAY_WORD_FULL = "EEEE"
        const val DAY = "d"
        const val DAY_2_DIGITS = "dd"
        const val DAY_IN_YEAR = "D"
        const val HOUR_24 = "HH"
        const val HOUR = "h"
        const val MINUTE = "mm"
        const val SECOND = "ss"
        const val TIMEZONE = "z"
        const val MERIDIAN = "a"

        const val TIME = "$HOUR:$MINUTE $MERIDIAN"
        const val TIME_24 = "$HOUR_24:$MINUTE"

        const val DATE_FORMAL = "$DAY $MONTH_WORD $YEAR"
        const val MONTH_DAY_FORMAL = "$MONTH_WORD $DAY"

        const val DASH_DATE = "$YEAR-$MONTH-$DAY_2_DIGITS"
        const val DASH_DATE_TIME_24 = "$DASH_DATE $TIME_24"
        const val DASH_DATE_TIME = "$DASH_DATE $TIME"

        const val SLASH_DATE = "$DAY_2_DIGITS/$MONTH/$YEAR"
        const val SLASH_DATE_TIME = "$SLASH_DATE $TIME"
    }
}
