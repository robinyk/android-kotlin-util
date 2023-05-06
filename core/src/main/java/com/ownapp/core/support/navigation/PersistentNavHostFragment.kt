package com.ownapp.core.support.navigation

import android.annotation.SuppressLint
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.plusAssign

class PersistentNavHostFragment: NavHostFragment()
{
    @SuppressLint("RestrictedApi")
    override fun onCreateNavController(navController: NavController)
    {
        super.onCreateNavController(navController)
        navController.navigatorProvider += PersistentFragmentNavigator(requireContext(), childFragmentManager, id)
    }
}