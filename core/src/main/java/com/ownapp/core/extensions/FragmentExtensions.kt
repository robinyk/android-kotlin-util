package com.ownapp.core.extensions

import android.Manifest
import android.content.Context
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.*
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.ownapp.core.R
import com.ownapp.core.extensions.resource.getEntryName
import com.ownapp.core.extensions.utility.*
import com.ownapp.core.support.network.HttpResponseCode
import com.ownapp.core.view.LoadingOverlay.Companion.hideLoading
import com.ownapp.core.view.LoadingOverlay.Companion.showLoading
import com.ownapp.core.view.fragment.FragmentOnBackPressListener
import retrofit2.Response
import java.lang.StringBuilder

/**
 * Updated by Robin on 2020/12/4
 */

fun FragmentTransaction.hideCurrentVisibleFragment(
	fragmentManager: FragmentManager
	, tag: String?
	, removeFromBackstack: Boolean = false
	, enableSlide: Boolean = false
): Boolean
{
	var isHidden = false
	
	fragmentManager.fragments.filter { it.tag != tag && it.isVisible }.let { fragments ->
		if(fragments.isEmpty())
		   "No visible fragments found".logWarn()
		
		fragments.forEach {
			if(removeFromBackstack)
			{
				"Remove ${it::class.simpleName}{${it.tag}} from backStack".log()
				
				if(enableSlide)
					setCustomAnimations(0, R.anim.slide_out_right, 0, R.anim.slide_out_right)
				else
					setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				
				remove(it)
				it.stop()
			}
			else
			{
				"Hide ${it::class.simpleName}{${it.tag}} into backStack".log()
				
				if(enableSlide)
					setCustomAnimations(0, R.anim.slide_out_left, 0, R.anim.slide_out_left)
				else
					setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
				
				it.pause()
				hide(it)
				isHidden = true
			}
		}
	}

	return isHidden
}

inline fun FragmentManager.replace(
	@IdRes fragmentLayoutId: Int
	, fragment: Fragment
	, removeFromBackstack: Boolean = false
	, enableSlide: Boolean = false
	, tag: String? = fragment::class.simpleName
	, block: (FragmentTransaction) -> FragmentTransaction = { it }
)
{
	commit {
		setReorderingAllowed(true)

		hideCurrentVisibleFragment(this@replace, tag, removeFromBackstack = removeFromBackstack, enableSlide = enableSlide).let { hasFragmentHidden ->
			if(enableSlide)
			{
				if(hasFragmentHidden)
					setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
				else setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_left)
			}
			else
				setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

			run(block)
			start(fragmentLayoutId, fragment, tag.orEmpty())
		}
	}
}

inline fun FragmentManager.resurface(
	tag: String?
	, removeFromBackstack: Boolean = false
	, enableSlide: Boolean = false
	, block: (FragmentTransaction) -> FragmentTransaction = { it }
): Boolean
{
	fragments.find { it.tag == tag }?.let { fragment ->
		if(fragment.isHidden)
		{
			commit {
				setReorderingAllowed(true)

				hideCurrentVisibleFragment(
					this@resurface
					, tag
					, removeFromBackstack = removeFromBackstack
					, enableSlide = enableSlide
				).let { hasFragmentHidden ->
					if(enableSlide)
					{
						if(hasFragmentHidden)
							setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_right)
						else setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_left)
					}
					else
						setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

					run(block)
					resume(fragment)
				}
			}
		}

		return true
	} ?: "No fragment with tag {$tag} is found".logWarn()

	return false
}

inline fun FragmentManager.change(
	@IdRes fragmentLayoutId: Int
	, fragment: Fragment
	, removeFromBackstack: Boolean = false
	, enableSlide: Boolean = false
	, uniqueTag: Any? = ""
	, block: (FragmentTransaction) -> FragmentTransaction = { it }
)
{
	val tag = StringBuilder(fragment::class.simpleName.orEmpty()).apply { if(uniqueTag.toString().isNotBlank()) append("-$uniqueTag") }.toString()
	var isCreated = false
	
	for(i in 0 until backStackEntryCount)
	{
		if(getBackStackEntryAt(i).name == tag)
		{
			isCreated = resurface(tag, removeFromBackstack, enableSlide, block)
			break
		}
	}

	"$tag ${if(!isCreated) "not yet create" else "is already created"}".warn()

	if(!isCreated)
		replace(fragmentLayoutId, fragment, removeFromBackstack, enableSlide, tag, block)
}

