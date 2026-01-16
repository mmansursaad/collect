package com.yedc.android.formmanagement.formmap

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import com.yedc.android.R
import com.yedc.android.instancemanagement.getStatusDescription
import com.yedc.android.instancemanagement.showAsEditable
import com.yedc.android.instancemanagement.userVisibleInstanceName
import com.yedc.androidshared.livedata.MutableNonNullLiveData
import com.yedc.androidshared.livedata.NonNullLiveData
import com.yedc.async.Scheduler
import com.yedc.geo.selection.IconifiedText
import com.yedc.geo.selection.MappableSelectItem
import com.yedc.geo.selection.SelectionMapData
import com.yedc.geo.selection.Status
import com.yedc.maps.MapPoint
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProtectedProjectKeys
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class FormMapViewModel(
    private val resources: Resources,
    private val formId: Long,
    private val formsRepository: com.yedc.forms.FormsRepository,
    private val instancesRepository: com.yedc.forms.instances.InstancesRepository,
    private val settingsProvider: SettingsProvider,
    private val scheduler: Scheduler
) : ViewModel(), SelectionMapData {

    private var _form: com.yedc.forms.Form? = null

    private val mapTitle = MutableLiveData<String?>()
    private var mappableItems = MutableLiveData<List<MappableSelectItem>>(null)
    private var itemCount = MutableNonNullLiveData(0)
    private val isLoading = MutableNonNullLiveData(false)

    override fun getMapTitle(): LiveData<String?> {
        return mapTitle
    }

    override fun getItemType(): String {
        return resources.getString(com.yedc.strings.R.string.saved_forms)
    }

    override fun getItemCount(): NonNullLiveData<Int> {
        return itemCount
    }

    override fun getMappableItems(): LiveData<List<MappableSelectItem>?> {
        return mappableItems
    }

    override fun isLoading(): NonNullLiveData<Boolean> {
        return isLoading
    }

    fun load() {
        isLoading.value = true

        scheduler.immediate(
            background = {
                val form = _form ?: formsRepository.get(formId)!!.also { _form = it }
                val instances = instancesRepository.getAllByFormId(form.formId)
                val items = mutableListOf<MappableSelectItem>()

                for (instance in instances) {
                    if (instance.geometry != null && instance.geometryType == com.yedc.forms.instances.Instance.GEOMETRY_TYPE_POINT) {
                        try {
                            val geometry = JSONObject(instance.geometry)
                            val coordinates = geometry.getJSONArray("coordinates")

                            // In GeoJSON, longitude comes before latitude.
                            val lon = coordinates.getDouble(0)
                            val lat = coordinates.getDouble(1)

                            items.add(createItem(instance, lat, lon))
                        } catch (e: JSONException) {
                            Timber.w("Invalid JSON in instances table: %s", instance.geometry)
                        }
                    }
                }

                Triple(form.displayName, items, instances.size)
            },
            foreground = {
                mapTitle.value = it.first
                mappableItems.value = it.second as List<MappableSelectItem>
                itemCount.value = it.third
                isLoading.value = false
            }
        )
    }

    private fun createItem(
        instance: com.yedc.forms.instances.Instance,
        latitude: Double,
        longitude: Double
    ): MappableSelectItem {
        val instanceLastStatusChangeDate = instance.getStatusDescription(resources)

        return if (instance.deletedDate != null) {
            val deletedTime = resources.getString(com.yedc.strings.R.string.deleted_on_date_at_time)
            val dateFormat = SimpleDateFormat(
                deletedTime,
                Locale.getDefault()
            )

            val info = "$instanceLastStatusChangeDate\n${dateFormat.format(instance.deletedDate)}"
            MappableSelectItem.MappableSelectPoint(
                instance.dbId,
                instance.userVisibleInstanceName(resources),
                point = MapPoint(latitude, longitude),
                smallIcon = getDrawableIdForStatus(instance.status, false),
                largeIcon = getDrawableIdForStatus(instance.status, true),
                info = info
            )
        } else if (!instance.canEditWhenComplete() && listOf(
                com.yedc.forms.instances.Instance.STATUS_COMPLETE,
                com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED,
                com.yedc.forms.instances.Instance.STATUS_SUBMITTED
            ).contains(instance.status)
        ) {
            val info = "$instanceLastStatusChangeDate\n${resources.getString(com.yedc.strings.R.string.cannot_edit_completed_form)}"
            MappableSelectItem.MappableSelectPoint(
                instance.dbId,
                instance.userVisibleInstanceName(resources),
                point = MapPoint(latitude, longitude),
                smallIcon = getDrawableIdForStatus(instance.status, false),
                largeIcon = getDrawableIdForStatus(instance.status, true),
                info = info
            )
        } else {
            val action =
                if (instance.showAsEditable(settingsProvider)) {
                    createEditAction()
                } else {
                    createViewAction()
                }

            MappableSelectItem.MappableSelectPoint(
                instance.dbId,
                instance.userVisibleInstanceName(resources),
                point = MapPoint(latitude, longitude),
                smallIcon = getDrawableIdForStatus(instance.status, false),
                largeIcon = getDrawableIdForStatus(instance.status, true),
                info = instanceLastStatusChangeDate,
                action = action,
                status = instanceStatusToMappableSelectionItemStatus(instance)
            )
        }
    }

    private fun instanceStatusToMappableSelectionItemStatus(instance: com.yedc.forms.instances.Instance): Status? {
        return when (instance.status) {
            com.yedc.forms.instances.Instance.STATUS_INVALID, com.yedc.forms.instances.Instance.STATUS_INCOMPLETE -> Status.ERRORS
            com.yedc.forms.instances.Instance.STATUS_VALID, com.yedc.forms.instances.Instance.STATUS_NEW_EDIT -> Status.NO_ERRORS
            else -> null
        }
    }

    private fun createViewAction(): IconifiedText {
        return IconifiedText(
            R.drawable.ic_visibility,
            resources.getString(com.yedc.strings.R.string.view_data)
        )
    }

    private fun createEditAction(): IconifiedText {
        val canEditSaved = settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)

        return IconifiedText(
            if (canEditSaved) R.drawable.ic_edit else R.drawable.ic_visibility,
            resources.getString(if (canEditSaved) com.yedc.strings.R.string.edit_data else com.yedc.strings.R.string.view_data)
        )
    }

    private fun getDrawableIdForStatus(status: String, enlarged: Boolean): Int {
        return when (status) {
            com.yedc.forms.instances.Instance.STATUS_INCOMPLETE, com.yedc.forms.instances.Instance.STATUS_VALID, com.yedc.forms.instances.Instance.STATUS_INVALID, com.yedc.forms.instances.Instance.STATUS_NEW_EDIT -> if (enlarged) R.drawable.ic_room_form_state_incomplete_48dp else R.drawable.ic_room_form_state_incomplete_24dp
            com.yedc.forms.instances.Instance.STATUS_COMPLETE -> if (enlarged) R.drawable.ic_room_form_state_complete_48dp else R.drawable.ic_room_form_state_complete_24dp
            com.yedc.forms.instances.Instance.STATUS_SUBMITTED -> if (enlarged) R.drawable.ic_room_form_state_submitted_48dp else R.drawable.ic_room_form_state_submitted_24dp
            com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED -> if (enlarged) R.drawable.ic_room_form_state_submission_failed_48dp else R.drawable.ic_room_form_state_submission_failed_24dp
            else -> com.yedc.icons.R.drawable.ic_map_point
        }
    }
}
