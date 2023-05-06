package com.ownapp.core.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.text.isDigitsOnly
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.widget.TextViewCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.ownapp.core.R
import com.ownapp.core.databinding.ViewQuantityViewBinding
import com.ownapp.core.extensions.layoutInflater
import com.ownapp.core.extensions.resource.getStyleableColorStateList
import com.ownapp.core.extensions.resource.getTintedDrawable
import com.ownapp.core.extensions.resource.toColorStateList
import com.ownapp.core.extensions.utility.logException
import com.ownapp.core.extensions.view.gone
import com.ownapp.core.extensions.view.setTextWithCorrectSelection
import com.ownapp.core.util.ViewUtil
import kotlin.math.max


/**
 * Updated by Robin on 2020/12/4
 */

class QuantityView: LinearLayout, View.OnFocusChangeListener, TextWatcher
{
    //**--------------------------------------------------------------------------------------------------
    //*     Binding
    //---------------------------------------------------------------------------------------------------*/
    object BindingAdapters
    {
        @BindingAdapter("quantity", "onBindingValueChangedListener", requireAll = false)
        @JvmStatic fun QuantityView.setQuantity(value: Int?, onBindingValueChangedListener: InverseBindingListener?)
        {
            this.onBindingValueChangedListener = onBindingValueChangedListener

            if(value != null && quantity != value)
                quantity = value
        }

        @InverseBindingAdapter(attribute = "quantity", event = "onBindingValueChangedListener")
        @JvmStatic fun QuantityView.getQuantity(): Int = quantity

        @BindingAdapter("min")
        @JvmStatic fun QuantityView.setMin(value: Int?)
        {
            if(value != null)
                min = value
        }

        @BindingAdapter("max")
        @JvmStatic fun QuantityView.setMax(value: Int?)
        {
            if(value != null)
                max = value
        }

        @BindingAdapter("onValueChangedListener")
        @JvmStatic fun QuantityView.setOnValueChangedListener(listener: OnValueChangedListener?)
        {
            onValueChangedListener = listener
        }
    
