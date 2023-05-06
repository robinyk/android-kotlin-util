package com.ownapp.core.view.badge

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.*
import androidx.core.view.ViewCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.ownapp.core.R
import com.ownapp.core.extensions.resource.toColor
import com.ownapp.core.extensions.resource.upperCased
import com.ownapp.core.view.badge.resource.MaterialResources
import com.ownapp.core.view.badge.typeface.TextAppearance
import com.ownapp.core.view.badge.util.BadgeUtils
import com.ownapp.core.view.badge.util.DrawableUtils
import com.ownapp.core.view.badge.util.TextDrawableHelper
import com.ownapp.core.view.badge.util.ThemeEnforcement
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.max

/**
 * BadgeDrawable contains all the layout and draw logic for a badge.
 *
 *
 * You can use `BadgeDrawable` to display dynamic information such as a number of pending
 * requests in a [com.google.android.material.bottomnavigation.BottomNavigationView]. To
 * create an instance of `BadgeDrawable`, use [.create] or [ ][.createFromResources]. How to add and display a `BadgeDrawable` on top of its
 * anchor view depends on the API level:
 *
 *
 * For API 18+ (APIs supported by [android.view.ViewOverlay])
 *
 *
 *  * Add `BadgeDrawable` as a [android.view.ViewOverlay] to the desired anchor view
 * using BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout) (This helper class
 * is currently package private).
 *  * Update the `BadgeDrawable BadgeDrawable's` coordinates (center and bounds) based on
 * its anchor view using [.updateBadgeCoordinates].
 *
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, null);
</pre> *
 *
 *
 * For Pre API-18
 *
 *
 *  * Set `BadgeDrawable` as the foreground of the anchor view's FrameLayout ancestor using
 * BadgeUtils#attachBadgeDrawable(BadgeDrawable, View, FrameLayout) (This helper class is
 * currently package private).
 *  * Update the `BadgeDrawable BadgeDrawable's` coordinates (center and bounds) based on
 * its anchor view (relative to its FrameLayout ancestor's coordinate space), using [       ][.updateBadgeCoordinates].
 *
 *
 * <pre>
 * BadgeDrawable badgeDrawable = BadgeDrawable.create(context);
 * BadgeUtils.attachBadgeDrawable(badgeDrawable, anchor, anchorFrameLayoutParent);
</pre> *
 *
 *
 * By default, `BadgeDrawable` is aligned to the top and end edges of its anchor view (with
 * some offsets). Call #setBadgeGravity(int) to change it to one of the other supported modes.
 *
 *
 * Note: This is still under development and may not support the full range of customization
 * Material Android components generally support (e.g. themed attributes).
 */
class TextBadgeDrawable private constructor(context: Context): Drawable(), TextDrawableHelper.TextDrawableDelegate
{
	/** Position the badge can be set to.  */
	@IntDef(TOP_END, TOP_START, BOTTOM_END, BOTTOM_START)
	@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
	annotation class BadgeGravity

	private val contextRef: WeakReference<Context> = WeakReference(context)
	private val shapeDrawable: MaterialShapeDrawable
	private val textDrawableHelper: TextDrawableHelper
	private val badgeBounds: Rect
	private val badgeRadius: Float
	private val badgeWithTextRadius: Float
	private val badgeWidePadding: Float
	val savedState: SavedState
	private var badgeCenterX = 0f
	private var badgeCenterY = 0f
	private var maxBadgeNumber = 0
	private var cornerRadius = 0f
	private var halfBadgeWidth = 0f
	private var halfBadgeHeight = 0f

	// Need to keep a local reference in order to support updating badge gravity.
	private var anchorViewRef: WeakReference<View?>? = null
	private var customBadgeParentRef: WeakReference<ViewGroup?>? = null

	/**
	 * A [Parcelable] implementation used to ensure the state of BadgeDrawable is saved.
	 *
	 * @hide
	 */
	@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
	class SavedState: Parcelable
	{
		@ColorInt
		var backgroundColor = 0

