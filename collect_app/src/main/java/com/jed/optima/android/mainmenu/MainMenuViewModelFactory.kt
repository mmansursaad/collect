package com.jed.optima.android.mainmenu

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.utilities.FormsRepositoryProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.async.Scheduler
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.settings.SettingsProvider

open class MainMenuViewModelFactory(
    private val versionInformation: com.jed.optima.android.version.VersionInformation,
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
