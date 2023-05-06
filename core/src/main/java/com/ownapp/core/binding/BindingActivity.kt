package com.ownapp.core.binding

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.ownapp.core.R
import com.ownapp.core.extensions.*
import com.ownapp.core.extensions.utility.*
import com.ownapp.core.util.helper.UpdateHelper.checkForUpdate
import com.ownapp.core.view.fragment.FragmentOnBackPressListener

/**
 * Updated by Robin on 2020/12/4
 */
abstract class BindingActivity: AppCompatActivity()
{
    //**--------------------------------------------------------------------------------------------------
    //*      Variable
    //---------------------------------------------------------------------------------------------------*/
    // Class
    protected val activity by lazy { this }
    open val appUpdateManager: AppUpdateManager? by lazy { AppUpdateManagerFactory.create(this) }

    // Value
    open var baseFragmentIds: List<Int> = listOf()
    open var fragmentContainerView: View? = null
    open var isBackOnFirstPress = true
    open var isClearFragment = true


    //**--------------------------------------------------------------------------------------------------
    //*      Initialize
    //---------------------------------------------------------------------------------------------------*/
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setupStatusBarColor()
        onExtras(intent.extras)
    }

    override fun onPostCreate(savedInstanceState: Bundle?)
    {
        super.onPostCreate(savedInstanceState)
        onInitialize()
    }

    protected open fun onExtras(bundle: Bundle?) {}
    protected open fun onInitialize() {}


    //**--------------------------------------------------------------------------------------------------
    //*      Function
    //---------------------------------------------------------------------------------------------------*/
    open fun checkForUpdate()
    {
        ("AutoUpdate enabled" to (appUpdateManager != null)).log()
        appUpdateManager?.let { checkForUpdate(it) }
    }

    fun exit()
    {
        when
        {
            isBackOnFirstPress -> finish()

            else ->
            {
                toast(R.string.msg_press_back_again_to_exit)
                isBackOnFirstPress = true
                lifecycleScope.launchDelay(2000) {  isBackOnFirstPress = false }
            }
        }
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Override
    //---------------------------------------------------------------------------------------------------*/
    override fun onStop()
    {
        hideKeyboard()
        super.onStop()
    }

    override fun onResume()
    {
        super.onResume()
        checkForUpdate()
    }

    override fun onDestroy()
    {
        hideKeyboard()
        super.onDestroy()
    }


    override fun onBackPressed()
    {
        hideKeyboard()

        if(currentChildFragment?.back() == false)
            exit()
    }
}