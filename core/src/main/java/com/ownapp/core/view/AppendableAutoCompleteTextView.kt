package com.ownapp.core.view

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textfield.MaterialAutoCompleteTextView

/**
 * Created by Robin on 5/3/2021.
 */
class AppendableAutoCompleteTextView: MaterialAutoCompleteTextView
{
	constructor(context: Context): super(context)
	constructor(context: Context, attrs: AttributeSet?): super(context, attrs)

	override fun replaceText(text: CharSequence?)
	{

	}
}