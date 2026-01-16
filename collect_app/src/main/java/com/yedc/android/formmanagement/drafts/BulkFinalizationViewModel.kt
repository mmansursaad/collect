package com.yedc.android.formmanagement.drafts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.yedc.android.instancemanagement.FinalizeAllResult
import com.yedc.android.instancemanagement.InstancesDataService
import com.yedc.androidshared.data.Consumable
import com.yedc.androidshared.livedata.MutableNonNullLiveData
import com.yedc.androidshared.livedata.NonNullLiveData
import com.yedc.async.Scheduler
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProtectedProjectKeys

class BulkFinalizationViewModel(
    private val projectId: String,
    private val scheduler: Scheduler,
    private val instancesDataService: InstancesDataService,
    settingsProvider: SettingsProvider
) {
    private val _finalizedForms = MutableLiveData<Consumable<FinalizeAllResult>>()
    val finalizedForms: LiveData<Consumable<FinalizeAllResult>> = _finalizedForms

    private val _isFinalizing = MutableNonNullLiveData(false)
    val isFinalizing: NonNullLiveData<Boolean> = _isFinalizing

    val draftsCount = instancesDataService.getEditableCount(projectId).asLiveData()
    val isEnabled =
        settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_BULK_FINALIZE)

    fun finalizeAllDrafts() {
        _isFinalizing.value = true

        scheduler.immediate(
            background = {
                instancesDataService.finalizeAllDrafts(projectId)
            },
            foreground = {
                _isFinalizing.value = false
                _finalizedForms.value = Consumable(it)
            }
        )
    }
}
