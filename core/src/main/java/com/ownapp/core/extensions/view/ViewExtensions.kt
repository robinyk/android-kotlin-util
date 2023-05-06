package com.ownapp.core.extensions.view

import android.animation.LayoutTransition
import android.content.res.ColorStateList
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.annotation.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.widget.TooltipCompat
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.*
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.ownapp.core.BR
import com.ownapp.core.R
import com.ownapp.core.annotation.ResourceType
import com.ownapp.core.extensions.getEntryName
import com.ownapp.core.extensions.back
import com.ownapp.core.extensions.resource.*
import com.ownapp.core.extensions.screenRect
import com.ownapp.core.extensions.toActivity
import com.ownapp.core.extensions.utility.*
import com.ownapp.core.view.badge.TextBadgeDrawable
import com.ownapp.core.view.pager.SimpleViewPager2Adapter
import com.ownapp.core.view.recycler.generic.GenericRecyclerItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Updated by Robin on 2020/12/16
 */

val View.entryName: String?
    get() = context.getEntryName(id)

//**--------------------------------------------------------------------------------------------------
//*      Binding
//---------------------------------------------------------------------------------------------------*/
@BindingAdapter(value = ["itemLayout", "entries"], requireAll = true)
fun <T> ViewGroup.setEntries(@LayoutRes layoutId: Int, entries: List<T>?)
{
    if(layoutId.getResourceTypeName(context) == ResourceType.LAYOUT && layoutId.getEntryName(context) != null)
    {
        removeAllViews()

        entries?.forEachIndexed { index, entry ->
            AsyncLayoutInflater(context).inflate(layoutId, this) { view, _, _ ->
                DataBindingUtil.bind<ViewDataBinding>(view)?.let {
                    (entry as? GenericRecyclerItem)?.apply {
                        adapterPosition = index
                        isFirst = index == 0
                        isLast = index == entries.size - 1
                    }

                    it.setVariable(BR.item, entry)
                    addView(it.root)
                    it.executePendingBindings()
                }
            }
        }
    }
}

@BindingAdapter("background")
fun View.setBackground(obj: Any?)
{
    when(obj)
    {
        is Drawable -> background = obj
        is Bitmap -> background = obj.toDrawable(resources)
        is ColorStateList -> setBackgroundColor(obj.getEnabledColorOrDefault())
        is Int -> when(obj.getResourceTypeName(context))
        {
            ResourceType.COLOR, ResourceType.ATTRIBUTE -> setBackgroundColor(obj.toColorStateList(context).defaultColor)
            ResourceType.DRAWABLE -> setBackgroundResource(obj)
            else -> setBackgroundColor(obj)
        }
}
}

fun View.setTooltipTextCompat(obj: Any?)
{
    setTooltipTextCompat(when
    {
        obj is Int && obj.getResourceTypeName(context) == ResourceType.STRING -> context.getString(obj)
        obj is String -> obj
        else -> obj.toString()
    })
}

@BindingAdapter("tooltipText")
fun View.setTooltipTextCompat(text: String?)
{
    if(!text.isNullOrBlank())
    {
        TooltipCompat.setTooltipText(this, text)

        if(this is ImageView && contentDescription.isNullOrBlank())
            this.contentDescription = text
    }
}

//@BindingAdapter("colorFilter", "filterMode", "index", requireAll = false)
//fun TextView.setSelectedColorFilter(obj: Any?, mode: Int?, index: Int?)
//{
//    if(obj == null || obj !is Int)
//        return
//
//    if(index == null)
//    {
//        compoundDrawables.forEach { drawable ->
//            if(isSelected)
//            {
//                drawable.colorFilter = PorterDuffColorFilter(
//                    obj.toColorStateList(context).getEnabledColorOrDefault()
//                    , PorterDuff.Mode.SRC_IN
//                )
//            }
//            else drawable.clearColorFilter()
//        }
//    }
//    else
//    {
//        compoundDrawables.getOrNull(0)?.let { drawable ->
//            if(isSelected)
//            {
//                drawable.colorFilter = PorterDuffColorFilter(
//                    obj.toColorStateList(context).getEnabledColorOrDefault()
//                    , PorterDuff.Mode.SRC_IN
//                )
//            }
//            else drawable.clearColorFilter()
//        }
//    }
//}

