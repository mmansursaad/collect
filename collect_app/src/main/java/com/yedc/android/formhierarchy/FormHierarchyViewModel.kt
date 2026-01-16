package com.yedc.android.formhierarchy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.instance.TreeReference
import com.yedc.android.instancemanagement.InstanceEditResult
import com.yedc.android.instancemanagement.InstancesDataService
import com.yedc.androidshared.async.TrackableWorker
import com.yedc.androidshared.data.Consumable
import com.yedc.async.Scheduler

class FormHierarchyViewModel(scheduler: Scheduler) : ViewModel() {
    private val trackableWorker = TrackableWorker(scheduler)
    val isEditingInstance: LiveData<Boolean> = trackableWorker.isWorking

    private val _instanceEditResult = MutableLiveData<Consumable<InstanceEditResult>>()
    val instanceEditResult: LiveData<Consumable<InstanceEditResult>> = _instanceEditResult

    var contextGroupRef: TreeReference? = null
    var screenIndex: FormIndex? = null
    var repeatGroupPickerIndex: FormIndex? = null
    var currentIndex: FormIndex? = null
    var elementsToDisplay: List<HierarchyItem>? = null
    var startIndex: FormIndex? = null

    fun shouldShowRepeatGroupPicker() = repeatGroupPickerIndex != null

    fun editInstance(
        instanceFilePath: String,
        instancesDataService: InstancesDataService,
        projectId: String
    ) {
        trackableWorker.immediate(
            background = {
                instancesDataService.editInstance(instanceFilePath, projectId)
            },
            foreground = { instanceEditResult ->
                _instanceEditResult.value = Consumable(instanceEditResult)
            }
        )
    }

    class Factory(private val scheduler: Scheduler) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return FormHierarchyViewModel(scheduler) as T
        }
    }
}
