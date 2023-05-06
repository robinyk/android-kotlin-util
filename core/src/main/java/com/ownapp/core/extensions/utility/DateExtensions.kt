package com.ownapp.core.extensions.utility

import android.text.format.DateUtils
import com.ownapp.core.annotation.DateTimeFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

/**
 * Updated by Robin on 2020/12/4
 */

var dateTimeZone = "GMT+8"
var dateLocale: Locale = Locale.ENGLISH

fun Long.toTimeZonedMillis(timeZone: TimeZone = TimeZone.getDefault()): Long
{
	return this - timeZone.rawOffset + timeZone.dstSavings
}

/**
 * Gets value of Milliseconds of current time
 */
inline val now: Long
	get() = ZonedDateTime.now(ZoneId.of(dateTimeZone)).toInstant().toEpochMilli()

inline val Long.calendar: Calendar
	get() = Calendar.getInstance().apply { timeInMillis = this@calendar }

/**
 * Gives [Date] object from [Long]
 */
inline val Long.date: Date
	get() = Calendar.getInstance().apply { timeInMillis = this@date }.time

/**
 * Gives [Calendar] object from Date
 */
inline val Date.calendar: Calendar
	get() = Calendar.getInstance().apply { time = this@calendar }

/**
 * Gets  Year directly from [Calendar] Object
 */
inline val Calendar.year: Int
	get() = get(Calendar.YEAR)

/**
 * Gets value of DayOfMonth from [Calendar] Object
 */
inline val Calendar.dayOfMonth: Int
	get() = get(Calendar.DAY_OF_MONTH)

/**
 * Gets value of Month from [Calendar] Object
 */
inline val Calendar.month: Int
	get() = get(Calendar.MONTH)

/**
 * Gets value of DayOfWeek from [Calendar] Object
 */
inline val Calendar.dayOfWeek: Int
	get() = get(Calendar.DAY_OF_WEEK)

/**
 * Gets value of Hour from [Calendar] Object
 */
inline val Calendar.hour: Int
	get() = get(Calendar.HOUR)

/**
 * Gets value of HourOfDay from [Calendar] Object
 */
inline val Calendar.hourOfDay: Int
	get() = get(Calendar.HOUR_OF_DAY)

/**
 * Gets value of Minute from [Calendar] Object
 */
inline val Calendar.minute: Int
	get() = get(Calendar.MINUTE)

/**
 * Gets value of Second from [Calendar] Object
 */
inline val Calendar.second: Int
	get() = get(Calendar.SECOND)

/**
 * Gets value of DayOfMonth from [Date] Object
 */
inline val Date.yearFromCalendar: Int
	get() = calendar.year

/**
 * Gets value of DayOfMonth from [Date] Object
 */
inline val Date.dayOfMonth: Int
	get() = calendar.dayOfMonth

/**
 * Gets value of Month from [Date] Object
 */
inline val Date.monthFromCalendar: Int
	get() = calendar.month

/**
 * Gets value of Hour from [Date] Object
 */
inline val Date.hour: Int
	get() = calendar.hour

/**
 * Gets value of HourOfDay from [Date] Object
 */
inline val Date.hourOfDay: Int
	get() = calendar.hourOfDay

/**
 * Gets value of Minute from [Date] Object
 */
inline val Date.minute: Int
	get() = calendar.minute

/**
 * Gets value of Second from [Date] Object
 */
inline val Date.second: Int
	get() = calendar.second

inline val Date.isSunday: Boolean
	get() = calendar.dayOfWeek == Calendar.SUNDAY

inline val Date.isMonday: Boolean
	get() = calendar.dayOfWeek == Calendar.MONDAY

inline val Date.isTuesday: Boolean
	get() = calendar.dayOfWeek == Calendar.TUESDAY

inline val Date.isWednesday: Boolean
	get() = calendar.dayOfWeek == Calendar.WEDNESDAY

inline val Date.isThursday: Boolean
	get() = calendar.dayOfWeek == Calendar.THURSDAY

inline val Date.isFriday: Boolean
	get() = calendar.dayOfWeek == Calendar.FRIDAY

inline val Date.isSaturday: Boolean
	get() = calendar.dayOfWeek == Calendar.SATURDAY

inline val Date.isWeekend: Boolean
	get() = this.isSaturday || this.isSunday

inline val Date.isWeekday: Boolean
	get() = !this.isWeekend

inline val Date.isToday: Boolean
	get() = DateUtils.isToday(calendar.timeInMillis)

val Date.isYesterday: Boolean
	get() = isDateIn(this, -1)

val Date.isTomorrow: Boolean
	get() = isDateIn(this, 1)

inline val Date.isFuture: Boolean
	get() = Date().after(this)

inline val Date.isPast: Boolean
	get() = Date().before(this)

inline val Date.beginningOfYear: Date
	get() = with(month = 1, day = 1, hour = 0, minute = 0, second = 0, millisecond = 0)

inline val Date.endOfYear: Date
	get() = with(month = 12, day = 31, hour = 23, minute = 59, second = 59, millisecond = 999)

inline val Date.beginningOfMonth: Date
	get() = with(day = 1, hour = 0, minute = 0, second = 0, millisecond = 0)

inline val Date.endOfMonth: Date
	get() = with(day = calendar.getActualMaximum(Calendar.DATE), hour = 23, minute = 59, second = 59)

inline val Date.beginningOfDay: Date
	get() = with(hour = 0, minute = 0, second = 0, millisecond = 0)

inline val Date.endOfDay: Date
	get() = with(hour = 23, minute = 59, second = 59, millisecond = 999)

inline val Date.beginningOfHour: Date
	get() = with(minute = 0, second = 0, millisecond = 0)

inline val Date.endOfHour: Date
	get() = with(minute = 59, second = 59, millisecond = 999)

inline val Date.beginningOfMinute: Date
	get() = with(second = 0, millisecond = 0)

inline val Date.endOfMinute: Date
	get() = with(second = 59, millisecond = 999)

fun Date.with(
	year: Int = -1,
	month: Int = -1,
	day: Int = -1,
	hour: Int = -1,
	minute: Int = -1,
	second: Int = -1,
	millisecond: Int = -1
): Date = calendar.apply {
	if (year > -1) set(Calendar.YEAR, year)
	if (month > 0) set(Calendar.MONTH, month - 1)
	if (day > 0) set(Calendar.DATE, day)
	if (hour > -1) set(Calendar.HOUR_OF_DAY, hour)
	if (minute > -1) set(Calendar.MINUTE, minute)
	if (second > -1) set(Calendar.SECOND, second)
	if (millisecond > -1) set(Calendar.MILLISECOND, millisecond)
}.time

fun Date.with(weekday: Int = -1): Date
{
	return calendar.apply {
		if (weekday > -1) calendar.set(Calendar.WEEK_OF_MONTH, weekday)
	}.time
}

fun Date.format(
	format: String = DateTimeFormat.DASH_DATE
	, timeZone: String = dateTimeZone
	, locale: Locale = dateLocale
): String = SimpleDateFormat(format, locale).apply {
	this.timeZone = TimeZone.getTimeZone(timeZone)
}.format(this)

private fun isDateIn(date: Date, variable: Int = 0): Boolean
{
	val now = Calendar.getInstance()
	val specificDate = Calendar.getInstance().apply { timeInMillis = date.time }

	now.add(Calendar.DATE, variable)

	return (now.year == specificDate.year
			&& now.get(Calendar.MONTH) == specificDate.get(Calendar.MONTH)
			&& now.get(Calendar.DATE) == specificDate.get(Calendar.DATE))
}