		@ColorInt
		var badgeTextColor: Int
		var alpha = 255
		var number = BADGE_NUMBER_NONE
		var maxCharacterCount = 0
		var contentDescriptionNumberless: CharSequence?

		@PluralsRes
		var contentDescriptionQuantityStrings: Int

		@StringRes
		var contentDescriptionExceedsMaxBadgeNumberRes = 0

		@BadgeGravity
		var badgeGravity = 0

		@Dimension(unit = Dimension.PX)
		var horizontalOffset = 0

		@Dimension(unit = Dimension.PX)
		var verticalOffset = 0

		@Dimension(unit = Dimension.PX)
		var padding = 0

		@Dimension(unit = Dimension.PX)
		var insetPadding = 0


		constructor(context: Context)
		{
			// If the badge text color attribute was not explicitly set, use the text color specified in
			// the TextAppearance.
//			val textAppearance = TextAppearance(context, R.style.TextAppearance_MaterialComponents_Badge)
			badgeTextColor = R.attr.colorOnNotification.toColor(context)
			contentDescriptionNumberless = context.getString(R.string.mtrl_badge_numberless_content_description)
			contentDescriptionQuantityStrings = R.plurals.mtrl_badge_content_description
			contentDescriptionExceedsMaxBadgeNumberRes = R.string.mtrl_exceed_max_badge_number_content_description
		}

		protected constructor(`in`: Parcel)
		{
			backgroundColor = `in`.readInt()
			badgeTextColor = `in`.readInt()
			alpha = `in`.readInt()
			number = `in`.readInt()
			maxCharacterCount = `in`.readInt()
			contentDescriptionNumberless = `in`.readString()
			contentDescriptionQuantityStrings = `in`.readInt()
			badgeGravity = `in`.readInt()
			horizontalOffset = `in`.readInt()
			verticalOffset = `in`.readInt()
		}

		override fun describeContents(): Int
		{
			return 0
		}

		override fun writeToParcel(dest: Parcel, flags: Int)
		{
			dest.writeInt(backgroundColor)
			dest.writeInt(badgeTextColor)
			dest.writeInt(alpha)
			dest.writeInt(number)
			dest.writeInt(maxCharacterCount)
			dest.writeString(contentDescriptionNumberless.toString())
			dest.writeInt(contentDescriptionQuantityStrings)
			dest.writeInt(badgeGravity)
			dest.writeInt(horizontalOffset)
			dest.writeInt(verticalOffset)
		}

		companion object
		{
			val CREATOR: Parcelable.Creator<SavedState> = object: Parcelable.Creator<SavedState>
			{
				override fun createFromParcel(`in`: Parcel): SavedState
				{
					return SavedState(`in`)
				}

				override fun newArray(size: Int): Array<SavedState?>
				{
					return arrayOfNulls(size)
				}
			}
		}
	}

	/**
	 * Convenience wrapper method for [Drawable.setVisible] with the `restart` parameter hardcoded to false.
	 */
	fun setVisible(visible: Boolean)
	{
		setVisible(visible,  /* restart= */false)
	}

	private fun restoreFromSavedState(savedState: SavedState)
	{
		maxCharacterCount = savedState.maxCharacterCount

		// Only set the badge number if it exists in the style.
		// Defaulting it to 0 means the badge will incorrectly show text when the user may want a
		// numberless badge.
		if(savedState.number != BADGE_NUMBER_NONE)
		{
			number = savedState.number
		}
		backgroundColor = savedState.backgroundColor

		// Only set the badge text color if this attribute has explicitly been set, otherwise use the
		// text color specified in the TextAppearance.
		badgeTextColor = savedState.badgeTextColor
		badgeGravity = savedState.badgeGravity
		horizontalOffset = savedState.horizontalOffset
		verticalOffset = savedState.verticalOffset
	}