@BindingAdapter("homeAsUp")
fun Toolbar.setHomeAsUp(isHomeAsUp: Boolean = true)
{
    if(isHomeAsUp)
        setNavigationOnClickListener { context.toActivity()?.onBackPressed() }
}

@BindingAdapter("homeAsUp")
fun Toolbar.setHomeAsUp(fragment: Fragment) = setNavigationOnClickListener { fragment.back() }

@BindingAdapter("homeAsUp")
fun Toolbar.setHomeAsUp(activity: AppCompatActivity) = setNavigationOnClickListener { activity.onBackPressed() }

@BindingAdapter("progressSpinnerColor")
fun SwipeRefreshLayout.setProgressSpinnerColor(obj: Any?)
{
    when(obj)
    {
        is ColorStateList -> setColorSchemeColors(obj.defaultColor)
        is Int -> when(obj.getResourceTypeName(context))
        {
            ResourceType.COLOR, ResourceType.ATTRIBUTE -> setColorSchemeColors(obj.toColor(context))
            else -> setColorSchemeColors(obj)
        }
    }
}


//**--------------------------------------------------------------------------------------------------
//*      Visibility
//---------------------------------------------------------------------------------------------------*/
fun View.show()
{
    isVisible = true
}

fun View.invisible()
{
    isInvisible = true
}

fun View.gone()
{
    isGone = true
}

@BindingAdapter(value = ["android:visibility", "invisible"], requireAll = false)
fun View.showIf(isVisible: Boolean?, isInvisible: Boolean? = false)
{
    visibility = if(isVisible == true)
        View.VISIBLE
    else
    {
        if(isInvisible == true)
            View.INVISIBLE
        else
            View.GONE
    }
}

/**
 * Hides all the views passed as argument(s)
 */
fun AppCompatActivity.hideViewAsync(vararg views: View) = lifecycleScope.launch { views.gone() }

/**
 * Shows all the views passed as argument(s)
 */
fun AppCompatActivity.showViewAsync(vararg views: View) = lifecycleScope.launch { views.show() }

fun Array<out View>.gone() = forEach { it.gone() }
fun Array<out View>.show() = forEach { it.show() }


//**--------------------------------------------------------------------------------------------------
//*      Dimension
//---------------------------------------------------------------------------------------------------*/
fun View.setMargin(@Px value: Int) = setMargin(value, value, value, value)

fun View.setMargin(
    @Px left: Int = marginLeft, @Px top: Int = marginTop, @Px right: Int = marginRight, @Px bottom: Int = marginBottom
) {
    (layoutParams as? ViewGroup.MarginLayoutParams)?.apply{
        setMargins(left, top, right, bottom)
        requestLayout()
    } ?: "Failed to set margins: $layoutParams of $entryName".logError()
}

@BindingAdapter("android:layout_marginTop")
fun View.setMarginTop(@Px margin: Float) = setMargin(top = margin.roundToInt())

@BindingAdapter("android:layout_marginBottom")
fun View.setMarginBottom(@Px margin: Float) = setMargin(bottom = margin.roundToInt())

@BindingAdapter("android:layout_marginStart")
fun View.setMarginStart(@Px margin: Float) = setMargin(left = margin.roundToInt())

@BindingAdapter("android:layout_marginEnd")
fun View.setMarginEnd(@Px margin: Float) = setMargin(right = margin.roundToInt())

fun View.aspect(ratio: Float = 9 / 16f) = post {
    val params = layoutParams
    params.height = (width / ratio).toInt()
    layoutParams = params
}


//fun View.setPadding(value: Int) = setPadding(value, value, value, value)


//**--------------------------------------------------------------------------------------------------
//*      Resource
//---------------------------------------------------------------------------------------------------*/
@get:ColorInt
val View.backgroundColor: Int
    get() = (background as? ColorDrawable)?.color ?: Color.TRANSPARENT

var ViewGroup.animateLayoutChanges: Boolean
    set(value)
    {
        layoutTransition = if (value)
            LayoutTransition().apply {
                disableTransitionType(LayoutTransition.DISAPPEARING)
            }
        else null
    }
    get() = layoutTransition != null

fun View.setAnimateLayoutChanges(isEnabled: Boolean)
{
    findViewGroupParent()?.animateLayoutChanges = isEnabled
}


