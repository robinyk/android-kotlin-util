package com.ownapp.core.view.pager

import android.os.Parcelable
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.ownapp.core.extensions.utility.logError

@Deprecated("Use ViewPager2")
class PagerAdapter(fragmentManager: FragmentManager)
    : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
{
    //**--------------------------------------------------------------------------------------------------
    //*      Variable
    //---------------------------------------------------------------------------------------------------*/
    // Value
    var fragments: MutableList<Fragment> = mutableListOf()

    var titleList: MutableList<String> = mutableListOf()
        private set


    //**--------------------------------------------------------------------------------------------------
    //*      Public
    //---------------------------------------------------------------------------------------------------*/
    fun addFragment(fragment: Fragment, title: String? = "")
    {
        fragments.add(fragment)
        titleList.add(title.orEmpty())
    }
    
    fun addFragments(fragmentList: List<Fragment>?): Boolean
    {
        if(!fragmentList.isNullOrEmpty())
            return fragments.addAll(fragmentList)
        
        return false
    }


    //**--------------------------------------------------------------------------------------------------
    //*      Override
    //---------------------------------------------------------------------------------------------------*/
    override fun getCount(): Int = fragments.size

    override fun getPageTitle(position: Int): CharSequence = if(position < titleList.size) titleList[position] else ""

    override fun getItem(position: Int): Fragment = fragments[position]

    override fun restoreState(state: Parcelable?, loader: ClassLoader?)
    {
        try
        {
            super.restoreState(state, loader)
        }
        catch(e: Exception)
        {
            e.message.logError()
        }
    }
}