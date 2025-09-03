package com.jed.optima.android.mainmenu

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.jed.optima.android.instancemanagement.autosend.shouldFormBeSentAutomatically
import com.jed.optima.android.instancemanagement.canBeEdited
import com.jed.optima.android.instancemanagement.isDraft
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.utilities.ContentUriHelper
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.androidshared.data.Consumable
import com.jed.optima.async.Scheduler
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.settings.enums.StringIdEnumUtils.getFormUpdateMode
import com.jed.optima.settings.keys.ProtectedProjectKeys

class MainMenuViewModel(
    private val application: Application,
    private val versionInformation: com.jed.optima.android.version.VersionInformation,
    private val settingsProvider: SettingsProvider,
    private val instancesDataService: InstancesDataService,
    private val scheduler: Scheduler,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider,
    private val projectsDataService: ProjectsDataService
) : ViewModel() {

    val version: String
        get() = versionInformation.versionToDisplay

    val versionCommitDescription: String?
        get() {
            var commitDescription = ""
            if (versionInformation.commitCount != null) {
                commitDescription =
                    appendToCommitDescription(
                        commitDescription,
                        versionInformation.commitCount.toString()
                    )
            }
            if (versionInformation.commitSHA != null) {
                commitDescription =
                    appendToCommitDescription(commitDescription, versionInformation.commitSHA!!)
            }
            if (versionInformation.isDirty) {
                commitDescription = appendToCommitDescription(commitDescription, "dirty")
            }
            return if (commitDescription.isNotEmpty()) {
                commitDescription
            } else {
                null
            }
        }

    private val _savedForm = MutableLiveData<SavedForm>()
    val savedForm: LiveData<Consumable<SavedForm>> = _savedForm.map { Consumable(it) }

    fun shouldEditSavedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_EDIT_SAVED)
    }

    fun shouldSendFinalizedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_SEND_FINALIZED)
    }

    fun shouldViewSentFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT)
    }

    fun shouldGetBlankFormButtonBeVisible(): Boolean {
        val buttonEnabled =
            settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_GET_BLANK)
        return !isMatchExactlyEnabled() && buttonEnabled
    }

    fun shouldDeleteSavedFormButtonBeVisible(): Boolean {
        return settingsProvider.getProtectedSettings()
            .getBoolean(ProtectedProjectKeys.KEY_DELETE_SAVED)
    }

    private fun isMatchExactlyEnabled(): Boolean {
        return settingsProvider.getUnprotectedSettings()
            .getFormUpdateMode(application) == com.jed.optima.settings.enums.FormUpdateMode.MATCH_EXACTLY
    }

    private fun appendToCommitDescription(commitDescription: String, part: String): String {
        return if (commitDescription.isEmpty()) {
            part
        } else {
            "$commitDescription-$part"
        }
    }

    fun refreshInstances() {
        scheduler.immediate<Any?>({
            com.jed.optima.android.instancemanagement.InstanceDiskSynchronizer(settingsProvider).doInBackground()
            instancesDataService.update(projectsDataService.requireCurrentProject().uuid)
            null
        }) { }
    }

    private val currentProject = projectsDataService.getCurrentProject().asLiveData()
    val editableInstancesCount: LiveData<Int> = currentProject.switchMap {
        instancesDataService.getEditableCount(it!!.uuid).asLiveData()
    }

    val sendableInstancesCount: LiveData<Int> = currentProject.switchMap {
        instancesDataService.getSendableCount(it!!.uuid).asLiveData()
    }

    val sentInstancesCount: LiveData<Int> = currentProject.switchMap {
        instancesDataService.getSentCount(it!!.uuid).asLiveData()
    }

    fun setSavedForm(uri: Uri?) {
        if (uri == null) {
            return
        }

        scheduler.immediate {
            val details = getFormSavedSnackbarDetails(uri)
            if (details != null) {
                _savedForm.postValue(SavedForm(uri, details.first, details.second))
            }
        }
    }

    private fun getFormSavedSnackbarDetails(uri: Uri): Pair<Int, Int?>? {
        val instance = instancesRepositoryProvider.create().get(ContentUriHelper.getIdFromUri(uri))
        return if (instance != null) {
            val message = if (instance.isDraft()) {
                com.jed.optima.strings.R.string.form_saved_as_draft
            } else if (instance.status == com.jed.optima.forms.instances.Instance.STATUS_COMPLETE || instance.status == com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED) {
                val form = formsRepositoryProvider.create()
                    .getAllByFormIdAndVersion(instance.formId, instance.formVersion).first()
                if (form.shouldFormBeSentAutomatically(autoSendSettingsProvider.isAutoSendEnabledInSettings())) {
                    com.jed.optima.strings.R.string.form_sending
                } else {
                    com.jed.optima.strings.R.string.form_saved
                }
            } else {
                return null
            }

            val action = if (instance.canBeEdited(settingsProvider)) {
                com.jed.optima.strings.R.string.edit_form
            } else {
                if (instance.isDraft() || instance.canEditWhenComplete()) {
                    com.jed.optima.strings.R.string.view_form
                } else {
                    null
                }
            }

            return Pair(message, action)
        } else {
            null
        }
    }

    data class SavedForm(val uri: Uri, val message: Int, val action: Int?)
}
