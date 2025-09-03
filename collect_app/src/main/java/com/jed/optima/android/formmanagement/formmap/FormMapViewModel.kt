package com.jed.optima.android.formmanagement.formmap

import android.content.res.Resources
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.json.JSONException
import org.json.JSONObject
import com.jed.optima.android.R
import com.jed.optima.android.instancemanagement.getStatusDescription
import com.jed.optima.android.instancemanagement.showAsEditable
import com.jed.optima.android.instancemanagement.userVisibleInstanceName
import com.jed.optima.androidshared.livedata.MutableNonNullLiveData
import com.jed.optima.androidshared.livedata.NonNullLiveData
import com.jed.optima.async.Scheduler
import com.jed.optima.geo.selection.IconifiedText
import com.jed.optima.geo.selection.MappableSelectItem
import com.jed.optima.geo.selection.SelectionMapData
import com.jed.optima.geo.selection.Status
import com.jed.optima.maps.MapPoint
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.keys.ProtectedProjectKeys
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale

class FormMapViewModel(
    private val resources: Resources,
    private val formId: Long,
    private val formsRepository: com.jed.optima.forms.FormsRepository,
    private val instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
    private val settingsProvider: SettingsProvider,
    private val scheduler: Scheduler
) : ViewModel(), SelectionMapData {

    private var _form: com.jed.optima.forms.Form? = null

    private val mapTitle = MutableLiveData<String?>()
    private var mappableItems = MutableLiveData<List<MappableSelectItem>>(null)
    private var itemCount = MutableNonNullLiveData(0)
    private val isLoading = MutableNonNullLiveData(false)

    override fun getMapTitle(): LiveData<String?> {
        return mapTitle
    }

    override fun getItemType(): String {
        return resources.getString(com.jed.optima.strings.R.string.saved_forms)
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
                    if (instance.geometry != null && instance.geometryType == com.jed.optima.forms.instances.Instance.GEOMETRY_TYPE_POINT) {
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
        instance: com.jed.optima.forms.instances.Instance,
        latitude: Double,
        longitude: Double
    ): MappableSelectItem {
        val instanceLastStatusChangeDate = instance.getStatusDescription(resources)

        return if (instance.deletedDate != null) {
            val deletedTime = resources.getString(com.jed.optima.strings.R.string.deleted_on_date_at_time)
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
                com.jed.optima.forms.instances.Instance.STATUS_COMPLETE,
                com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED,
                com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED
            ).contains(instance.status)
        ) {
            val info = "$instanceLastStatusChangeDate\n${resources.getString(com.jed.optima.strings.R.string.cannot_edit_completed_form)}"
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

    private fun instanceStatusToMappableSelectionItemStatus(instance: com.jed.optima.forms.instances.Instance): Status? {
        return when (instance.status) {
            com.jed.optima.forms.instances.Instance.STATUS_INVALID, com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE -> Status.ERRORS
            com.jed.optima.forms.instances.Instance.STATUS_VALID, com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT -> Status.NO_ERRORS
            else -> null
        }
    }

    private fun createViewAction(): IconifiedText {
        return IconifiedText(
            R.drawable.ic_visibility,
            resources.getString(com.jed.optima.strings.R.string.view_data)
        )
    }

    private fun createEditAction(): IconifiedText {
        val canEditSaved = settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)

        return IconifiedText(
            if (canEditSaved) R.drawable.ic_edit else R.drawable.ic_visibility,
            resources.getString(if (canEditSaved) com.jed.optima.strings.R.string.edit_data else com.jed.optima.strings.R.string.view_data)
        )
    }

    private fun getDrawableIdForStatus(status: String, enlarged: Boolean): Int {
        return when (status) {
            com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE, com.jed.optima.forms.instances.Instance.STATUS_VALID, com.jed.optima.forms.instances.Instance.STATUS_INVALID, com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT -> if (enlarged) R.drawable.ic_room_form_state_incomplete_48dp else R.drawable.ic_room_form_state_incomplete_24dp
            com.jed.optima.forms.instances.Instance.STATUS_COMPLETE -> if (enlarged) R.drawable.ic_room_form_state_complete_48dp else R.drawable.ic_room_form_state_complete_24dp
            com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED -> if (enlarged) R.drawable.ic_room_form_state_submitted_48dp else R.drawable.ic_room_form_state_submitted_24dp
            com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED -> if (enlarged) R.drawable.ic_room_form_state_submission_failed_48dp else R.drawable.ic_room_form_state_submission_failed_24dp
            else -> com.jed.optima.icons.R.drawable.ic_map_point
        }
    }
}