	private fun loadDefaultStateFromAttributes(
		context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int
	)
	{
		val a = context.obtainStyledAttributes(
			attrs, R.styleable.Badge, defStyleAttr, defStyleRes
		)
		maxCharacterCount = a.getInt(R.styleable.Badge_maxCharacterCount, DEFAULT_MAX_BADGE_CHARACTER_COUNT)

		// Only set the badge number if it exists in the style.
		// Defaulting it to 0 means the badge will incorrectly show text when the user may want a
		// numberless badge.
		if(a.hasValue(R.styleable.Badge_number))
		{
			number = a.getInt(R.styleable.Badge_number, 0)
		}
		backgroundColor = readColorFromAttributes(context, a, R.styleable.Badge_backgroundColor)

		// Only set the badge text color if this attribute has explicitly been set, otherwise use the
		// text color specified in the TextAppearance.
		if(a.hasValue(R.styleable.Badge_badgeTextColor))
		{
			badgeTextColor = readColorFromAttributes(context, a, R.styleable.Badge_badgeTextColor)
		}
		badgeGravity = a.getInt(R.styleable.Badge_badgeGravity, TOP_END)
		horizontalOffset = a.getDimensionPixelOffset(R.styleable.Badge_horizontalOffset, 0)
		verticalOffset = a.getDimensionPixelOffset(R.styleable.Badge_verticalOffset, 0)
		textAllCaps = a.getBoolean(R.styleable.Badge_android_textAllCaps, false)
		a.recycle()
	}

	/**
	 * Calculates and updates this badge's center coordinates based on its anchor's bounds. Internally
	 * also updates this BadgeDrawable's bounds, because they are dependent on the center coordinates.
	 * For pre API-18, coordinates will be calculated relative to `customBadgeParent` because
	 * the BadgeDrawable will be set as the parent's foreground.
	 *
	 * @param anchorView This badge's anchor.
	 * @param customBadgeParent An optional parent view that will set this BadgeDrawable as its
	 * foreground.
	 */
	fun updateBadgeCoordinates(
		anchorView: View, customBadgeParent: ViewGroup?
	)
	{
		anchorViewRef = WeakReference(anchorView)
		customBadgeParentRef = WeakReference(customBadgeParent)
		updateCenterAndBounds()
		invalidateSelf()
	}
	/**
	 * Returns this badge's background color.
	 *
	 * @see .setBackgroundColor
	 * @attr ref com.google.android.material.R.styleable#Badge_backgroundColor
	 */
	/**
	 * Sets this badge's background color.
	 *
	 * @param backgroundColor This badge's background color.
	 * @attr ref com.google.android.material.R.styleable#Badge_backgroundColor
	 */
	@get:ColorInt
	var backgroundColor: Int
		get() = shapeDrawable.fillColor!!.defaultColor
		set(backgroundColor)
		{
			savedState.backgroundColor = backgroundColor
			val backgroundColorStateList = ColorStateList.valueOf(backgroundColor)
			if(shapeDrawable.fillColor !== backgroundColorStateList)
			{
				shapeDrawable.fillColor = backgroundColorStateList
				invalidateSelf()
			}
		}
	/**
	 * Returns this badge's text color.
	 *
	 * @see .setBadgeTextColor
	 * @attr ref com.google.android.material.R.styleable#Badge_badgeTextColor
	 */
	/**
	 * Sets this badge's text color.
	 *
	 * @param badgeTextColor This badge's text color.
	 * @attr ref com.google.android.material.R.styleable#Badge_badgeTextColor
	 */
	@get:ColorInt
	var badgeTextColor: Int
		get() = textDrawableHelper.textPaint.color
		set(badgeTextColor)
		{
			savedState.badgeTextColor = badgeTextColor
			if(textDrawableHelper.textPaint.color != badgeTextColor)
			{
				textDrawableHelper.textPaint.color = badgeTextColor
				invalidateSelf()
			}
		}

