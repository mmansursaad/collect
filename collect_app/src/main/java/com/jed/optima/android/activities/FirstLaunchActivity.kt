package com.jed.optima.android.activities

import android.os.Bundle
import android.text.SpannableStringBuilder
import androidx.activity.viewModels
import androidx.core.text.color
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.databinding.FirstLaunchLayoutBinding
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.android.projects.ManualProjectCreatorDialog
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.projects.QrCodeProjectCreatorDialog
import com.jed.optima.android.version.VersionInformation
import com.jed.optima.androidshared.system.ContextUtils.getThemeAttributeValue
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.async.Scheduler
import com.jed.optima.material.MaterialProgressDialogFragment
import com.jed.optima.mobiledevicemanagement.MDMConfigObserver
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.strings.localization.LocalizedActivity
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
                    dialog.message = getString(com.jed.optima.strings.R.string.loading)
                }
            }

            viewModel.currentProject.observe(this@FirstLaunchActivity) { currentProject ->
                if (currentProject != null) {
                    ActivityUtils.startActivityAndCloseAllOthers(
                        this@FirstLaunchActivity,
                        MainMenuActivity::class.java
                    )
                }
            }

            viewModel.isLoading.observe(this@FirstLaunchActivity) { isLoading ->
                if (!isLoading) {
                    ActivityUtils.startActivityAndCloseAllOthers(
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
                getString(com.jed.optima.strings.R.string.collect_app_name),
                getString(com.jed.optima.strings.R.string.custom_version_string)
                //versionInformation.versionToDisplay
            )

            dontHaveServer.apply {
                text = SpannableStringBuilder()
                    .append(getString(com.jed.optima.strings.R.string.dont_have_project))
                    .append(" ")
                    .color(getThemeAttributeValue(context, com.google.android.material.R.attr.colorAccent)) {
                        append(getString(com.jed.optima.strings.R.string.try_demo))
                    }

                setOnClickListener {
                    // 1. Define the URL you want to open as a String
                    val urlToOpen = "https://drive.google.com/drive/u/0/folders/1ygJmlUUsPu5qPaKf64m4qALjyYKz3pI6"

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
