package com.yedc.android.activities

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.activity.viewModels
import androidx.core.text.color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.yedc.analytics.Analytics
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.databinding.FirstLaunchLayoutBinding
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.android.projects.ManualProjectCreatorDialog
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.projects.QrCodeProjectCreatorDialog
import com.yedc.android.version.VersionInformation
import com.yedc.androidshared.system.ContextUtils.getThemeAttributeValue
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.async.Scheduler
import com.yedc.material.MaterialProgressDialogFragment
import com.yedc.mobiledevicemanagement.MDMConfigObserver
import com.yedc.projects.Project
import com.yedc.projects.ProjectsRepository
import com.yedc.settings.SettingsProvider
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject
import android.content.Intent // <--- For using Intent
import android.net.Uri       // <--- For using Uri

class FirstLaunchActivity : LocalizedActivity() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var versionInformation: VersionInformation

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var mdmConfigObserver: MDMConfigObserver

    private val viewModel: FirstLaunchViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FirstLaunchViewModel(scheduler, projectsRepository, projectsDataService) as T
            }
        }
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)

        FirstLaunchLayoutBinding.inflate(layoutInflater).apply {
            setContentView(this.root)

            MaterialProgressDialogFragment.showOn(
                this@FirstLaunchActivity,
                viewModel.isLoading,
                supportFragmentManager
            ) {
                MaterialProgressDialogFragment().also { dialog ->
                    dialog.message = getString(com.yedc.strings.R.string.loading)
                }
            }

            viewModel.currentProject.observe(this@FirstLaunchActivity) { currentProject ->
                if (currentProject != null) {
                    _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                        this@FirstLaunchActivity,
                        MainMenuActivity::class.java
                    )
                }
            }

            viewModel.isLoading.observe(this@FirstLaunchActivity) { isLoading ->
                if (!isLoading) {
                    _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                        this@FirstLaunchActivity,
                        MainMenuActivity::class.java
                    )
                }
            }

            configureViaQrButton.setOnClickListener {
                DialogFragmentUtils.showIfNotShowing(
                    QrCodeProjectCreatorDialog::class.java,
                    supportFragmentManager
                )
            }

            configureManuallyButton.setOnClickListener {
                DialogFragmentUtils.showIfNotShowing(
                    ManualProjectCreatorDialog::class.java,
                    supportFragmentManager
                )
            }

            appName.text = String.format(
                "%s %s",
                getString(com.yedc.strings.R.string.collect_app_name),
                getString(com.yedc.strings.R.string.custom_version_string)
                //versionInformation.versionToDisplay
            )

            dontHaveServer.apply {
                text = SpannableStringBuilder()
                    .append(getString(com.yedc.strings.R.string.dont_have_project))
                    .append(" ")
                    .color(getThemeAttributeValue(context, com.google.android.material.R.attr.colorAccent)) {
                        append(getString(com.yedc.strings.R.string.try_demo))
                    }

                setOnClickListener {
                    // 1. Define the URL you want to open as a String
                    val urlToOpen = "https://drive.google.com/drive/u/0/folders/1UBQnM38lsBXGBmNukeNPc9hCPUF-YIVj"

                    // 2. Create an Intent with the ACTION_VIEW action
                    //    This tells Android you want to view something.
                    val intent = Intent(Intent.ACTION_VIEW)

                    // 3. Set the data for the Intent. This is your URL.
                    //    Uri.parse() converts your string URL into a format Android understands.
                    intent.data = Uri.parse(urlToOpen)

                    // 4. Start the activity (which will be the web browser)
                    //    `this@FirstLaunchActivity` refers to your current screen (the Activity).
                    startActivity(intent)

                    // --- END OF CHANGES ---

                    //viewModel.tryDemo()
                }
            }
        }
        lifecycle.addObserver(mdmConfigObserver)
    }
}

private class FirstLaunchViewModel(
    private val scheduler: Scheduler,
    private val projectsRepository: ProjectsRepository,
    private val projectsDataService: ProjectsDataService
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val currentProject = projectsDataService.getCurrentProject().asLiveData()

    fun tryDemo() {
        Analytics.log(AnalyticsEvents.TRY_DEMO)

        _isLoading.value = true
        scheduler.immediate(
            background = {
                projectsRepository.save(Project.DEMO_PROJECT)
                projectsDataService.setCurrentProject(Project.DEMO_PROJECT_ID)
            },
            foreground = {
                _isLoading.value = false
            }
        )
    }
}
