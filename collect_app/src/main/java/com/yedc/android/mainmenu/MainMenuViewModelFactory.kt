package com.yedc.android.mainmenu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yedc.android.instancemanagement.InstancesDataService
import com.yedc.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.async.Scheduler
import com.yedc.permissions.PermissionsChecker
import com.yedc.settings.SettingsProvider

open class MainMenuViewModelFactory(
    private val versionInformation: _root_ide_package_.com.yedc.android.version.VersionInformation,
    private val application: Application,
    private val settingsProvider: SettingsProvider,
    private val instancesDataService: InstancesDataService,
    private val scheduler: Scheduler,
    private val projectsDataService: ProjectsDataService,
    private val permissionChecker: PermissionsChecker,
    private val formsRepositoryProvider: FormsRepositoryProvider,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val autoSendSettingsProvider: AutoSendSettingsProvider
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when (modelClass) {
            MainMenuViewModel::class.java -> MainMenuViewModel(
                application,
                versionInformation,
                settingsProvider,
                instancesDataService,
                scheduler,
                formsRepositoryProvider,
                instancesRepositoryProvider,
                autoSendSettingsProvider,
                projectsDataService
            )

            CurrentProjectViewModel::class.java -> CurrentProjectViewModel(
                projectsDataService
            )

            RequestPermissionsViewModel::class.java -> RequestPermissionsViewModel(
                settingsProvider,
                permissionChecker
            )

            else -> throw IllegalArgumentException()
        } as T
    }
}
