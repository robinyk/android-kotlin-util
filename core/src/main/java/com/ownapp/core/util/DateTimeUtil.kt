package com.ownapp.core.util

import com.ownapp.core.extensions.utility.dateTimeZone
import com.ownapp.core.extensions.utility.dateLocale
import com.ownapp.core.annotation.DateTimeFormat
import com.ownapp.core.extensions.utility.logException
import com.ownapp.core.extensions.utility.now
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Updated by Robin on 2020/12/4
 */

object DateTimeUtil
{
	/**
	 * Get current epoch milliseconds based on [dateTimeZone] from [ZonedDateTime]<br>
	 *
	 * <p>
	 * Format String: DateTimeFormatter.ofPattern(DateTimeFormat.DASH_DATE_TIME_24)
	 * .format(ZonedDateTime.now(ZoneId.of(ZoneIdFormat.KUALA_LUMPUR))
	 * </p>
	 */
	fun getCurrentMillis(): Long = now

	/**
	 * Get current date time and convert it into readable text format
	 *
	 * @param format Can use [DateTimeFormat] for pre-defined format
	 * @param timeZone Default is global value [dateTimeZone], which is set
	 * to Malaysia (GMT+8). Can override this variable.
	 * @param locale Default is global value [dateLocale], which is set
	 * to [Locale.getDefault()]. Can override this variable.
	 */
	fun getCurrentString(
		format: String = DateTimeFormat.DASH_DATE_TIME
		, timeZone: String = dateTimeZone
		, locale: Locale = dateLocale
	): String? = getString(Calendar.getInstance().time, format, timeZone, locale)

	/**
	 * Convert [Date] into readable text format
	 *
	 * @param dateTime [Date]
	 * @param format Can use [DateTimeFormat] for pre-defined format
	 * @param timeZone Default is global value [dateTimeZone], which is set
	 * to Malaysia (GMT+8). Can override this variable.
	 * @param locale Default is global value [dateLocale], which is set
	 * to [Locale.getDefault()]. Can override this variable.
	 */
	fun getString(
		dateTime: Date
		, format: String = DateTimeFormat.DASH_DATE_TIME
		, timeZone: String = dateTimeZone
		, locale: Locale = dateLocale
	): String?
	{
		return try
		{
			SimpleDateFormat(format, locale).apply {
				this.timeZone = TimeZone.getTimeZone(timeZone)
			}.format(dateTime)
		}
		catch(e: Exception)
		{
			e.logException()
			null
		}
	}

	/**
	 * Convert [Calendar] into readable text format
	 *
	 * @param calendar [Calendar]
	 * @param format Can use [DateTimeFormat] for pre-defined format
	 * @param timeZone Default is global value [dateTimeZone], which is set
	 * to Malaysia (GMT+8). Can override this variable.
	 * @param locale Default is global value [dateLocale], which is set
	 * to [Locale.getDefault()]. Can override this variable.
	 */
	fun getString(
		calendar: Calendar
		, format: String = DateTimeFormat.DASH_DATE_TIME
		, timeZone: String = dateTimeZone
		, locale: Locale = dateLocale
	): String? = getString(calendar.time, format, timeZone, locale)

	/**
	 * Convert [Long] into readable text format
	 *
	 * @param timeInMillis [Long]
	 * @param format Can use [DateTimeFormat] for pre-defined format
	 * @param timeZone Default is global value [dateTimeZone], which is set
	 * to Malaysia (GMT+8). Can override this variable.
	 * @param locale Default is global value [dateLocale], which is set
	 * to [Locale.getDefault()]. Can override this variable.
	 */
	fun getString(
		timeInMillis: Long
		, format: String = DateTimeFormat.DASH_DATE_TIME
		, timeZone: String = dateTimeZone
		, locale: Locale = dateLocale
	): String? = getString(Date(timeInMillis), format, timeZone, locale)

	fun getDifferenceInDay(date: Date, otherDate: Date = Calendar.getInstance().time): Long
	{
		return if(date.after(otherDate))
			TimeUnit.MILLISECONDS.toDays(date.time - otherDate.time)
		else
			TimeUnit.MILLISECONDS.toDays(otherDate.time - date.time)
	}

	fun getDifferenceInSecond(date: Date, otherDate: Date = Calendar.getInstance().time): Long
	{
		return if(date.after(otherDate))
			TimeUnit.MILLISECONDS.toSeconds(date.time - otherDate.time)
		else
			TimeUnit.MILLISECONDS.toSeconds(otherDate.time - date.time)
	}

	/**
	 * Parse string from server
	 */
	fun String.parseDateTime() = (if(this.length > 16)
		this.substring(0, 16)
	else this).replace("T", " ")

	fun String.parseDate() = if(this.length > 10)
		this.substring(0, 10)
	else this

	fun parseTime(date: String?): String = if(date.orEmpty().length > 16) date?.substring(11, 16).orEmpty() else ""
	fun parseYear(date: String?): Int? = if(date.orEmpty().length > 9) date?.substring(0, 4)?.toIntOrNull() else null
	fun parseMonth(date: String?): Int? = if(date.orEmpty().length > 9) date?.substring(5, 7)?.toIntOrNull() else null
	fun parseDay(date: String?): Int? = if(date.orEmpty().length > 9) date?.substring(8, 10)?.toIntOrNull() else null
}