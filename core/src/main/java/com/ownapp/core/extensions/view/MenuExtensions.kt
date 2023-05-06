package com.ownapp.core.extensions.view

import android.view.Menu
import android.view.MenuItem
import androidx.core.text.parseAsHtml
import java.util.*

/**
 * Updated by Robin on 2020/12/4
 */

inline fun Menu.forEachIndexed(action: (index: Int, item: MenuItem) -> Unit) {
	for (index in 0 until size()) {
		action(index, getItem(index))
	}
}

fun Menu.indexOf(menuItem: MenuItem): Int {
	forEachIndexed { index, item ->
		if (menuItem.itemId == item.itemId) return index
	}
	return -1
}

fun Menu.getMenuItem(index: Int): MenuItem? {
	forEachIndexed { i, item ->
		if (index == i) return item
	}
	return null
}

fun MenuItem.setTitleColor(color: Int) {
	Integer.toHexString(color).toUpperCase(Locale.ENGLISH).substring(2).let { hexColor ->
		this.title = "<font color='#$hexColor'>$title</font>".parseAsHtml()
	}
}
