package com.ownapp.core.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.*
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.annotation.StyleRes
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.core.widget.TextViewCompat
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.ownapp.core.R
import com.ownapp.core.extensions.*
import com.ownapp.core.extensions.view.setCursorDrawable
import com.ownapp.core.annotation.ResourceType
import com.ownapp.core.extensions.resource.*
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.logException
import com.ownapp.core.extensions.utility.showKeyboard
import com.ownapp.core.util.ViewUtil
import kotlin.math.max

/**
 * Updated by Robin on 2020/12/4
 */

class PinEditText: ConstraintLayout, View.OnFocusChangeListener, TextWatcher
{
    //**--------------------------------------------------------------------------------------------------
    //*     Class
    //---------------------------------------------------------------------------------------------------*/
    class MyPasswordTransformationMethod: PasswordTransformationMethod()
    {
        override fun getTransformation(source: CharSequence?, view: View?): CharSequence
        {
            return MyPasswordCharSequence(source!!)
        }

        inner class MyPasswordCharSequence(private val mSource: CharSequence): CharSequence
        {
            override val length: Int
                get() = mSource.length

            override fun get(index: Int): Char = PASSWORD_DOT_LARGE

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence
            {
                return mSource.subSequence(startIndex, endIndex)
            }

        }
    }

    interface OnOtpChangedListener
    {
        fun onOtpChanged(text: String, isFilled: Boolean)
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Variable
    //---------------------------------------------------------------------------------------------------*/
    companion object
    {
        private val DEF_STYLE_RES = R.style.Ownapp_Style_OtpEditText

        private const val PASSWORD_DOT = '\u2022'
        private const val PASSWORD_DOT_LARGE = '●'
    }

    // Value

    private lateinit var tag: String

    var onOtpChangedListener: OnOtpChangedListener? = null

    var text: String = ""
        get()
        {
            var text = ""

            for(i in 0 until childCount)
            {
                text += (getChildAt(i) as AppCompatEditText).text.toString()
            }

            return text
        }
        private set

    var isFilled: Boolean = false
        get() = text.length == childCount
        private set

    var chainStyle: Int = 0
        set(@IntRange(from = 0, to = 2) value)
    {
        field = value

        val set = ConstraintSet()
        set.clone(this)

        if(childCount > 0)
        {
            getEditTextAt(0)?.let { set.setHorizontalChainStyle(it.id, value) }
        }

        set.applyTo(this)
        requestLayout()
    }

    var textBackground: Drawable? = null
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    background = value

                    backgroundTintList = if(value == null)
                        context.getAttrColorStateList(android.R.attr.colorPrimary)
                    else null