        @BindingAdapter("editable")
        @JvmStatic fun QuantityView.setEditable(isEnabled: Boolean?)
        {
            if(isEnabled != null)
                isEditable = isEnabled
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Interface
    //---------------------------------------------------------------------------------------------------*/
    interface OnValueChangedListener
    {
        fun onValueChanged(newQuantity: Int, isIncrease: Boolean? = null)
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Variable
    //---------------------------------------------------------------------------------------------------*/
    // Class
    private lateinit var binding: ViewQuantityViewBinding

    var onValueChangedListener: OnValueChangedListener? = null
    private var onBindingValueChangedListener: InverseBindingListener? = null

    // View
    val removeButtonImageView
        get() = binding.removeButtonImageView
    val addButtonImageView
        get() = binding.addButtonImageView
    val editText
        get() = binding.editText
    
    // Value
    var text: String = ""
        get() = quantity.toString()
        private set

    var quantity: Int = 1
        set(value)
        {
            field = value
            editText.setTextWithCorrectSelection(value.toString())
            checkQuantity()
        }

    var interval: Int = 1
    var min: Int = 1
        set(value)
        {
            if(field != value)
            {
                field = value
                editText.minEms = value.toString().length

                if(max != -1 && value > max)
                    max = value
                else
                    checkQuantity()
            }
        }
    var max: Int = 99
        set(value)
        {
            if(field != value)
            {
                field = value

                if(value != -1)
                {
//                editText.setEms(value.toString().length)
//                editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(value.toString().length))

                    if(value < min)
                        min = value
                }
                else
                    checkQuantity()
            }
        }
    
    var isEditable: Boolean = true
        set(value)
        {
            field = value
            checkQuantity()
            
            if(!value)
                isTextEditable = value
        }
    
    var isTextEditable: Boolean = false
        set(value)
        {
            field = value

            editText.inputType = if(value)
                InputType.TYPE_CLASS_NUMBER
            else InputType.TYPE_NULL

            editText.isClickable = value
            editText.isFocusable = value
        }

    var textBackgroundTintList: ColorStateList? = null
        set(value)
        {
            if (field !== value)
            {
                field = value

                editText.background.getTintedDrawable(value, textBackgroundTintMode).let {
                    if(editText.background !== it)
                        editText.background = it
                }
            }
        }
    var textBackgroundTintMode: PorterDuff.Mode? = null


    //**--------------------------------------------------------------------------------------------------
    //*     Constructor
    //---------------------------------------------------------------------------------------------------*/
    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.quantityViewStyle)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(
        context,
        attrs,
        defStyleAttr,
        R.style.Ownapp_Style_QuantityView
    )

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr, defStyleRes)
    {
        try
        {
            binding = ViewQuantityViewBinding.inflate(context.layoutInflater, this, true)
            initialize(attrs, defStyleAttr, defStyleRes)
        }
        catch(e: Exception)
        {
            e.logException()
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Initialize
    //---------------------------------------------------------------------------------------------------*/
    private fun initialize(attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
    {
        if(attrs != null)
        {
            val a = context.obtainStyledAttributes(attrs, R.styleable.QuantityView, defStyleAttr, defStyleRes)

            // View
            removeButtonImageView.setOnClickListener{ remove() }
            addButtonImageView.setOnClickListener{ add() }

            // General
            if (a.hasValue(R.styleable.QuantityView_min))
                min = a.getInt(R.styleable.QuantityView_min, min)

            if (a.hasValue(R.styleable.QuantityView_max))
                max = a.getInt(R.styleable.QuantityView_max, max)

            quantity = a.getInt(R.styleable.QuantityView_defaultValue, min)

            isEditable = a.getBoolean(R.styleable.QuantityView_editable, true)
            isTextEditable = a.getBoolean(R.styleable.QuantityView_textEditable, false)

            // Text
            TextViewCompat.setTextAppearance(
                editText, a.getResourceId(
                    R.styleable.QuantityView_android_textAppearance, android.R.style.TextAppearance_Material_Body2
                )
            )

            if (a.hasValue(R.styleable.QuantityView_android_text))
                editText.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX, a.getDimensionPixelSize(
                        R.styleable.QuantityView_android_textSize, editText.textSize.toInt()
                    ).toFloat()
                )

            if (a.hasValue(R.styleable.QuantityView_android_textColor))
                editText.setTextColor(a.getColorStateList(R.styleable.QuantityView_android_textColor))

            if (a.hasValue(R.styleable.QuantityView_android_textStyle))
                editText.setTypeface(editText.typeface, a.getInt(R.styleable.QuantityView_android_textStyle, Typeface.NORMAL))

            if (a.hasValue(R.styleable.QuantityView_android_textAlignment))
                editText.textAlignment = a.getInt(R.styleable.QuantityView_android_textAlignment, View.TEXT_ALIGNMENT_CENTER)

            if (a.hasValue(R.styleable.QuantityView_android_letterSpacing))
                editText.letterSpacing = a.getFloat(R.styleable.QuantityView_android_letterSpacing, editText.letterSpacing)

            if (a.hasValue(R.styleable.QuantityView_textMargin))
                a.getDimensionPixelSize(R.styleable.QuantityView_textMargin, 0).let {
                    if(it != 0)
                        editText.setPadding(it)
                }

            if (a.hasValue(R.styleable.QuantityView_android_minEms))
                editText.minEms = a.getInt(R.styleable.QuantityView_android_minEms, max.toString().length)

            if (a.hasValue(R.styleable.QuantityView_textBackground))
                editText.background = a.getDrawable(R.styleable.QuantityView_textBackground)

            if (a.hasValue(R.styleable.QuantityView_textBackgroundTint))
                textBackgroundTintList = context.getStyleableColorStateList(a, R.styleable.QuantityView_textBackgroundTint)

            textBackgroundTintMode = ViewUtil.parsePorterDuffMode(a.getInt(R.styleable.QuantityView_textBackgroundTint, -1), PorterDuff.Mode.SRC_IN)

            // Button
            //        if (a.hasValue(R.styleable.CarrotQuantityButton_iconEnabledTint))
            //             setButtonStartTintList(context.getStyleableColorStateList(a, R.styleable.CarrotQuantityButton_iconEnabledTint))

            // Button Start
            if (a.hasValue(R.styleable.QuantityView_startIconDrawable))
                setButtonStartDrawable(a.getDrawable(R.styleable.QuantityView_startIconDrawable))

            //        if (a.hasValue(R.styleable.CarrotQuantityButton_startIconTint))
            //            setButtonStartTintList(context.getStyleableColorStateList(a, R.styleable.CarrotQuantityButton_startIconTint))
            //        else
            //        {
            //            context.getAttrColorStateList(R.attr.colorControlNormal)?.apply {
            //                startButton.getImageDrawable()?.colorFilter = PorterDuffColorFilter(this.defaultColor, PorterDuff.Mode.SRC_IN)
            //            }
            //        }
            //
            //        setButtonStartTintMode(ViewUtil.parseTintMode(a.getInt(R.styleable.CarrotQuantityButton_startIconTintMode, -1), PorterDuff.Mode.SRC_IN))

            // Button End
            if (a.hasValue(R.styleable.QuantityView_endIconDrawable))
                setButtonEndDrawable(a.getDrawable(R.styleable.QuantityView_endIconDrawable))

            //        if (a.hasValue(R.styleable.CarrotQuantityButton_endIconTint))
            //            setButtonEndTintList(context.getStyleableColorStateList(a, R.styleable.CarrotQuantityButton_endIconTint))
            //        else
            //        {
            //            context.getAttrColorStateList(R.attr.colorControlNormal)?.apply {
            //                endButton.getImageDrawable()?.colorFilter = PorterDuffColorFilter(this.defaultColor, PorterDuff.Mode.SRC_IN)
            //            }
            //        }
            //
            //        setButtonEndTintMode(ViewUtil.parseTintMode(a.getInt(R.styleable.CarrotQuantityButton_endIconTintMode, -1), PorterDuff.Mode.SRC_IN))

            a.recycle()
        }

        editText.onFocusChangeListener = this
        editText.addTextChangedListener(this)

        checkQuantity()
    }

    
    //**--------------------------------------------------------------------------------------------------
    //*     Private
    //---------------------------------------------------------------------------------------------------*/
    private fun checkQuantity()
    {
//        "$quantity < $min ?".log(value = quantity < min)
//        "$quantity > $max ?".log(value = quantity > max)

        if(quantity < min)
            quantity = min
        else if(max != -1 && quantity > max)
            quantity = max

        setButtonStartEnabled(isEditable && quantity > min)
        
        setButtonEndEnabled(
            isEditable
            && (max == -1 || quantity < max)
            && (quantity + interval).toString().length <= editText.maxEms
        )
    }

    private fun setButtonEnabled(imageView: ImageView, isEnabled: Boolean)
    {
        imageView.isVisible = isEditable
        imageView.isEnabled = isEnabled

        imageView.drawable?.let {
            if(isEnabled)
            {
                if(imageView.imageTintList != null)
                    it.clearColorFilter()
                else
                    it.colorFilter = PorterDuffColorFilter(
                        R.attr.colorControlNormal.toColorStateList(context).defaultColor
                        , PorterDuff.Mode.SRC_IN
                    )
            }
            else
                it.colorFilter = PorterDuffColorFilter(
                    R.attr.colorControlHighlight.toColorStateList(context).defaultColor
                    , PorterDuff.Mode.SRC_IN
                )
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Button Start
    //---------------------------------------------------------------------------------------------------*/
    fun setButtonStartDrawable(drawable: Drawable?)
    {
        removeButtonImageView.setImageDrawable(drawable)
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Button End
    //---------------------------------------------------------------------------------------------------*/
    fun setButtonEndDrawable(drawable: Drawable?)
    {
        addButtonImageView.setImageDrawable(drawable)
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Setter
    //---------------------------------------------------------------------------------------------------*/
    fun setButtonEndEnabled(isEnabled: Boolean)
    {
        setButtonEnabled(addButtonImageView, isEnabled)
    }

    fun setButtonStartEnabled(isEnabled: Boolean)
    {
        setButtonEnabled(removeButtonImageView, isEnabled)
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Setter
    //---------------------------------------------------------------------------------------------------*/
    fun onValueChanged(listener: (newQuantity: Int, isIncrease: Boolean?) -> Unit = { _, _ -> })
    {
        onValueChangedListener = object: OnValueChangedListener {
            override fun onValueChanged(newQuantity: Int, isIncrease: Boolean?)
            {
                listener(newQuantity, isIncrease)
            }
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Public
    //---------------------------------------------------------------------------------------------------*/
    fun add()
    {
        if(max == -1 || quantity + interval <= max || (quantity + interval).toString().length > editText.maxEms)
        {
            quantity += interval
            onValueChangedListener?.onValueChanged(quantity, true)
            onBindingValueChangedListener?.onChange()
        }
        else checkQuantity()
    }

    fun remove()
    {
        if(quantity - interval >= min)
        {
            quantity -= interval
            onValueChangedListener?.onValueChanged(quantity, false)
            onBindingValueChangedListener?.onChange()
        }
        else checkQuantity()
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Override
    //---------------------------------------------------------------------------------------------------*/
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom

        val calculatedWidth = ViewUtil.measureDimension(max(desiredWidth, measuredWidth), widthMeasureSpec)
        val calculatedHeight = ViewUtil.measureDimension(max(desiredHeight, measuredHeight), heightMeasureSpec)

        setMeasuredDimension(calculatedWidth, calculatedHeight)

        measureChildren(
            MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.getMode(widthMeasureSpec))
            , MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.getMode(heightMeasureSpec))
        )
    }


    //**--------------------------------------------------------------------------------------------------
    //*     Implement
    //---------------------------------------------------------------------------------------------------*/
    override fun onFocusChange(v: View?, hasFocus: Boolean)
    {
        if(hasFocus)
        {
            editText.hint = text
            editText.setText("")
        }
        else
        {
            if(editText.text.isNullOrBlank() && editText.hint?.toString()?.isDigitsOnly() == true)
                quantity = editText.hint.toString().toInt()
            else if(editText.text.toString().isDigitsOnly())
                quantity = editText.text.toString().toInt()

            editText.setTextWithCorrectSelection(editText.hint)
            editText.hint = ""

            onValueChangedListener?.onValueChanged(quantity, null)
            onBindingValueChangedListener?.onChange()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)
    {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)
    {

    }

    override fun afterTextChanged(s: Editable?)
    {
        editText.removeTextChangedListener(this)

        if(!editText.text.isNullOrBlank()
            && !editText.hint.isNullOrBlank()
            && editText.text.toString().isDigitsOnly()
            && editText.hint.toString() != editText.text.toString()
        )
        {
            editText.hint = editText.text
            quantity = editText.text.toString().toInt()
            onValueChangedListener?.onValueChanged(quantity, null)
            onBindingValueChangedListener?.onChange()
        }

        editText.addTextChangedListener(this)
    }
}