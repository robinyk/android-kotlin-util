package com.ownapp.core.view.fragment

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.annotation.StyleRes
import androidx.core.os.bundleOf
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.ownapp.core.R
import com.ownapp.core.extensions.setupStatusBarColor
import com.ownapp.core.extensions.utility.hideKeyboard
import com.ownapp.core.extensions.utility.logWarn
import kotlin.random.Random


/**
 * Updated by Robin on 2021/1/21
 */

open class FullScreenDialogFragment<T: ViewDataBinding> : DialogFragment, DialogInterface.OnKeyListener
{
	//**--------------------------------------------------------------------------------------------------
	//*      Interface
	//---------------------------------------------------------------------------------------------------*/
	interface Callback
	{
		fun onDialogResult(requestCode: Int, resultCode: Int, intent: Intent)
	}


	//**--------------------------------------------------------------------------------------------------
	//*      Constructor
	//---------------------------------------------------------------------------------------------------*/
	constructor(viewBinder: (LayoutInflater, ViewGroup?, Boolean) -> T) : super()
	{
		this.viewBinder = viewBinder
		requestKey = Random.nextInt(99999).toString()
	}

	constructor(viewBinder: (LayoutInflater, ViewGroup?, Boolean) -> T, requestKey: String) : super()
	{
		this.viewBinder = viewBinder
		this.requestKey = requestKey
	}


	//**--------------------------------------------------------------------------------------------------
	//*      Variable
	//---------------------------------------------------------------------------------------------------*/
	// Class
	protected lateinit var binder: T
	private var callback: Callback? = null

	private val viewBinder: (LayoutInflater, ViewGroup?, Boolean) -> T
	val requestKey: String

	// Value
	@StyleRes open var animationStyle = R.style.Animation_Slide_Up
	override fun getTheme(): Int = R.style.Theme_MaterialComponents_DayNight_Dialog
	
	
	//**--------------------------------------------------------------------------------------------------
	//*      Initialize
	//------------------------------------------------------ ---------------------------------------------*/
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
	{
		return viewBinder(inflater, container, false).apply {
			lifecycleOwner = this@FullScreenDialogFragment
			binder = this
		}.root
	}
	
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		return super.onCreateDialog(savedInstanceState).apply {
			window?.attributes?.windowAnimations = animationStyle
			window?.setGravity(Gravity.BOTTOM)
			
			context.setupStatusBarColor(window)
			setOnKeyListener(this@FullScreenDialogFragment)
		}
	}
	
	// override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	// {
	// 	super.onViewCreated(view, savedInstanceState)
	// 	dialog?.window?.attributes?.windowAnimations = animationStyle
	// 	dialog?.window?.setGravity(Gravity.BOTTOM)
	// }

	
	//**--------------------------------------------------------------------------------------------------
	//*      Protected
	//------------------------------------------------------ ---------------------------------------------*/
	protected open fun dismissWithResult(vararg results: Pair<String, Any?>) = dismissWithResult(bundleOf(*results))
	
	protected open fun dismissWithResult(bundle: Bundle)
	{
		// targetFragment?.onActivityResult(targetRequestCode, resultCode, intent)
		// 	?: callback?.onDialogResult(requestCode, resultCode, intent)
		
		setFragmentResult(requestKey, bundle)
		dismiss()
	}


	//**--------------------------------------------------------------------------------------------------
	//*      Setter
	//---------------------------------------------------------------------------------------------------*/
	// fun setTargetFragment(fragment: Fragment)
	// {
	// 	setTargetFragment(fragment, requestCode)
	// }


	//**--------------------------------------------------------------------------------------------------
	//*      Public
	//---------------------------------------------------------------------------------------------------*/
	open fun show(fragmentManager: FragmentManager) = show(fragmentManager, this.javaClass.simpleName)
	
	open fun onBackPressed() = dismiss()
	

	//**--------------------------------------------------------------------------------------------------
	//*      Override
	//---------------------------------------------------------------------------------------------------*/
	override fun onAttach(context: Context)
	{
		super.onAttach(context)

		try
		{
			callback = if(activity != null)
				context as Callback
			else
				parentFragment as Callback
		}
		catch(e: ClassCastException)
		{
			"Parent activity/fragment doesn't implement the resultCallback interface".logWarn()
		}
	}

	override fun onStart()
	{
		super.onStart()
		dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
	}

	// override fun onActivityCreated(savedInstanceState: Bundle?)
	// {
	// 	super.onActivityCreated(savedInstanceState)
	// 	dialog?.window?.attributes?.windowAnimations = animationStyle
	// 	dialog?.window?.setGravity(Gravity.BOTTOM)
	// }

	override fun dismiss()
	{
		hideKeyboard()
		super.dismiss()
	}
	
	
	//**--------------------------------------------------------------------------------------------------
	//*      Public
	//------------------------------------------------------ ---------------------------------------------*/
	override fun onKey(dialog: DialogInterface?, keyCode: Int, event: KeyEvent?): Boolean
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && event?.action == KeyEvent.ACTION_UP)
		{
			onBackPressed()
			return true
		}
		
		return false
	}
}