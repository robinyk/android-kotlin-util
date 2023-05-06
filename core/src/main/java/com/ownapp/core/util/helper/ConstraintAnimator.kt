package com.ownapp.core.util.helper

import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager


/**
 * Created by crazy on 1/24/20 to long live and prosper !
 */

/**
 * Constraint Set Animator class
 *
 * Example usage:
 *
 * ConstraintSetAnimator(clMain, duration = 200) { newLayout ->
 *    newLayout.setVisibility(R.id.tvText, View.GONE)
 * }
 *
 * Callback should be used to set the other state of the constraint layout
 */
open class ConstraintAnimator(
	private val layout: ConstraintLayout,
	private val duration: Long = 200,
	initialiseSet: (constraintSet: ConstraintSet) -> Unit
) {

	var isAnimated: Boolean = false
	private var originalConstraintSet: ConstraintSet = ConstraintSet()
	private var modifiedConstraintSet: ConstraintSet = ConstraintSet()

	init {
		originalConstraintSet.clone(layout)
		modifiedConstraintSet.clone(originalConstraintSet)
		initialiseSet(modifiedConstraintSet)
	}

	fun revert() {
		if (!isAnimated)
			return
		beginTransition()
		originalConstraintSet.applyTo(layout)
		isAnimated = false
	}

	fun animate() {
		if (isAnimated)
			return
		beginTransition()
		modifiedConstraintSet.applyTo(layout)
		isAnimated = true
	}

	fun toggle() {
		if (isAnimated) {
			revert()
		} else {
			animate()
		}
	}

	private fun beginTransition() {
		val transition = AutoTransition()
		transition.duration = duration
		transition.interpolator = DecelerateInterpolator()
		TransitionManager.beginDelayedTransition(layout, transition)
	}
}