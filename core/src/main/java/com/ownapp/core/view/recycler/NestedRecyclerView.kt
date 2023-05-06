package com.ownapp.core.view.recycler

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.doOnAttach
import androidx.core.view.doOnLayout
import androidx.databinding.BindingAdapter
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.*
import com.google.android.material.theme.overlay.MaterialThemeOverlay
import com.ownapp.core.R
import com.ownapp.core.extensions.utility.cast
import com.ownapp.core.extensions.utility.log
import com.ownapp.core.extensions.utility.logError
import com.ownapp.core.extensions.utility.logWarn
import com.ownapp.core.extensions.view.*
import com.ownapp.core.util.ViewUtil
import com.ownapp.core.view.recycler.NestedRecyclerView.BindingAdapters.setEmptyView
import com.ownapp.core.view.recycler.NestedRecyclerView.BindingAdapters.setMaxRatio
import com.ownapp.core.view.recycler.NestedRecyclerView.BindingAdapters.setMinRatio
import com.ownapp.core.view.recycler.NestedRecyclerView.BindingAdapters.setRatio
import kotlin.math.max
import kotlin.math.min

class NestedRecyclerView: RecyclerView
{
    //**--------------------------------------------------------------------------------------------------
    //*      Binding
    //---------------------------------------------------------------------------------------------------*/
    object BindingAdapters
    {
        @BindingAdapter("emptyViewId")
        @JvmStatic fun NestedRecyclerView.setEmptyView(obj: Any?)
        {
            when(obj)
            {
                is Int -> findViewInParent(obj)?.let { emptyView = it }
                is View -> emptyView = obj
                is ViewDataBinding -> emptyView = obj.root
                else -> "Failed to set empty view with => $obj".logError()
            }
        }
    
        @BindingAdapter("isScrollable")
        @JvmStatic fun NestedRecyclerView.setScrollable(isEnabled: Boolean)
        {
            isScrollable = isEnabled
        }
    
        @BindingAdapter("showEmpty")
        @JvmStatic fun NestedRecyclerView.setShowEmpty(isEnabled: Boolean)
        {
            isShowEmpty = isEnabled
        }
    
        @BindingAdapter("ratio")
        @JvmStatic fun NestedRecyclerView.setRatio(value: String?)
        {
            if(!value.isNullOrBlank())
            {
                if(value.contains(":"))
                {
                    value.split(":").let {
                        ratio = (it.getOrNull(0)?.toFloatOrNull() ?: 0f) to (it.getOrNull(1)?.toFloatOrNull() ?: 0f)
                    }
                }
                else "Ratio doesn't contain correct format => $value".logError()
            }
        }
    
        @BindingAdapter("minRatio")
        @JvmStatic fun NestedRecyclerView.setMinRatio(value: String?)
        {
            if(!value.isNullOrBlank())
            {
                if(value.contains(":"))
                {
                    value.split(":").let {
                        minRatio = (it.getOrNull(0)?.toFloatOrNull() ?: 0f) to (it.getOrNull(1)?.toFloatOrNull() ?: 0f)
                    }
                }
                else "Ratio doesn't contain correct format => $value".logError()
            }
        }
        
