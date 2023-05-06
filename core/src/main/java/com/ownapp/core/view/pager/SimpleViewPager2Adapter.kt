package com.ownapp.core.view.pager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class SimpleViewPager2Adapter: FragmentStateAdapter
{
    constructor(fragment: Fragment): super(fragment)
    constructor(fragmentActivity: FragmentActivity): super(fragmentActivity)
    constructor(fragmentManager: FragmentManager, lifecycle: Lifecycle): super(fragmentManager, lifecycle)
    
    
    //**--------------------------------------------------------------------------------------------------
    //*      Variable
    //---------------------------------------------------------------------------------------------------*/
    // Value
    var fragments: MutableList<Fragment> = mutableListOf()

    var titleList: MutableList<String> = mutableListOf()
        private set


    //**--------------------------------------------------------------------------------------------------
    //*      Function
    //---------------------------------------------------------------------------------------------------*/
    private fun addFragment(fragment: Fragment, title: String? = "")
    {
        fragments.add(fragment)
        titleList.add(title.orEmpty())
    }
    
    fun add(fragment: Fragment) = addFragment(fragment)
    fun add(fragment: Pair<String, Fragment>) = addFragment(fragment.second, fragment.first)
    fun add(vararg fragments: Pair<String, Fragment>) = fragments.forEach {
        addFragment(it.second, it.first)
    }
    
    fun add(fragments: List<Fragment>?): Boolean
    {
        if(!fragments.isNullOrEmpty())
            return this.fragments.addAll(fragments)
        
        return false
    }
    
    fun replace(fragments: List<Fragment>?)
    {
        this.fragments = mutableListOf()
        add(fragments)
    }
    
    fun getTitle(position: Int): CharSequence = titleList.getOrNull(position) ?: ""

    
    //**--------------------------------------------------------------------------------------------------
    //*      Override
    //---------------------------------------------------------------------------------------------------*/
    override fun getItemCount(): Int = fragments.size
    
    override fun createFragment(position: Int): Fragment = fragments[position]
}