inline fun <reified T> FragmentManager.safeResponse(call: () -> Response<T?>?): T?
{
	try
	{
		call.invoke()?.let {
			if(it.isSuccessful)
			{
				return it.body().apply {
					if(this is HttpResponseCode)
						code = it.code()
				}
			}
			else if(it.errorBody() != null)
			{
				return Gson().fromJson<T?>(it.errorBody()?.charStream()).apply {
					if(this is HttpResponseCode)
						code = it.code()
				}
			}
		} ?: "Retrofit response is null: ${T::class.simpleName}".logError()
	}
	catch(e: Exception)
	{
		e.logException()
	}
	
	return null
}

fun FragmentManager.safePop(enableSlide: Boolean = true): Boolean
{
	if(fragments.size > 2)
	{
		pop(enableSlide)
		return true
	}
	
	return false
}

fun FragmentManager.pop(enableSlide: Boolean = true) = commit {
	fragments.firstOrNull { it.isVisible }?.let {
		if (enableSlide)
			setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
		else
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)

		destroy(it)
		it.stop()
		//			popBackStack(it.tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)
	}
	
	fragments.lastOrNull { it.isHidden }?.let {
		if (enableSlide)
			setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_left, R.anim.slide_out_right)
		else
			setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
		
		resume(it)
		it.resume()
	}
}

inline fun Fragment.replaceFragment(
	@IdRes fragmentLayoutId: Int
	, fragment: Fragment
	, removeFromBackstack: Boolean = false
	, enableSlide: Boolean = false
	, tag: String? = fragment::class.simpleName
	, block: (FragmentTransaction) -> FragmentTransaction = { it }
) = childFragmentManager.replace(fragmentLayoutId, fragment, removeFromBackstack, enableSlide, tag, block)

inline fun Fragment.changeFragment(
	@IdRes fragmentLayoutId: Int
	, fragment: Fragment
	, removeFromBackstack: Boolean = false
	, enableSlide: Boolean = false
	, uniqueTag: Any? = ""
	, block: (FragmentTransaction) -> FragmentTransaction = { it }
) = childFragmentManager.change(fragmentLayoutId, fragment, removeFromBackstack, enableSlide, uniqueTag, block)

inline val Fragment.currentChildFragment: Fragment?
	get() = childFragmentManager.primaryNavigationFragment

val Fragment.deepestActiveChildFragment: Fragment?
	get()
	{
		debug("${this::class.simpleName} has child fragment?" to childFragmentManager.fragments.firstOrNull { it.isVisible }?.javaClass?.simpleName)

		return childFragmentManager.fragments.firstOrNull { it.isVisible }?.let {
			it.deepestActiveChildFragment ?: it
		}
	}

inline fun <reified T: Fragment> Fragment.getChildFragment(): T?
{
	return currentChildFragment.cast<T>() ?: run {
		"Failed to cast ${this::class.simpleName} into ${T::class.simpleName}".logWarn()
		null
	}
}

inline fun <reified T: Fragment> Fragment.getActiveChildFragment(): T?
{
	return deepestActiveChildFragment.cast<T>() ?: run {
		"Failed to cast active child ${this::class.simpleName} into ${T::class.simpleName}".logWarn()
		null
	}
}

inline val AppCompatActivity.currentChildFragment: Fragment?
	get() = supportFragmentManager.primaryNavigationFragment

inline fun <reified T: Fragment> AppCompatActivity.getChildFragment(): T?
{
	return currentChildFragment.cast<T>() ?: run {
		"Failed to cast ${this::class.simpleName} into ${T::class.simpleName}".logWarn()
		null
	}
}

private inline fun Fragment.changeChildrenState(onChanged: (Fragment) -> Unit)
{
	var childFragment = currentChildFragment
	
	while (childFragment != null)
	{
		onChanged(childFragment)
		childFragment = childFragment.currentChildFragment
	}
}

