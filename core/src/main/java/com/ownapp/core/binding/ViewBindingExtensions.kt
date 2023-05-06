package com.ownapp.core.binding

import android.app.Activity
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.ownapp.core.binding.delegate.ActivityViewBindingDelegate
import com.ownapp.core.binding.delegate.FragmentViewBindingDelegate
import com.ownapp.core.binding.delegate.GlobalViewBindingDelegate

/**
 * Updated by Robin on 2020/12/4
 */

fun <T: ViewDataBinding> Activity.viewBinding(@LayoutRes layoutId: Int): Lazy<T>
		= lazy { DataBindingUtil.setContentView(this, layoutId) }

inline fun <T: ViewBinding> Activity.viewBinder(crossinline bindingInflater: (LayoutInflater) -> T) =
	lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }

fun <T: ViewBinding> AppCompatActivity.viewBinding(bindingInflater: (LayoutInflater) -> T, beforeSetContent: () -> Unit = {}) =
	ActivityViewBindingDelegate(this, bindingInflater, beforeSetContent)

fun <T: ViewBinding> Fragment.viewBinding(viewBindingFactory: (View) -> T, disposeEvents: T.() -> Unit = {}) =
	FragmentViewBindingDelegate(this, viewBindingFactory, disposeEvents)


fun <T: ViewBinding> globalViewBinding(viewBindingFactory: (View) -> T) = GlobalViewBindingDelegate(viewBindingFactory)

internal fun ensureMainThread() {
	if (Looper.myLooper() != Looper.getMainLooper()) {
		throw IllegalThreadStateException("View can be accessed only on the main thread.")
	}
}