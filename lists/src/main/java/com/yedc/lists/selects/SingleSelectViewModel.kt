package com.yedc.lists.selects

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

class SingleSelectViewModel(
    selected: String?,
    data: LiveData<List<SelectItem<*>>>
) : ViewModel() {

    private val _selected = MutableLiveData<String?>(selected)
    private val selected = _root_ide_package_.com.yedc.androidshared.livedata.LiveDataUtils.zip(_selected, data).map { (selected, data) ->
        selected.takeIf { id -> data.any { it.id == id } }
    }

    fun getSelected(): LiveData<String?> {
        return selected
    }

    fun select(item: String) {
        _selected.value = item
    }

    fun clear() {
        _selected.value = null
    }
}