	/** Returns whether this badge will display a number.  */
	fun hasNumber(): Boolean
	{
		return savedState.number != BADGE_NUMBER_NONE
	}
	/**
	 * Returns this badge's number. Only non-negative integer numbers will be returned because the
	 * setter clamps negative values to 0.
	 *
	 *
	 * WARNING: Do not call this method if you are planning to compare to BADGE_NUMBER_NONE
	 *
	 * @see .setNumber
	 * @attr ref com.google.android.material.R.styleable#Badge_number
	 */
	/**
	 * Sets this badge's number. Only non-negative integer numbers are supported. If the number is
	 * negative, it will be clamped to 0. The specified value will be displayed, unless its number of
	 * digits exceeds `maxCharacterCount` in which case a truncated version will be shown.
	 *
	 * @param number This badge's number.
	 * @attr ref com.google.android.material.R.styleable#Badge_number
	 */
	var number: Int
		get() = if(!hasNumber())
		{
			0
		} else savedState.number
		set(value)
		{
			var number = value
			number = max(0, number)
			if(savedState.number != number)
			{
				savedState.number = number
				textDrawableHelper.isTextWidthDirty = true
				updateCenterAndBounds()
				invalidateSelf()
			}
		}

	var text: String = ""
		set(value)
		{
			if(field != value)
			{
				field = value
				textDrawableHelper.isTextWidthDirty = true
				updateCenterAndBounds()
				invalidateSelf()
			}
		}

	var padding: Int = 0
		set(value)
		{
			if(field != value)
			{
				field = value
				savedState.padding = padding
				updateCenterAndBounds()
				invalidateSelf()
			}
		}

	var insetPadding: Int = 0
		set(value)
		{
			if(field != value)
			{
				field = value
				savedState.insetPadding = insetPadding
				updateCenterAndBounds()
				invalidateSelf()
			}
		}

	var textAllCaps: Boolean = false
		set(value)
		{
			if(field != value)
			{
				field = value
				textDrawableHelper.isTextWidthDirty = true
				updateCenterAndBounds()
				invalidateSelf()
			}
		}

	/** Resets any badge number so that a numberless badge will be displayed.  */
	fun clearNumber()
	{
		savedState.number = BADGE_NUMBER_NONE
		invalidateSelf()
	}
	/**
	 * Returns this badge's max character count.
	 *
	 * @see .setMaxCharacterCount
	 * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
	 */
	/**
	 * Sets this badge's max character count.
	 *
	 * @param maxCharacterCount This badge's max character count.
	 * @attr ref com.google.android.material.R.styleable#Badge_maxCharacterCount
	 */
	var maxCharacterCount: Int
		get() = savedState.maxCharacterCount
		set(maxCharacterCount)
		{
			if(savedState.maxCharacterCount != maxCharacterCount)
			{
				savedState.maxCharacterCount = maxCharacterCount
				updateMaxBadgeNumber()
				textDrawableHelper.isTextWidthDirty = true
				updateCenterAndBounds()
				invalidateSelf()
			}
		}

	/**
	 * Sets this badge's gravity with respect to its anchor view.
	 *
	 * @param gravity Constant representing one of 4 possible [BadgeGravity] values.
	 */
	@get:BadgeGravity
	var badgeGravity: Int
		get() = savedState.badgeGravity
		set(gravity)
		{
			if(savedState.badgeGravity != gravity)
			{
				savedState.badgeGravity = gravity
				if(anchorViewRef != null && anchorViewRef!!.get() != null)
				{
					updateBadgeCoordinates(
						anchorViewRef!!.get()!!, if(customBadgeParentRef != null) customBadgeParentRef!!.get() else null
					)
				}
			}
		}

	override fun isStateful(): Boolean
	{
		return false
	}

	override fun setColorFilter(colorFilter: ColorFilter?)
	{
		// Intentionally empty.
	}

	override fun getAlpha(): Int
	{
		return savedState.alpha
	}

	override fun setAlpha(alpha: Int)
	{
		savedState.alpha = alpha
		textDrawableHelper.textPaint.alpha = alpha
		invalidateSelf()
	}

	override fun getOpacity(): Int
	{
		return PixelFormat.TRANSLUCENT
	}

	/** Returns the height at which the badge would like to be laid out.  */
	override fun getIntrinsicHeight(): Int
	{
		return badgeBounds.height()
	}

	/** Returns the width at which the badge would like to be laid out.  */
	override fun getIntrinsicWidth(): Int
	{
		return badgeBounds.width()
	}