//**--------------------------------------------------------------------------------------------------
//*      UI
//---------------------------------------------------------------------------------------------------*/
val View?.isVisibleOnScreen: Boolean
    get() = this?.isShown == true && Rect().apply { getGlobalVisibleRect(this) }.intersect(context.screenRect)

@BindingAdapter("android:layout_height")
fun View.setHeight(@Px newValue: Float)
{
    val params = layoutParams
    params?.let {
        params.height = newValue.roundToInt()
        layoutParams = params
    }
}

@BindingAdapter("android:layout_width")
fun View.setWidth(@Px newValue: Float)
{
    val params = layoutParams
    params?.let {
        params.width = newValue.roundToInt()
        layoutParams = params
    }
}

fun View.findViewInParent(@IdRes resId: Int): View?
{
    return if(parent is ViewGroup)
        (parent as ViewGroup).findViewById(resId) ?: (parent as ViewGroup).findViewInParent(resId)
    else null
}

fun View.findViewGroupParent(): ViewGroup? = when
{
    this is ViewGroup -> this
    parent != null -> (parent as View).findViewGroupParent()
    else -> null
}

inline fun <reified T : ViewParent> View.findParentOfType(): T? {
    return findParentOfType(T::class.java)
}

fun <T : ViewParent> View.findParentOfType(type: Class<T>): T? {
    var p = parent
    while (p != null) {
        if (type.isInstance(p)) {
            return type.cast(p)
        }
        p = p.parent
    }
    return null
}

fun View.setScaledBackground(@DrawableRes drawableId: Int)
{
    background = BitmapDrawable(resources, BitmapFactory.decodeResource(resources, drawableId, BitmapFactory.Options().apply { inScaled = false }))
}

fun View.blockMultipleClick(timeMillis: Long = 500)
{
    if(isClickable)
    {
        isClickable = false
        GlobalScope.launchDelay(timeMillis) { isClickable = true }
    }
}

fun View.disableClipOnParents()
{
    if(this is ViewGroup)
    {
        clipToPadding = false
        clipChildren = false
    }

    (parent as? View)?.disableClipOnParents()
}

fun View.setBadge(obj: Any? = "", drawableReference: Drawable? = null)
{
    doOnLayout {
        GlobalScope.launch(Dispatchers.Main) {
            it.disableClipOnParents()

            val badgeDrawable = TextBadgeDrawable.create(context)
            val backgroundDrawable = TextBadgeDrawable.create(context)
            val badgeFrameSize = R.attr.badgeFrameSize.toDimensionPixel(context) ?: 1.dp

            Rect().let { rect ->
                it.getDrawingRect(rect)
                badgeDrawable.bounds = rect
                backgroundDrawable.bounds = rect

                if(drawableReference != null)
                {
                    badgeDrawable.horizontalOffset = ((rect.width() - drawableReference.intrinsicWidth) * 0.5f).roundToInt()
                    badgeDrawable.verticalOffset = ((rect.height() - drawableReference.intrinsicHeight) * 0.5f).roundToInt()
                }
                else
                {
                    badgeDrawable.horizontalOffset = (
                        (rect.width() - it.layoutParams.width - it.paddingLeft - it.paddingRight + badgeFrameSize) * 0.5f
                    ).roundToInt()

                    badgeDrawable.verticalOffset = (
                        (rect.height() - it.layoutParams.height - it.paddingTop - it.paddingBottom + badgeFrameSize) * 0.25f
                    ).roundToInt()
                }

                when(it)
                {
                    is ImageView -> if(it.drawable != null)
                    {
                        badgeDrawable.horizontalOffset = ((rect.width() - it.drawable.intrinsicWidth) * 0.5f).roundToInt()
                        badgeDrawable.verticalOffset = ((rect.height() - it.drawable.intrinsicHeight) * 0.5f).roundToInt()
                    }

                    is ImageButton -> if(it.drawable != null)
                    {
                        badgeDrawable.horizontalOffset = ((rect.width() - it.drawable.intrinsicWidth) * 0.5f).roundToInt()
                        badgeDrawable.verticalOffset = ((rect.height() - it.drawable.intrinsicHeight) * 0.5f).roundToInt()
                    }
                }

                backgroundDrawable.horizontalOffset = badgeDrawable.horizontalOffset
                backgroundDrawable.verticalOffset = badgeDrawable.verticalOffset
            }

            backgroundDrawable.apply {
                updateBadgeCoordinates(it, null)
                text = obj?.toString() ?: ""
                padding = badgeFrameSize

                (it.parent as View).backgroundColor.let { color ->
                    badgeTextColor = color
                    backgroundColor = color
                }
            }

            badgeDrawable.apply {
                updateBadgeCoordinates(it, null)
                text = obj?.toString() ?: ""
                insetPadding = R.attr.badgeFrameSize.toDimensionPixel(context) ?: 1.dp
            }

            when(obj)
            {
                is Int ->
                {
                    backgroundDrawable.number = obj
                    badgeDrawable.number = obj
                }

                else ->
                {
                    (obj?.toString() ?: "").let {
                        backgroundDrawable.text = it
                        badgeDrawable.text = it
                    }
                }
            }

            it.overlay.clear()
            it.overlay.add(LayerDrawable(arrayOf(backgroundDrawable, badgeDrawable)))
        }
    }
}

