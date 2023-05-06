package com.ownapp.core.extensions.view

import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.ArrayRes
import androidx.databinding.BindingAdapter
import com.ownapp.core.extensions.utility.cast
import com.ownapp.core.extensions.utility.getKeyOrNull

/**
 * Updated by Robin on 2020/12/10
 */

fun AutoCompleteTextView.setArrayAdapter(@ArrayRes arrayRes: Int)
{
	setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, resources.getStringArray(arrayRes)))
}

@BindingAdapter(value = ["dropDownList", "defaultValue", "defaultText", "forceDefault", "onItemSelected"], requireAll = false)
fun <T> AutoCompleteTextView.setArrayAdapter(
	list: List<T>?,
	defaultValue: T? = null,
	defaultText: String? = null,
	isForceDefault: Boolean = false,
	onItemSelected: ((tag: Any?, obj: T?) -> Unit)? = null
)
{
	if(!list.isNullOrEmpty())
	{
		setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, when(list[0])
		{
			is Pair<*, *> -> list.map { it.cast<Pair<T, String>>()?.second.toString() }
			is Triple<*, *, *> -> list.map { it.cast<Triple<T, String, String>>()?.second.toString() }
			else -> list
		}))

		setOnItemClickListener { _, _, position, _ ->
			when(list[0])
			{
				is Pair<*, *> -> list.getOrNull(position)?.cast<Pair<T, String>>()?.let { tag = it.first }
				is Triple<*, *, *> -> list.getOrNull(position)?.cast<Triple<T, String, String>>()?.let {
					tag = it.first
					setText(it.third, false)
				}
				else -> tag = list[position].toString()
			}

			onItemSelected?.let { it(tag, list[position]) }
			clearFocus()
		}
	}
	
	tag = null

	if(!defaultText.isNullOrBlank())
		setText(defaultText, false)
	else if(!isForceDefault)
		list?.firstOrNull()?.let { setArraySelection(list, it) }

	if(!list.isNullOrEmpty() && defaultValue != null)
		setArraySelection(list, defaultValue)
}

@BindingAdapter(value = ["dropDownMap", "defaultValue", "defaultText", "forceDefault", "onItemSelected"], requireAll = false)
fun AutoCompleteTextView.setArrayAdapter(
	map: Map<*, *>?,
	defaultValue: Any? = null,
	defaultText: String? = "",
	isForceDefault: Boolean = false,
	onItemSelected: (tag: Any?, text: String) -> Unit = { _, _ -> }
)
{
	if(!map.isNullOrEmpty())
	{
		setAdapter(ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, map.values.toTypedArray()))

		setOnItemClickListener { adapterView, _, position, _ ->
			adapterView.getItemAtPosition(position).let {
				tag = map.getKeyOrNull(value = it)
				onItemSelected(tag, it.toString())
				clearFocus()
			}
		}

		map.entries.firstOrNull()?.key?.let { setArraySelection(map, it) }
	}
	
	tag = null

	if(!defaultText.isNullOrBlank())
		setText(defaultText, false)
	else if(!isForceDefault)
		map?.entries?.firstOrNull()?.key?.let { setArraySelection(map, it) }

	if (!map.isNullOrEmpty() && defaultValue != null)
		setArraySelection(map, defaultValue)
}

fun AutoCompleteTextView.setArraySelection(map: Map<*, *>, obj: Any)
{
	if(map.containsKey(obj) || map.containsValue(obj))
	{
		adapter?.run {
			for(i in 0 until count)
			{
				map.entries.find { (_, value) -> value == getItem(i) }?.key.let {
					if(getItem(i) == obj || it == obj)
					{
						setText(getItem(i).toString(), false)
						tag = it
						return
					}
				}
			}
		}
	}
}

fun <T> AutoCompleteTextView.setArraySelection(list: List<*>?, obj: T)
{
	if(!list.isNullOrEmpty())
	{
		when(list[0])
		{
			is Pair<*, *> -> list.cast<List<Pair<T, String>>>()?.find { it.first == obj }?.let {
				tag = it.first
				setText(it.second, false)
			}
			is Triple<*, *, *> -> list.cast<List<Triple<T, String, String>>>()?.find { it.first == obj }?.let {
				tag = it.first
				setText(it.third, false)
			}
			else -> list.find { it == obj }?.let {
				tag = it.toString()
				setText(it.toString(), false)
			}
		}
	}
}