	override fun draw(canvas: Canvas)
	{
		val bounds = bounds
		if(bounds.isEmpty || alpha == 0 || !isVisible)
		{
			return
		}
		shapeDrawable.draw(canvas)
		if(text.isNotBlank() || hasNumber())
		{
			drawText(canvas)
		}
	}

	/**
	 * Implements the TextDrawableHelper.TextDrawableDelegate interface.
	 *
	 * @hide
	 */
	@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
	override fun onTextSizeChange()
	{
		invalidateSelf()
	}

	override fun onStateChange(state: IntArray): Boolean
	{
		return super.onStateChange(state)
	}

	fun setContentDescriptionNumberless(charSequence: CharSequence?)
	{
		savedState.contentDescriptionNumberless = charSequence
	}

	fun setContentDescriptionQuantityStringsResource(@PluralsRes stringsResource: Int)
	{
		savedState.contentDescriptionQuantityStrings = stringsResource
	}

	fun setContentDescriptionExceedsMaxBadgeNumberStringResource(
		@StringRes stringsResource: Int
	)
	{
		savedState.contentDescriptionExceedsMaxBadgeNumberRes = stringsResource
	}

	val contentDescription: CharSequence?
		get()
		{
			if(!isVisible)
			{
				return null
			}
			return if(hasNumber())
			{
				if(savedState.contentDescriptionQuantityStrings > 0)
				{
					val context = contextRef.get() ?: return null
					if(number <= maxBadgeNumber)
					{
						context.resources.getQuantityString(
							savedState.contentDescriptionQuantityStrings, number, number
						)
					} else
					{
						context.getString(
							savedState.contentDescriptionExceedsMaxBadgeNumberRes, maxBadgeNumber
						)
					}
				} else
				{
					null
				}
			} else
			{
				savedState.contentDescriptionNumberless
			}
		}
	/**
	 * Returns how much (in pixels) this badge is being horizontally offset towards the center of its
	 * anchor.
	 */
	/**
	 * Sets how much (in pixels) to horizontally move this badge towards the center of its anchor.
	 *
	 * @param px badge's horizontal offset
	 */
	var horizontalOffset: Int
		get() = savedState.horizontalOffset
		set(px)
		{
			savedState.horizontalOffset = px
			updateCenterAndBounds()
		}
	/**
	 * Returns how much (in pixels) this badge is being vertically moved towards the center of its
	 * anchor.
	 */
	/**
	 * Sets how much (in pixels) to vertically move this badge towards the center of its anchor.
	 *
	 * @param px badge's vertical offset
	 */
	var verticalOffset: Int
		get() = savedState.verticalOffset
		set(px)
		{
			savedState.verticalOffset = px
			updateCenterAndBounds()
		}

	private fun setTextAppearanceResource(@StyleRes id: Int)
	{
		val context = contextRef.get() ?: return
		setTextAppearance(TextAppearance(context, id))
	}

	private fun setTextAppearance(textAppearance: TextAppearance?)
	{
		if(textDrawableHelper.textAppearance === textAppearance)
		{
			return
		}
		val context = contextRef.get() ?: return
		textDrawableHelper.setTextAppearance(textAppearance, context)
		updateCenterAndBounds()
	}

	private fun updateCenterAndBounds()
	{
		val context = contextRef.get()
		val anchorView = if(anchorViewRef != null) anchorViewRef!!.get() else null
		if(context == null || anchorView == null)
		{
			return
		}
		val tmpRect = Rect()
		tmpRect.set(badgeBounds)
		val anchorRect = Rect()
		// Retrieves the visible bounds of the anchor view.
		anchorView.getDrawingRect(anchorRect)
		val customBadgeParent = if(customBadgeParentRef != null) customBadgeParentRef!!.get() else null
		if(customBadgeParent != null || BadgeUtils.USE_COMPAT_PARENT)
		{
			// Calculates coordinates relative to the parent.
			val viewGroup = customBadgeParent ?: anchorView.parent as ViewGroup
			viewGroup.offsetDescendantRectToMyCoords(anchorView, anchorRect)
		}
		calculateCenterAndBounds(context, anchorRect, anchorView)
		BadgeUtils.updateBadgeBounds(badgeBounds, badgeCenterX, badgeCenterY, halfBadgeWidth, halfBadgeHeight)
		shapeDrawable.setCornerSize(cornerRadius)
		if(tmpRect != badgeBounds)
		{
			shapeDrawable.bounds = badgeBounds
		}
	}