                    applyTextBackgroundTint(this)
                }
            }
        }

    @ColorInt var textBackgroundColor: Int? = null
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    if(value != null)
                        setBackgroundColor(value)
                    else
                        textBackground = null

                    applyTextBackgroundTint(this)
                }
            }
        }

    var textBackgroundTintList: ColorStateList? = null
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    background?.apply {
                        if(value != null)
                            mutate().colorFilter = PorterDuffColorFilter(value.defaultColor, PorterDuff.Mode.SRC_IN)
                        else
                            mutate().clearColorFilter()
                    }
                }
            }
        }

    var space: Int = 0
        set(@IntRange(from = 0) value)
        {
            field = value

            for(i in 1 until childCount)
            {
                (getEditTextAt(i)?.layoutParams as MarginLayoutParams).apply {
                    marginStart = value
                }
            }
        }

    var otpLength: Int = 4
        set(@IntRange(from = 1) value)
        {
            field = value
            createOtpView()
        }

    var inputType: Int = 0
        set(@IntRange(from = 1) value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    inputType = value

                    if(inputType == InputType.TYPE_CLASS_TEXT + InputType.TYPE_TEXT_VARIATION_PASSWORD
                       || inputType == InputType.TYPE_CLASS_NUMBER + InputType.TYPE_NUMBER_VARIATION_PASSWORD
                    )
                    {
                        transformationMethod = MyPasswordTransformationMethod()
                    }
                }
            }
        }

    var isCursorVisible: Boolean = false
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.isCursorVisible = value
            }
        }

    @StyleRes var textAppearance: Int = 0
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    TextViewCompat.setTextAppearance(this, value)
                }
            }
        }

    var textPadding: Int = 0
        set(value)
        {
            field = value

            textPaddingVertical = value
            textPaddingHorizontal = value
        }

    var textPaddingVertical: Int = 0
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    setPadding(textPaddingHorizontal, value, textPaddingHorizontal, value)
                    requestLayout()
                }
            }
        }

    var textPaddingHorizontal: Int = 0
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    setPadding(value, textPaddingVertical, value, textPaddingVertical)
                    requestLayout()
                }
            }
        }

    var textLength: Int = 1
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    filters = arrayOf<InputFilter>(InputFilter.LengthFilter(value))
                    setEms(value + 1)
                }
            }
        }

    var textElevation: Float = 0f
        set(value)
        {
            field = value

            for(i in 0 until childCount)
            {
                getEditTextAt(i)?.apply {
                    elevation = value
                }
            }
        }


    //**--------------------------------------------------------------------------------------------------
    //*     Constructor
    //---------------------------------------------------------------------------------------------------*/
    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.otpEditTextStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, DEF_STYLE_RES)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes)
    {
        try
        {
            attrs?.let { initialize(it, defStyleAttr, defStyleRes) }
            ?: "${this::class.java.simpleName}: AttributeSet is null".logError()
        }
        catch (e: Exception)
        {
            e.logException()
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     OnCreate
    //---------------------------------------------------------------------------------------------------*/
    private fun initialize(attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int)
    {
        tag = "OtpEditText_${View.generateViewId()}"

        clipToPadding = false
        clipChildren = false

        val a = context.obtainStyledAttributes(attrs, R.styleable.OtpEditText, defStyleAttr, defStyleRes)

        otpLength = a.getInt(R.styleable.OtpEditText_otpLength, 4)

        chainStyle = a.getInt(R.styleable.OtpEditText_chainStyle, ConstraintSet.CHAIN_SPREAD)
        space = a.getDimensionPixelSize(R.styleable.OtpEditText_space, resources.getDimensionPixelSize(R.dimen.view_padding_s))

        inputType = a.getInt(R.styleable.OtpEditText_android_inputType, InputType.TYPE_CLASS_NUMBER)
        isCursorVisible = a.getBoolean(R.styleable.OtpEditText_android_cursorVisible, false)

        if(a.hasValue(R.styleable.OtpEditText_android_textAppearance))
            textAppearance = a.getResourceId(R.styleable.OtpEditText_android_textAppearance,
                                             android.R.style.TextAppearance_DeviceDefault_Widget_EditText)
        if(a.hasValue(R.styleable.OtpEditText_textBackground))
            setTextBackgroundResource(a.getResourceId(R.styleable.OtpEditText_textBackground, 0))

        if(a.hasValue(R.styleable.OtpEditText_textBackgroundTint))
            textBackgroundTintList = context.getStyleableColorStateList(a, R.styleable.OtpEditText_textBackgroundTint)

        textPadding = a.getDimensionPixelSize(R.styleable.OtpEditText_textPadding, resources.getDimensionPixelSize(R.dimen.view_padding))

        if(a.hasValue(R.styleable.OtpEditText_textPaddingVertical))
            textPaddingVertical = a.getDimensionPixelSize(R.styleable.OtpEditText_textPaddingVertical, textPaddingVertical)

        if(a.hasValue(R.styleable.OtpEditText_textPaddingHorizontal))
            textPaddingHorizontal = a.getDimensionPixelSize(R.styleable.OtpEditText_textPaddingHorizontal, textPaddingHorizontal)

        if(a.hasValue(R.styleable.OtpEditText_textElevation))
            textElevation = a.getDimensionPixelSize(R.styleable.OtpEditText_textElevation, 0).toFloat()

        textLength = a.getInt(R.styleable.OtpEditText_textLength, 1)

        a.recycle()

        createOtpView()
        requestLayout()
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Private
    //---------------------------------------------------------------------------------------------------*/
    private fun createEditText(): AppCompatEditText
    {
        return AppCompatEditText(context).apply {
            id = View.generateViewId()
            tag = this@PinEditText.tag

            layoutParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            gravity = Gravity.CENTER
            setPadding(0)
//            backgroundTintList = ResourceUtil.getAttrColorStateList(context, android.R.attr.colorPrimary)

//            inputType = this@OtpEditText.inputType
//            filters = arrayOf<InputFilter>(InputFilter.LengthFilter(textLength))
//            setEms(textLength + 1)

            setCursorDrawable(R.drawable.shape_cursor)
//            isCursorVisible = this@OtpEditText.isCursorVisible

            onFocusChangeListener = this@PinEditText
            addTextChangedListener(this@PinEditText)
        }
    }

    private fun createOtpView()
    {
        if(otpLength < childCount)
        {
            while (childCount > otpLength)
            {
                removeViewAt(childCount)
            }
        }
        else
        {
            for(i in childCount until otpLength)
            {
                addView(createEditText())
            }
        }

        setupConstraint()
    }

    private fun setupConstraint()
    {
        val set = ConstraintSet()
        set.clone(this)

        for(i in 0 until childCount)
        {
            set.connect(getChildAt(i).id, ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID, ConstraintSet.TOP)

            set.connect(getChildAt(i).id, ConstraintSet.BOTTOM,
                        ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

            when(i)
            {
                0 ->
                {
                    set.connect(getChildAt(i).id, ConstraintSet.LEFT,
                                ConstraintSet.PARENT_ID, ConstraintSet.LEFT)

                    set.connect(getChildAt(i).id, ConstraintSet.RIGHT,
                                getChildAt(i + 1).id, ConstraintSet.LEFT)
                }

                childCount - 1 ->
                {
                    set.connect(getChildAt(i).id, ConstraintSet.LEFT,
                                getChildAt(i - 1).id, ConstraintSet.RIGHT)

                    set.connect(getChildAt(i).id, ConstraintSet.RIGHT,
                                ConstraintSet.PARENT_ID, ConstraintSet.RIGHT)
                }

                else ->
                {
                    set.connect(getChildAt(i).id, ConstraintSet.LEFT,
                                getChildAt(i - 1).id, ConstraintSet.RIGHT)

                    set.connect(getChildAt(i).id, ConstraintSet.RIGHT,
                                getChildAt(i + 1).id, ConstraintSet.LEFT)
                }
            }
        }

        set.applyTo(this)
    }

    private fun applyTextBackgroundTint(view: AppCompatEditText?)
    {
        view?.run {
            backgroundTintList = textBackgroundTintList
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Public
    //---------------------------------------------------------------------------------------------------*/
    fun overrideDispatchKeyEvent(event: KeyEvent, currentFocus: View?): Boolean?
    {
        if (event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_DEL)
        {
            if(currentFocus is AppCompatEditText
               && currentFocus.tag == tag
               && currentFocus.text.isNullOrEmpty()
               && currentFocus.focusSearch(View.FOCUS_LEFT) is AppCompatEditText
               && currentFocus.focusSearch(View.FOCUS_LEFT).tag == tag
            )
            {
                (currentFocus.focusSearch(View.FOCUS_LEFT) as AppCompatEditText).apply {
                    setText(text!!.substring(0, text!!.length - 1))
                    requestFocus()
                    return true
                }
            }
        }

        return null
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Getter
    //---------------------------------------------------------------------------------------------------*/
    fun getEditTextAt(position: Int): AppCompatEditText? = if(position < childCount) (getChildAt(position) as AppCompatEditText) else null


    //**--------------------------------------------------------------------------------------------------
    //*      Setter
    //---------------------------------------------------------------------------------------------------*/
    fun setTextBackgroundResource(resId: Int)
    {
        for(i in 0 until childCount)
        {
            getEditTextAt(i)?.apply {
                when (resources.getResourceTypeName(resId))
                {
                    ResourceType.DRAWABLE -> textBackground = context.getDrawableCompat(resId)
                    ResourceType.COLOR, ResourceType.ATTRIBUTE -> textBackgroundColor = resId.toColor(context)
                    else -> "${this@PinEditText::class.java.simpleName}: Invalid resource type".logError()
                }
            }
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Override
    //---------------------------------------------------------------------------------------------------*/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val specModeW = MeasureSpec.getMode(widthMeasureSpec)
        val specModeH = MeasureSpec.getMode(heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val calculatedWidth = ViewUtil.measureDimension(max(desiredWidth, measuredWidth), widthMeasureSpec)
        val calculatedHeight = ViewUtil.measureDimension(max(desiredHeight, measuredHeight), heightMeasureSpec)

        setMeasuredDimension(calculatedWidth, calculatedHeight)

        measureChildren(MeasureSpec.makeMeasureSpec(calculatedWidth, specModeW),
                        MeasureSpec.makeMeasureSpec(calculatedHeight, specModeH))
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean
    {
        getEditTextAt(0)?.requestFocus()
        context.showKeyboard()
        return super.onTouchEvent(event)
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Implement
    //---------------------------------------------------------------------------------------------------*/
    override fun onFocusChange(view: View?, hasFocus: Boolean)
    {
        if(view is AppCompatEditText)
        {
            view.text?.length?.let { view.setSelection(it) }

            if(hasFocus)
            {
                view.isCursorVisible = isCursorVisible

                if(text.isBlank())
                {
                    getEditTextAt(0)?.requestFocus()
                }
                else if(view.text.isNullOrBlank())
                {
                    if(view.focusSearch(View.FOCUS_LEFT) is AppCompatEditText)
                    {
                        if((view.focusSearch(View.FOCUS_LEFT) as AppCompatEditText).text.isNullOrBlank())
                            view.focusSearch(View.FOCUS_LEFT).requestFocus()
                    }
                }
                else if(view.text!!.length >= (view.filters[0] as InputFilter.LengthFilter).max)
                {
                    if(view.focusSearch(View.FOCUS_RIGHT) is AppCompatEditText)
                    {
                        (view.focusSearch(View.FOCUS_RIGHT) as AppCompatEditText).run {
                            requestFocus()
                        }

                        view.clearFocus()
                    }
                }

                context.showKeyboard()
            }
            else
                view.isCursorVisible = false
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
    {
    }

    override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int)
    {
    }

    override fun afterTextChanged(editable: Editable)
    {
        if(focusedChild != null)
        {
            val editText = focusedChild as AppCompatEditText

            if(editText.text.isNullOrEmpty()
               && editText.focusSearch(View.FOCUS_LEFT) != null
               && editText.focusSearch(View.FOCUS_LEFT).tag == tag
            )
            {
                editText.focusSearch(View.FOCUS_LEFT).requestFocus()
//                editText.clearFocus()
            }
            else if(!editText.text.isNullOrBlank()
                    && editText.text!!.length >= (editText.filters[0] as InputFilter.LengthFilter).max
                    && editText.focusSearch(View.FOCUS_RIGHT) != null
                    && editText.focusSearch(View.FOCUS_RIGHT).tag == tag
            )
            {
                editText.focusSearch(View.FOCUS_RIGHT).requestFocus()
//                editText.clearFocus()
            }

            onOtpChangedListener?.onOtpChanged(text, isFilled)
        }
    }
}