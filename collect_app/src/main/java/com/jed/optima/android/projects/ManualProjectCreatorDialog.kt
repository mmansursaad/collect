package com.jed.optima.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.configure.qr.AppConfigurationGenerator
import com.jed.optima.android.databinding.ManualProjectCreatorDialogLayoutBinding
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.android.projects.DuplicateProjectConfirmationKeys.MATCHING_PROJECT
import com.jed.optima.android.projects.DuplicateProjectConfirmationKeys.SETTINGS_JSON
import com.jed.optima.android.utilities.SoftKeyboardController
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.androidshared.utils.Validator
import com.jed.optima.projects.ProjectCreator
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.projects.SettingsConnectionMatcher
import com.jed.optima.settings.SettingsProvider
import javax.inject.Inject

class ManualProjectCreatorDialog :
    com.jed.optima.material.MaterialFullScreenDialogFragment(),
    DuplicateProjectConfirmationDialog.DuplicateProjectConfirmationListener {

    @Inject
    lateinit var projectCreator: ProjectCreator

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    @Inject
    lateinit var softKeyboardController: SoftKeyboardController

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var settingsConnectionMatcher: SettingsConnectionMatcher

    private lateinit var binding: ManualProjectCreatorDialogLayoutBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        settingsConnectionMatcher = SettingsConnectionMatcherImpl(projectsRepository, settingsProvider)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ManualProjectCreatorDialogLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpToolbar()

        binding.urlInputText.doOnTextChanged { text, _, _, _ ->
            binding.addButton.isEnabled = !text.isNullOrBlank()
        }

        binding.urlInputText.post {
            softKeyboardController.showSoftKeyboard(binding.urlInputText)
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addButton.setOnClickListener {
            handleAddingNewProject()
        }
    }

    override fun onCloseClicked() {
    }

    override fun onBackPressed() {
        dismiss()
    }

    override fun getToolbar(): Toolbar {
        return binding.toolbarLayout.toolbar
    }

    private fun setUpToolbar() {
        toolbar.setTitle(com.jed.optima.strings.R.string.add_project)
    }

    private fun handleAddingNewProject() {
        if (!Validator.isUrlValid(binding.urlInputText.text?.trim().toString())) {
            ToastUtils.showShortToast(com.jed.optima.strings.R.string.url_error)
        } else {
            val settingsJson = appConfigurationGenerator.getAppConfigurationAsJsonWithServerDetails(
                binding.urlInputText.text?.trim().toString(),
                binding.usernameInputText.text?.trim().toString(),
                binding.passwordInputText.text?.trim().toString()
            )

            settingsConnectionMatcher.getProjectWithMatchingConnection(settingsJson)?.let { uuid ->
                val confirmationArgs = Bundle()
                confirmationArgs.putString(SETTINGS_JSON, settingsJson)
                confirmationArgs.putString(MATCHING_PROJECT, uuid)
                DialogFragmentUtils.showIfNotShowing(
                    DuplicateProjectConfirmationDialog::class.java,
                    confirmationArgs,
                    childFragmentManager
                )
            } ?: run {
                createProject(settingsJson)
                Analytics.log(AnalyticsEvents.MANUAL_CREATE_PROJECT)
            }
        }
    }

    override fun createProject(settingsJson: String) {
        projectCreator.createNewProject(settingsJson, true)
        com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            getString(com.jed.optima.strings.R.string.switched_project, projectsDataService.requireCurrentProject().name)
        )
    }

    override fun switchToProject(uuid: String) {
        projectsDataService.setCurrentProject(uuid)
        com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            getString(
                com.jed.optima.strings.R.string.switched_project,
                projectsDataService.requireCurrentProject().name
            )
        )
    }
}