        @BindingAdapter("maxRatio")
        @JvmStatic fun NestedRecyclerView.setMaxRatio(value: String?)
        {
            if(!value.isNullOrBlank())
            {
                if(value.contains(":"))
                {
                    value.split(":").let {
                        maxRatio = (it.getOrNull(0)?.toFloatOrNull() ?: 0f) to (it.getOrNull(1)?.toFloatOrNull() ?: 0f)
                    }
                }
                else "Ratio doesn't contain correct format => $value".logError()
            }
        }
    }
    
    
    //**--------------------------------------------------------------------------------------------------
    //*      Class
    //---------------------------------------------------------------------------------------------------*/
    private val observer: AdapterDataObserver = object : AdapterDataObserver()
    {
        override fun onChanged()
        {
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int)
        {
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int)
        {
            checkIfEmpty()
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Variable
    //---------------------------------------------------------------------------------------------------*/
    // View
    private var nestedScrollTarget: View? = null

    var emptyView: View? = null
        set(value)
        {
            field = value

            if(adapter != null)
                checkIfEmpty()
        }
    
    // Class
    var pagerSnapHelper: PagerSnapHelper? = null
    var pagerSnapListener: SnapPagerScrollListener.OnChangeListener? = null
        set(listener)
        {
            if(field != listener)
            {
                field = listener
                
                if(listener != null)
                    pagerSnapHelper = setPagerSnapListener(listener, type = snapType)
            }
        }
    var snapType: Int = SnapPagerScrollListener.ON_SETTLED
    
    // Value
    private var nestedScrollTargetIsBeingDragged = false
    private var nestedScrollTargetWasUnableToScroll = false
    private var skipsTouchInterception = false
    private var isInitialized = false

    var isShowEmpty: Boolean = false
        get() = field && (adapter?.let { it.itemCount == 0 } ?: true)
        set(value)
        {
            field = value
            checkIfEmpty()
        }

    var isScrollable: Boolean = true

    val snapPosition: Int
        get() = getSnapPosition(pagerSnapHelper)
    
    var ratio: Pair<Float, Float>? = null
        set(value)
        {
            if(field != value)
            {
                field = value
                requestLayout()
                invalidate()
            }
        }
    
    var minRatio: Pair<Float, Float>? = null
        set(value)
        {
            if(field != value)
            {
                field = value
                requestLayout()
                invalidate()
            }
        }
    
    var maxRatio: Pair<Float, Float>? = null
        set(value)
        {
            if(field != value)
            {
                field = value
                requestLayout()
                invalidate()
            }
        }
    
    

    //**--------------------------------------------------------------------------------------------------
    //*      Constructor
    //---------------------------------------------------------------------------------------------------*/
    constructor(context: Context): this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, R.attr.nestedRecyclerStyle, R.style.Ownapp_Style_RecyclerView)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int)
            : super(MaterialThemeOverlay.wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr)
    {
        if(attrs != null)
        {
            context.obtainStyledAttributes(attrs, R.styleable.NestedRecyclerView, defStyleAttr, defStyleRes).run {
                getInt(R.styleable.NestedRecyclerView_emptyViewId, R.id.empty_recycler_container).let {
                    parent.cast<View>()?.doOnLayout { setEmptyView(it) }
                }
    
                if(hasValue(R.styleable.NestedRecyclerView_showEmpty))
                    isShowEmpty = getBoolean(R.styleable.NestedRecyclerView_showEmpty, false)
    
                if(hasValue(R.styleable.NestedRecyclerView_isScrollable))
                    isScrollable = getBoolean(R.styleable.NestedRecyclerView_isScrollable, true)
    
                if(hasValue(R.styleable.NestedRecyclerView_pagerSnap))
                {
                    snapType = getInt(R.styleable.NestedRecyclerView_pagerSnap, SnapPagerScrollListener.ON_SETTLED)
                    pagerSnapHelper = setPagerSnapListener(pagerSnapListener, type = snapType)
                }
    
                if(hasValue(R.styleable.NestedRecyclerView_spacingDecor))
                    addItemDecoration(SpacingItemDecoration(getDimensionPixelSize(R.styleable.NestedRecyclerView_spacingDecor, 0)))
    
                if(hasValue(R.styleable.NestedRecyclerView_dividerDecor))
                {
                    when(getInt(R.styleable.NestedRecyclerView_dividerDecor, -1))
                    {
                        0 -> DividerItemDecoration.VERTICAL
                        1 -> DividerItemDecoration.HORIZONTAL
                        else -> null
                    }?.let { addItemDecoration(DividerItemDecoration(context, it)) }
                }
    
                if(hasValue(R.styleable.NestedRecyclerView_ratio))
                    setRatio(getString(R.styleable.NestedRecyclerView_ratio))
    
                if(hasValue(R.styleable.NestedRecyclerView_minRatio))
                    setMinRatio(getString(R.styleable.NestedRecyclerView_minRatio))
    
                if(hasValue(R.styleable.NestedRecyclerView_maxRatio))
                    setMaxRatio(getString(R.styleable.NestedRecyclerView_maxRatio))
    
                recycle()
            }
        }
        
        itemAnimator = DefaultItemAnimator()

        doOnAttach { it.setAnimateLayoutChanges(false) }
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Public
    //---------------------------------------------------------------------------------------------------*/
    fun checkIfEmpty()
    {
        emptyView?.let {
            it.showIf(isShowEmpty)
            this.showIf(!isShowEmpty)
        } ?: "No empty view found".logWarn()
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Override
    //---------------------------------------------------------------------------------------------------*/
    override fun setAdapter(adapter: Adapter<*>?)
    {
        getAdapter()?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        
        if(isInitialized)
            checkIfEmpty()
        else isInitialized = true
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean
    {
        val temporarilySkipsInterception = nestedScrollTarget != null
        if (temporarilySkipsInterception)
        {
            // If a descendent view is scrolling we set a flag to temporarily skip our onInterceptTouchEvent implementation
            skipsTouchInterception = true
        }

        // First dispatch, potentially skipping our onInterceptTouchEvent
        var handled = super.dispatchTouchEvent(ev)
        if (temporarilySkipsInterception)
        {
            skipsTouchInterception = false

            // If the first dispatch yielded no result or we noticed that the descendent view is unable to scroll in the
            // direction the user is scrolling, we dispatch once more but without skipping our onInterceptTouchEvent.
            // Note that RecyclerView automatically cancels active touches of all its descendents once it starts scrolling
            // so we don't have to do that.
            if (!handled || nestedScrollTargetWasUnableToScroll) handled = super.dispatchTouchEvent(ev)
        }
        return handled
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean
    {
        // Skips RecyclerView's onInterceptTouchEvent if requested
        return !skipsTouchInterception && super.onInterceptTouchEvent(e)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int)
    {
        if (target === nestedScrollTarget && !nestedScrollTargetIsBeingDragged)
        {
            if (dyConsumed != 0)
            {
                // The descendent was actually scrolled, so we won't bother it any longer.
                // It will receive all future events until it finished scrolling.
                nestedScrollTargetIsBeingDragged = true
                nestedScrollTargetWasUnableToScroll = false
            }
            else if (dyConsumed == 0 && dyUnconsumed != 0)
            {
                // The descendent tried scrolling in response to touch movements but was not able to do so.
                // We remember that in order to allow RecyclerView to take over scrolling.
                nestedScrollTargetWasUnableToScroll = true
                target.parent.requestDisallowInterceptTouchEvent(false)
            }
        }
    }

    override fun onNestedScrollAccepted(child: View?, target: View, axes: Int)
    {
        if (axes != 0 && View.SCROLL_AXIS_VERTICAL != 0)
        {
            // A descendent started scrolling, so we'll observe it.
            nestedScrollTarget = target
            nestedScrollTargetIsBeingDragged = false
            nestedScrollTargetWasUnableToScroll = false
        }
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onStartNestedScroll(child: View?, target: View?, nestedScrollAxes: Int): Boolean
    {
        // We only support vertical scrolling
        return nestedScrollAxes != 0 && View.SCROLL_AXIS_VERTICAL != 0
    }

    override fun onStopNestedScroll(child: View?)
    {
        // The descendent finished scrolling. Clean up!
        nestedScrollTarget = null
        nestedScrollTargetIsBeingDragged = false
        nestedScrollTargetWasUnableToScroll = false
    }

    override fun canScrollVertically(direction: Int): Boolean = isScrollable && super.canScrollVertically(direction)
    override fun canScrollHorizontally(direction: Int): Boolean = isScrollable && super.canScrollHorizontally(direction)
    
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
    
        val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight
        val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom
    
        var calculatedWidth = ViewUtil.measureDimension(max(desiredWidth, measuredWidth), widthMeasureSpec)
        var calculatedHeight = ViewUtil.measureDimension(max(desiredHeight, measuredHeight), heightMeasureSpec)
        
        if (widthMode == MeasureSpec.EXACTLY && (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED))
        {
            val ratioHeight = ratio?.let { (calculatedWidth.toDouble() / it.first * it.second).toInt() } ?: 0
            val maxRatioHeight = maxRatio?.let { (calculatedWidth.toDouble() / it.first * it.second).toInt() } ?: 0
            
            minRatio?.let { minimumHeight = (calculatedWidth.toDouble() / it.first * it.second).toInt() }

            if(ratioHeight != 0)
            {
                calculatedHeight = if(maxRatioHeight != 0)
                    min(desiredHeight, maxRatioHeight)
                else ratioHeight
            }
            else if(maxRatioHeight != 0 && calculatedHeight > maxRatioHeight)
                calculatedHeight = maxRatioHeight
    
            setMeasuredDimension(calculatedWidth, calculatedHeight)
        }
        else if (heightMode == MeasureSpec.EXACTLY && (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED))
        {
            val ratioWidth = ratio?.let { (calculatedHeight.toDouble() / it.second * it.first).toInt() } ?: 0
            val maxRatioWidth = maxRatio?.let { (calculatedHeight.toDouble() / it.second * it.first).toInt() } ?: 0
            
            minRatio?.let { minimumWidth = (calculatedHeight.toDouble() / it.second * it.first).toInt() }
            
            if(ratioWidth != 0)
            {
                calculatedWidth = if(maxRatioWidth != 0)
                    min(desiredWidth, maxRatioWidth)
                else ratioWidth
            }
            else if(maxRatioWidth != 0 && calculatedWidth > maxRatioWidth)
                calculatedWidth = maxRatioWidth
    
            setMeasuredDimension(calculatedWidth, calculatedHeight)
        }
        
        // measureChildren(MeasureSpec.makeMeasureSpec(calculatedWidth, MeasureSpec.getMode(widthMeasureSpec)),
        //     MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.getMode(heightMeasureSpec)))
    }
}