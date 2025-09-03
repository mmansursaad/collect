package com.jed.optima.entities.browser

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.jed.optima.async.Scheduler
import com.jed.optima.entities.storage.EntitiesRepository
import com.jed.optima.entities.storage.Entity
import com.jed.optima.entities.storage.getListNames

class EntitiesViewModel(
    private val scheduler: Scheduler,
    private val entitiesRepository: EntitiesRepository
) : ViewModel() {

    private val _lists = MutableLiveData<List<String>>(emptyList())
    val lists: LiveData<List<String>> = _lists

    init {
        scheduler.immediate {
            _lists.postValue(entitiesRepository.getListNames().toList())
        }
    }

    fun getEntities(list: String): LiveData<List<Entity.Saved>> {
        val result = MutableLiveData<List<Entity.Saved>>(emptyList())
        scheduler.immediate {
            result.postValue(entitiesRepository.query(list))
        }

        return result
    }
}
