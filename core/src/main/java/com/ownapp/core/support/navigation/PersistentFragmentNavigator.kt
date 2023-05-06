package com.ownapp.core.support.navigation

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.ClassType
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.NavigatorProvider
import androidx.navigation.fragment.FragmentNavigator
import com.ownapp.core.R
import com.ownapp.core.extensions.destroy
import com.ownapp.core.extensions.pause
import com.ownapp.core.extensions.resource.getEntryName
import com.ownapp.core.extensions.resume
import com.ownapp.core.extensions.start
import com.ownapp.core.extensions.utility.*

@Navigator.Name("persistent_fragment") // `persistent_fragment` is used in navigation xml
class PersistentFragmentNavigator(
	private val context: Context
	, private val manager: FragmentManager
	, private val containerId: Int
): Navigator<PersistentFragmentNavigator.Destination>()
{
	override fun createDestination(): Destination = Destination(this)

	override fun navigate(
		destination: Destination
		, args: Bundle?
		, navOptions: NavOptions?
		, navigatorExtras: Extras?
	): NavDestination?
	{
		if(manager.isStateSaved)
		{
			"Ignoring navigate() call: FragmentManager has already saved its state".warn()
			return null
		}

		val tag = StringBuilder("${destination.id}-${destination.label}").apply {
			args?.keySet()?.filter { it.endsWith("id") }?.run {
				forEach { args.get(it)?.let { value -> append("-$value")} }
			}
		}.toString()

		val addAnimation = navOptions?.let { if(it.enterAnim >= 0) it.enterAnim else R.anim.slide_in_right } ?: R.anim.slide_in_right
		val hideAnimation = navOptions?.let { if(it.exitAnim >= 0) it.exitAnim else R.anim.slide_out_left } ?: R.anim.slide_out_left
		val showAnimation = navOptions?.let { if(it.popEnterAnim >= 0) it.popEnterAnim else R.anim.slide_in_left } ?: R.anim.slide_in_left
		val removeAnimation = navOptions?.let { if(it.popExitAnim >= 0) it.popExitAnim else R.anim.slide_out_right } ?: R.anim.slide_out_right

		var destinationFragment = if(navOptions?.popUpTo != destination.id)
			manager.fragments.find { it.tag == tag }
		else null

		info(
			"Destination {$destinationFragment} is in backstack => ${(destinationFragment != null)}"
			, "popUpTo" to "${navOptions?.popUpTo} = ${navOptions?.popUpTo?.getEntryName(context)}"
//			, "Start animation {$addAnimation}" to addAnimation.getEntryName(context)
//			, "Pause animation {$hideAnimation}" to hideAnimation.getEntryName(context)
//			, "Destroy animation {$showAnimation}" to showAnimation.getEntryName(context)
//			, "Resume animation {$removeAnimation}" to removeAnimation.getEntryName(context)
		)

		manager.commit {
			setCustomAnimations(showAnimation, removeAnimation, addAnimation, hideAnimation)

			manager.primaryNavigationFragment?.let { currentFragment ->
				if(navOptions?.popUpTo != -1)
				{
					if(navOptions?.popUpTo == destination.id)
					{
						manager.fragments.asReversed().forEach { destroy(it) }
					}
					else
					{
						// Check start index
						val popIndexStart = if(destinationFragment != null) // Fragment already created in backstack
							manager.fragments.indexOf(destinationFragment) + 1
						else 0

						info("popIndexStart from $popIndexStart"
							, "check exists" to (manager.fragments.any { it.tag?.startsWith(navOptions?.popUpTo.toString()) == true })
						)

						// Check end index
						if(manager.fragments.any { it.tag?.startsWith(navOptions?.popUpTo.toString()) == true })
						{
							val popFragment = manager.fragments.findLast { it.tag?.startsWith(navOptions?.popUpTo.toString()) == true }
							val popIndexEnd = manager.fragments.indexOfLast { it == popFragment }

//							if(navOptions?.isPopUpToInclusive != true)
//								popIndexEnd--

							for(i in popIndexStart .. popIndexEnd)
							{
								manager.fragments.filterIndexed { index, fragment -> index == i && fragment != destinationFragment }.forEach {
									destroy(it)
								}
							}

							info(
								"Navigator",
								"popIndexEnd" to popIndexEnd,
								"popFragment" to popFragment,
								"navOptions?.isPopUpToInclusive" to navOptions?.isPopUpToInclusive
							)
						}
						else
						{
							setCustomAnimations(addAnimation, hideAnimation, addAnimation, hideAnimation)
							pause(currentFragment)
						}
					}
				}
				else
				{
					setCustomAnimations(addAnimation, hideAnimation, addAnimation, hideAnimation)
					pause(currentFragment)
				}
			}
		}

		manager.commit {
			setCustomAnimations(addAnimation, hideAnimation, addAnimation, hideAnimation)

			if(destinationFragment == null)
			{
				val className = destination.className.let { if(it[0] == '.') context.packageName + it else it }
				destinationFragment = manager.fragmentFactory.instantiate(context.classLoader, className).apply {
					val bundle = bundleOf("showAnim" to showAnimation, "removeAnim" to removeAnimation)
					arguments = args?.apply { putAll(bundle) } ?: bundle
				}

				start(containerId, destinationFragment!!, tag)
			}
			else resume(destinationFragment!!)

			if(navigatorExtras is FragmentNavigator.Extras)
			{
				for((key, value) in navigatorExtras.sharedElements)
				{
					addSharedElement(key, value)
				}
			}

			"${manager.primaryNavigationFragment?.tag ?: ""} navigate to => ${destinationFragment!!.tag}".debug()

//			with(StringBuilder()) {
//				appendLine("${manager.primaryNavigationFragment?.tag} navigate to => ${destinationFragment!!.tag}")
//
//				tryOrElse({ append("args => ${args.prettyJson}") }
//					, {
//						append("args => ")
//
//						if(args != null)
//						{
//							appendLine("{")
//							args.keySet().forEach { appendLine("  \"$it\": ${args.get(it)},") }
//							appendLine("}")
//						}
//						else appendLine("null")
//
//					}
//				)
//
//				appendLine("navOptions => ${navOptions.prettyJson}")
//				appendLine("navigatorExtras => $navigatorExtras")
//
//				toString().info()
//			}
		}

		return destination
	}
	
	

	override fun popBackStack(): Boolean
	{
		if(manager.fragments.count() <= 1)
			return false

		if(manager.isStateSaved)
		{
			"Ignoring popBackStack() call: FragmentManager has already saved its state".warn()
			return false
		}

		val currentFragment = manager.primaryNavigationFragment

		if(currentFragment != null)
		{
			manager.commit {
				setReorderingAllowed(true)

				val showAnim = currentFragment.safeExtra("showAnim", 0).value
				val removeAnim = currentFragment.safeExtra("removeAnim", 0).value

				info("On popBackStack animation"
					, "Remove" to removeAnim.getEntryName(context)
					, "Show" to showAnim.getEntryName(context)
				)

				setCustomAnimations(showAnim, removeAnim, 0, 0)
				destroy(currentFragment)

				manager.fragments
					.lastOrNull { it.isHidden }
					?.let { resume(it) }
			}

			return true
		}

		return false
	}

	@ClassType(Fragment::class)
	class Destination(fragmentNavigator: Navigator<out Destination?>): FragmentNavigator.Destination(fragmentNavigator)
	{
		constructor(navigatorProvider: NavigatorProvider): this(navigatorProvider.getNavigator(PersistentFragmentNavigator::class.java))
	}
}