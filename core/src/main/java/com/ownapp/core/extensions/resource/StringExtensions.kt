package com.ownapp.core.extensions.resource

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Base64
import android.util.Patterns
import android.webkit.URLUtil
import androidx.annotation.IntRange
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.ownapp.core.extensions.utility.logException
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.net.MalformedURLException
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*
import java.util.regex.Pattern


//**--------------------------------------------------------------------------------------------------
//*      Hash
//---------------------------------------------------------------------------------------------------*/
enum class HashType
{
    MD5, SHA1, SHA256
}

fun String.hash(type: HashType): String = MessageDigest.getInstance(type.name)
    .digest(toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

val String.md5: String
    get() = hash(HashType.MD5)

val String.sha1: String
    get() = hash(HashType.SHA1)


//**--------------------------------------------------------------------------------------------------
//*      Validate
//---------------------------------------------------------------------------------------------------*/
val  String.isValidEmail: Boolean
    get() = Pattern.compile("^[\\w.-]+@([\\w\\-]+\\.)+[A-Z]{2,8}$", Pattern.CASE_INSENSITIVE)
        .matcher(this)
        .matches()

val  String.isValidUrl: Boolean
    get()
    {
        try
        {
            return URLUtil.isValidUrl(this) && Patterns.WEB_URL.matcher(this).matches()
        }
        catch(e: MalformedURLException)
        {
            e.logException()
        }

        return false
    }

val String.containsLatinLetter: Boolean
    get() = matches(Regex(".*[A-Za-z].*"))

val String.containsDigit: Boolean
    get() = matches(Regex(".*[0-9].*"))

val String.isAlphanumeric: Boolean
    get() = matches(Regex("[A-Za-z0-9]*"))

val String.hasLettersAndDigits: Boolean
    get() = containsLatinLetter && containsDigit

val String.isIntegerNumber: Boolean
    get() = toIntOrNull() != null

val String.isDecimalNumber: Boolean
    get() = toDoubleOrNull() != null


//**--------------------------------------------------------------------------------------------------
//*      Format
//---------------------------------------------------------------------------------------------------*/
fun String.toCreditCardFormat(censor: Char? = null): String
{
    StringBuilder().run {
        replace(" ", "").trim().let {
            onEachIndexed { index, char ->
                if(index % 4 == 0 && index != 0)
                    append(" ")

                append(if(censor != null && index + 4 >= it.length) censor else char)
            }
        }

        return toString()
    }
}

val String.lowerCased
    get() = toLowerCase(Locale.getDefault())

val String.upperCased
    get() = toUpperCase(Locale.getDefault())

val String.capitalized
    get() = capitalize(Locale.getDefault())


//**--------------------------------------------------------------------------------------------------
//*      Utility
//---------------------------------------------------------------------------------------------------*/
fun String.getIntOrNull(): Int? = Scanner(this).useDelimiter("[^0-9]+").run {
    if(hasNextInt())
        nextInt()
    else null
}

val String?.encode: String
    get()
    {
        return try
        {
            URLEncoder.encode(this, Charsets.UTF_8.toString())
        }
        catch(e: UnsupportedEncodingException)
        {
            e.logException()
            ""
        }
    }

val String.bitmap: Bitmap?
    get() = try
    {
        (if(contains(",")) this.split(",")[1] else this).let {
            Base64.decode(it, Base64.DEFAULT).run {
                BitmapFactory.decodeByteArray(this, 0, this.size)
            }
        }
    }
    catch(e: Exception)
    {
        Timber.e(e, "Invalid Base64 string")
        null
    }

/**
 * Method to check String equalsIgnoreCase
 */
fun String.equalsIgnoreCase(other: String) = this.lowerCased.contentEquals(other.lowerCased)

@JvmOverloads
fun String.toQrBitmap(@IntRange(from = 1) size: Int = 512): Bitmap?
{
    var bitmap: Bitmap? = null

    try
    {
        val bitMatrix = MultiFormatWriter().encode(this, BarcodeFormat.QR_CODE, size, size, null)
        val bitMatrixWidth = bitMatrix.width
        val bitMatrixHeight = bitMatrix.height

        bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.RGB_565)

        for(x in 0 until bitMatrixWidth)
        {
            for(y in 0 until bitMatrixHeight)
            {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
    }
    catch (e: Exception)
    {
        e.logException()
    }

    return bitmap
}