	private fun calculateCenterAndBounds(
		context: Context, anchorRect: Rect, anchorView: View
	)
	{
		badgeCenterY = when(savedState.badgeGravity)
		{
			BOTTOM_END, BOTTOM_START -> anchorRect.bottom - savedState.verticalOffset.toFloat()
			TOP_END, TOP_START -> anchorRect.top + savedState.verticalOffset.toFloat()
			else -> anchorRect.top + savedState.verticalOffset.toFloat()
		}

		when
		{
			text.isNotBlank() ->
			{
				cornerRadius = badgeWithTextRadius
				halfBadgeHeight = cornerRadius + padding
				val badgeText = badgeText
				halfBadgeWidth = max(cornerRadius, textDrawableHelper.getTextWidth(badgeText) / 2f + badgeWidePadding) + padding
			}
			number <= MAX_CIRCULAR_BADGE_NUMBER_COUNT ->
			{
				cornerRadius = if(!hasNumber()) badgeRadius else badgeWithTextRadius
				halfBadgeHeight = cornerRadius + padding
				halfBadgeWidth = cornerRadius + padding
			}
			else ->
			{
				cornerRadius = badgeWithTextRadius
				halfBadgeHeight = cornerRadius + padding
				val badgeText = badgeText
				halfBadgeWidth = textDrawableHelper.getTextWidth(badgeText) / 2f + badgeWidePadding + padding
			}
		}
		val inset = context.resources.getDimensionPixelSize(
			if(hasNumber() || text.isNotBlank()) R.dimen.mtrl_badge_text_horizontal_edge_offset else R.dimen.mtrl_badge_horizontal_edge_offset
		) - insetPadding
		badgeCenterX = when(savedState.badgeGravity)
		{
			BOTTOM_START, TOP_START -> if(ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_LTR)
				anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset
			else anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset

			BOTTOM_END, TOP_END -> if(ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_LTR)
				anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset
			else anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset

			else -> if(ViewCompat.getLayoutDirection(anchorView) == ViewCompat.LAYOUT_DIRECTION_LTR)
				anchorRect.right + halfBadgeWidth - inset - savedState.horizontalOffset
			else anchorRect.left - halfBadgeWidth + inset + savedState.horizontalOffset
		}
	}

	private fun drawText(canvas: Canvas)
	{
		val textBounds = Rect()
		val badgeText = if(!textAllCaps) badgeText else badgeText.upperCased

		textDrawableHelper.textPaint.getTextBounds(badgeText, 0, badgeText.length, textBounds)
		canvas.drawText(
			badgeText, badgeCenterX, badgeCenterY + textBounds.height() / 2
			, textDrawableHelper.textPaint
		)
	}

	// If number exceeds max count, show badgeMaxCount+ instead of the number.
	private val badgeText: String
		get() = // If number exceeds max count, show badgeMaxCount+ instead of the number.
			when
			{
				text.isNotBlank() -> text

				number <= maxBadgeNumber -> Integer.toString(number)

				else ->
				{
					val context = contextRef.get()
					context?.getString(
						R.string.mtrl_exceed_max_badge_number_suffix, maxBadgeNumber, DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX
					) ?: ""
				}
			}

	private fun updateMaxBadgeNumber()
	{
		maxBadgeNumber = Math.pow(10.0, maxCharacterCount.toDouble() - 1).toInt() - 1
	}

