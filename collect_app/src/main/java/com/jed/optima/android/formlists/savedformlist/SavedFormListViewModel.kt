package com.jed.optima.android.formlists.savedformlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.instancemanagement.userVisibleInstanceName
import com.jed.optima.androidshared.async.TrackableWorker
import com.jed.optima.androidshared.data.Consumable
import com.jed.optima.async.Scheduler
import com.jed.optima.async.flowOnBackground
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.shared.settings.Settings

class SavedFormListViewModel(
    scheduler: Scheduler,
    private val settings: Settings,
    private val instancesDataService: InstancesDataService,
    val projectId: String
) : ViewModel() {

    private val _sortOrder =
        MutableStateFlow(SortOrder.entries[settings.getInt(ProjectKeys.KEY_SAVED_FORM_SORT_ORDER)])
    var sortOrder: SortOrder = _sortOrder.value
        set(value) {
            settings.save(ProjectKeys.KEY_SAVED_FORM_SORT_ORDER, value.ordinal)
            _sortOrder.value = value
            field = value
        }

    private val _filterText = MutableStateFlow("")
    var filterText: String = ""
        set(value) {
            field = value
            _filterText.value = value
        }

    val formsToDisplay: LiveData<List<com.jed.optima.forms.instances.Instance>> = instancesDataService.getInstances(projectId)
        .map { instances -> instances.filter { instance -> instance.deletedDate == null } }
        .combine(_sortOrder) { instances, order ->
            when (order) {
                SortOrder.DATE_DESC -> instances.sortedByDescending { it.lastStatusChangeDate }
                SortOrder.NAME_DESC -> instances.sortedByDescending { it.userVisibleInstanceName().lowercase() }
                SortOrder.NAME_ASC -> instances.sortedBy { it.userVisibleInstanceName().lowercase() }
                SortOrder.DATE_ASC -> instances.sortedBy { it.lastStatusChangeDate }
            }
        }.combine(_filterText) { instances, filter ->
            instances.filter { it.userVisibleInstanceName().contains(filter, ignoreCase = true) }
        }.flowOnBackground(scheduler).asLiveData()

    private val worker = TrackableWorker(scheduler)
    val isDeleting: LiveData<Boolean> = worker.isWorking

    fun deleteForms(databaseIds: LongArray): LiveData<Consumable<Int>?> {
        val result = MutableLiveData<Consumable<Int>?>(null)
        worker.immediate(
            background = {
                instancesDataService.deleteInstances(projectId, databaseIds)
            },
            foreground = { instancesDeleted ->
                if (instancesDeleted) {
                    result.value = Consumable(databaseIds.count())
                } else {
                    result.value = Consumable(0)
                }
            }
        )

        return result
    }

    enum class SortOrder {
        DATE_DESC,
        NAME_ASC,
        NAME_DESC,
        DATE_ASC
    }
}