fun View.drawToBitmap(): Bitmap?
{
    measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

    Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888).let {
        Canvas(it).let { canvas ->
            layout(0, 0, measuredWidth, measuredHeight)
            draw(canvas)
            return it
        }
    }
}

/**
 * Displays a popup by inflating menu with specified
 * [menu resource id][menuResourceId], calling [onClick] when an item
 * is clicked, and optionally calling [onInit] with
 * [PopupMenu] as receiver to initialize prior to display.
 */
fun View.showPopup(
    @MenuRes menuResourceId: Int,
    onInit: PopupMenu.() -> Unit = {},
    onClick: (MenuItem) -> Boolean
) {
    PopupMenu(context, this).apply {
        menuInflater.inflate(menuResourceId, menu)
        onInit(this)
        setOnMenuItemClickListener(onClick)
    }.show()
}


//**--------------------------------------------------------------------------------------------------
//*      Etc
//---------------------------------------------------------------------------------------------------*/
fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View = LayoutInflater.from(context).inflate(layoutRes, this, false)

val Toolbar.navigationIconView: ImageButton?
    get()
    {
        //check if contentDescription previously was set
        val hadContentDescription = !TextUtils.isEmpty(navigationContentDescription)
        val contentDescription = if(hadContentDescription) navigationContentDescription else "navigationIcon"
        navigationContentDescription = contentDescription

        val potentialViews = arrayListOf<View>()

        //find the view based on it's content description, set programatically or with android:contentDescription
        findViewsWithText(potentialViews, contentDescription, View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION)

        //Clear content description if not previously present
        if (!hadContentDescription)
            navigationContentDescription = null

        //Nav icon is always instantiated at this point because calling setNavigationContentDescription ensures its existence
        return potentialViews.firstOrNull() as? ImageButton
    }

@BindingAdapter("autoScroll")
fun ViewPager.autoScroll(interval: Long = 5000L)
{
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run()
        {
            val count = adapter?.count ?: 0
            setCurrentItem((currentItem + 1) % count, true)

            handler.postDelayed(this, interval)
        }
    }

    setOnTouchListener { view, event ->
        if(event?.actionMasked == MotionEvent.ACTION_DOWN)
        {
            handler.removeCallbacks(runnable)
        }
        else if(event?.action == MotionEvent.ACTION_UP)
        {
            handler.postDelayed(runnable, interval)
        }

        view?.performClick() == true
    }

    handler.postDelayed(runnable, interval)
}

@BindingAdapter("autoScroll")
fun ViewPager2.autoScroll(interval: Long = 5000L)
{
    val handler = Handler(Looper.getMainLooper())
    val runnable = object : Runnable {
        override fun run()
        {
            val count = adapter?.itemCount ?: 0
            setCurrentItem((currentItem + 1) % count, true)

            handler.postDelayed(this, interval)
        }
    }

    setOnTouchListener { view, event ->
        if(event?.actionMasked == MotionEvent.ACTION_DOWN)
        {
            handler.removeCallbacks(runnable)
        }
        else if(event?.action == MotionEvent.ACTION_UP)
        {
            handler.postDelayed(runnable, interval)
        }

        view?.performClick() == true
    }

    handler.postDelayed(runnable, interval)
}