	companion object
	{
		/** The badge is positioned along the top and end edges of its anchor view  */
		const val TOP_END = Gravity.TOP or Gravity.END

		/** The badge is positioned along the top and start edges of its anchor view  */
		const val TOP_START = Gravity.TOP or Gravity.START

		/** The badge is positioned along the bottom and end edges of its anchor view  */
		const val BOTTOM_END = Gravity.BOTTOM or Gravity.END

		/** The badge is positioned along the bottom and start edges of its anchor view  */
		const val BOTTOM_START = Gravity.BOTTOM or Gravity.START

		/**
		 * Maximum number of characters a badge supports displaying by default. It could be changed using
		 * BadgeDrawable#setMaxBadgeCount.
		 */
		private const val DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4

		/** Value of -1 denotes a numberless badge.  */
		private const val BADGE_NUMBER_NONE = -1

		/** Maximum value of number that can be displayed in a circular badge.  */
		private const val MAX_CIRCULAR_BADGE_NUMBER_COUNT = 9

		@StyleRes
		private val DEFAULT_STYLE = R.style.Ownapp_Style_Badge

		@AttrRes
		private val DEFAULT_THEME_ATTR = R.attr.badgeStyle

		/**
		 * If the badge number exceeds the maximum allowed number, append this suffix to the max badge
		 * number and display is as the badge text instead.
		 */
		const val DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+"

		/** Creates an instance of BadgeDrawable with the provided [SavedState].  */
		@JvmStatic fun createFromSavedState(
			context: Context, savedState: SavedState
		): TextBadgeDrawable
		{
			val badge = TextBadgeDrawable(context)
			badge.restoreFromSavedState(savedState)
			return badge
		}

		/** Creates an instance of BadgeDrawable with default values.  */
		fun create(context: Context): TextBadgeDrawable
		{
			return createFromAttributes(context,  /* attrs= */null, DEFAULT_THEME_ATTR, DEFAULT_STYLE)
		}

		/**
		 * Returns a BadgeDrawable from the given XML resource. All attributes from [ ][R.styleable.Badge] and a custom `style` attribute are supported. A badge resource
		 * may look like:
		 *
		 * <pre>`<badge
		 * xmlns:app="http://schemas.android.com/apk/res-auto"
		 * style="@style/Widget.MaterialComponents.Badge"
		 * app:maxCharacterCount="2"/>
		`</pre> *
		 */
		fun createFromResource(context: Context, @XmlRes id: Int): TextBadgeDrawable
		{
			val attrs = DrawableUtils.parseDrawableXml(context, id, "badge")
			@StyleRes var style = attrs.styleAttribute
			if(style == 0)
			{
				style = DEFAULT_STYLE
			}
			return createFromAttributes(context, attrs, DEFAULT_THEME_ATTR, style)
		}

		/** Returns a BadgeDrawable from the given attributes.  */
		private fun createFromAttributes(
			context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int, @StyleRes defStyleRes: Int
		): TextBadgeDrawable
		{
			val badge = TextBadgeDrawable(context)
			badge.loadDefaultStateFromAttributes(context, attrs, defStyleAttr, defStyleRes)
			return badge
		}

		private fun readColorFromAttributes(
			context: Context, a: TypedArray, @StyleableRes index: Int
		): Int
		{
			return MaterialResources.getColorStateList(context, a, index)!!.let {
				it.getColorForState(intArrayOf(android.R.attr.state_enabled), it.defaultColor)
			}
		}
	}

	init
	{
		ThemeEnforcement.checkMaterialTheme(context)
		val res = context.resources
		badgeBounds = Rect()
		shapeDrawable = MaterialShapeDrawable()
		badgeRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_radius).toFloat()
		badgeWidePadding = res.getDimensionPixelSize(R.dimen.mtrl_badge_long_text_horizontal_padding).toFloat()
		badgeWithTextRadius = res.getDimensionPixelSize(R.dimen.mtrl_badge_with_text_radius).toFloat()
		textDrawableHelper = TextDrawableHelper( /* delegate= */this)
		textDrawableHelper.textPaint.textAlign = Paint.Align.CENTER
		savedState = SavedState(context)
		setTextAppearanceResource(R.style.Ownapp_TextAppearance_Badge)
	}
}