fun Fragment.pause()
{
	"${this::class.simpleName} onPause".debug()
	onPause()
	changeChildrenState {
		"Child ${it::class.simpleName} onPause".debug()
		it.onPause()
	}
}

fun Fragment.stop()
{
	"${this::class.simpleName} onStop".debug()
	onStop()
	changeChildrenState {
		"Child ${it::class.simpleName} onStop".debug()
		it.onStop()
	}
}

fun Fragment.resume()
{
	"${this::class.simpleName} onResume".debug()
	onResume()
	changeChildrenState {
		"Child ${it::class.simpleName} onResume".debug()
		it.onResume()
	}
}

fun Fragment.back(): Boolean
{
	currentChildFragment?.let {
		if(it.back())
			return true
	}
//	?: childFragment?.let {
//		if(it.back())
//			return true
//	}

	if(parentFragmentManager.primaryNavigationFragment == null || parentFragmentManager.primaryNavigationFragment == this)
	{
		("${this::class.simpleName} has FragmentOnBackPressListener" to (cast<FragmentOnBackPressListener>() != null)).info()

		if (cast<FragmentOnBackPressListener>()?.onBackPressed() == true)
		{
			"${this::class.simpleName} called onBackPressedListener".debug()
			return true
		}
	}
	else if(parentFragmentManager.fragments.find { it.isHidden && it == this } != null)
	{
		parentFragmentManager.commit { destroy(this@back) }
		"${this::class.simpleName} is in backstack. Removed from backstack".debug()
	}

	"${this::class.simpleName} no back action is done".warn()

	return try
	{
		"Try NavController navigateUp()".debug()
		findNavController().navigateUp()
	}
	catch (e: Exception)
	{
		"No NavController found. Use FragmentManager.safePop instead".warn()
		requireActivity().supportFragmentManager.safePop()
	}
}

fun Fragment.showLoading() = requireActivity().showLoading()
fun Fragment.hideLoading() = requireActivity().hideLoading()

fun Fragment.navigate(directions: NavDirections) = findNavController().navigate(directions)
fun Fragment.navigate(@IdRes id: Int, args: Bundle? = null, clearAll: Boolean = false)
{
	if(clearAll)
		navigate(id, args, id)
	else findNavController().navigate(id, args)
}

fun Fragment.navigate(@IdRes id: Int, args: Bundle? = null, @IdRes popUpToId: Int, isInclusive: Boolean = false)
{
	findNavController().navigate(id, args, NavOptions.Builder().setPopUpTo(popUpToId, isInclusive).build())
}

fun Fragment.navigate(@IdRes id: Int, args: Bundle? = null, navOptions: NavOptions) = findNavController().navigate(id, args, navOptions)

fun FragmentTransaction.start(@IdRes containerId: Int, fragment: Fragment, tag: String)
{
	"Add ${fragment::class.simpleName} {${tag}} into $containerId".debug()

	add(containerId, fragment, tag)
	addToBackStack(tag)
//	fragment.onStart()

	setPrimaryNavigationFragment(fragment)
}

fun FragmentTransaction.pause(fragment: Fragment, @AnimRes animationRes: Int = 0) = with(fragment) {
	"Hide ${this::class.simpleName} {${tag}} from ${fragment.parentFragment?.javaClass?.simpleName} backStack".debug()

	if(animationRes != 0)
		setCustomAnimations(0, animationRes, 0, animationRes)

	hide(this)
	hideKeyboard()
	pause()
}

fun FragmentTransaction.resume(fragment: Fragment) = with(fragment) {
	"Show ${this::class.simpleName} {${tag}} from ${fragment.parentFragment?.javaClass?.simpleName} backStack".debug()
	show(this)
	resume()

	setPrimaryNavigationFragment(this)
}

fun FragmentTransaction.destroy(fragment: Fragment) = with(fragment) {
	"Remove ${this::class.simpleName} {${tag}} from ${fragment.parentFragment?.javaClass?.simpleName} backStack".debug()
	remove(this)
	hideKeyboard()
	stop()
}

fun Fragment.getDrawableCompat(@DrawableRes drawableId: Int) = requireContext().getDrawableCompat(drawableId)

fun Fragment.isNetworkConnected() = requireContext().isNetworkConnected()

inline val Fragment.isDebuggable
	get() = requireContext().isDebuggable