fun ViewPager.toPrevious(animate: Boolean = true)
{
    if(adapter != null && currentItem > 0)
        setCurrentItem(currentItem - 1, animate)
}
fun ViewPager.toNext(animate: Boolean = true)
{
    if(adapter != null && currentItem < adapter!!.count - 1)
        setCurrentItem(currentItem + 1, animate)
}

fun ViewPager2.setupSimpleTab(tabLayout: TabLayout, adapter: SimpleViewPager2Adapter)
{
    TabLayoutMediator(tabLayout, this) { tab, position ->
        tab.text = adapter.getTitle(position)
    }.attach()
}

fun ViewPager2.setup(adapter: SimpleViewPager2Adapter, tabLayout: TabLayout? = null)
{
    this.adapter = adapter
    offscreenPageLimit = adapter.fragments.size
    
    if(tabLayout != null)
        setupSimpleTab(tabLayout, adapter)
}

val ViewPager.isOnLastPage: Boolean
    get() = currentItem == adapter?.count?.minus(1)

fun ShimmerFrameLayout.start()
{
    show()
    showShimmer(true)
}

fun ShimmerFrameLayout.stop()
{
    gone()
    hideShimmer()
}

fun SwipeRefreshLayout.enableRefresh(isEnabled: Boolean = true)
{
    setDistanceToTriggerSync(if(isEnabled)
        SwipeRefreshLayout.DEFAULT_SLINGSHOT_DISTANCE
    else 99999)
}

val SearchView?.getEditTextSearchView get() = this?.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)

/**
 * Attaches a listener to the recyclerview to hide the fab when it is scrolling downwards
 * The fab will reappear when scrolling has stopped or if the user scrolls up
 */
fun FloatingActionButton.hideOnScrollingDown(recycler: RecyclerView)
{
    recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if (newState == RecyclerView.SCROLL_STATE_IDLE && !isShown) show()
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0 && isShown) hide()
            else if (dy < 0 && isOrWillBeHidden) show()
        }
    })
}

/**
 * Do action and close drawer
 */
inline fun DrawerLayout.consume(gravity: Int = GravityCompat.START, action: () -> Unit): Boolean
{
    action()
    this.closeDrawer(gravity)
    return true
}

fun ConstraintLayout.addConstraints(block: ConstraintSet.() -> Unit)
{
   ConstraintSet().let {
        it.clone(this)
        block(it)
        it.applyTo(this)
    }
}

fun Menu.changeMenuIconColor(@ColorInt color: Int) {
    for (i in 0 until this.size()) {
        val drawable = this.getItem(i).icon
        drawable?.apply {
            mutate()
            colorFilter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                BlendModeColorFilter(color, BlendMode.SRC_ATOP)
            } else {
                PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
            }
        }
    }
}

fun ProgressBar.tint(@ColorInt color: Int, skipIndeterminate: Boolean = false)
{
    context.getColors(color).let {
        progressTintList = it
        secondaryProgressTintList = it
        if (!skipIndeterminate) indeterminateTintList = it
    }
}

fun SeekBar.tint(@ColorInt color: Int)
{
    context.getColors(color).let {
        thumbTintList = it
        progressTintList = it
    }
}

fun Toolbar.tint(@ColorInt color: Int)
{
    (0 until childCount).asSequence().forEach { (getChildAt(it) as? ImageButton)?.setColorFilter(color) }
}

fun Toolbar.getMenuItemView(@IdRes menuItemId: Int): View?
{
    tryOrIgnore {
        Toolbar::class.java.getDeclaredField("mMenuView").let {
            it.isAccessible = true

            (it.get(this) as ViewGroup).children.forEach { menuItemView ->
                if(menuItemView.id == menuItemId)
                    return menuItemView
            }
        }
    }

    return null
}

@BindingAdapter("navigationIcon")
fun Toolbar.setNavIcon(obj: Any?)
{
    when(obj)
    {
        is Drawable -> navigationIcon = obj
        is Int -> when(obj.getResourceTypeName(context))
        {
            ResourceType.DRAWABLE -> setNavigationIcon(obj)
            ResourceType.ATTRIBUTE -> navigationIcon = obj.toDrawable(context)
        }
    }
}
