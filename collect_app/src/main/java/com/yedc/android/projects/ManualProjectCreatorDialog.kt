package com.yedc.android.projects

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.doOnTextChanged
import com.yedc.analytics.Analytics
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.configure.qr.AppConfigurationGenerator
import com.yedc.android.databinding.ManualProjectCreatorDialogLayoutBinding
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.android.projects.DuplicateProjectConfirmationKeys.MATCHING_PROJECT
import com.yedc.android.projects.DuplicateProjectConfirmationKeys.SETTINGS_JSON
import com.yedc.android.utilities.SoftKeyboardController
import com.yedc.androidshared.ui.DialogFragmentUtils
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.androidshared.utils.Validator
import com.yedc.projects.ProjectCreator
import com.yedc.projects.ProjectsRepository
import com.yedc.projects.SettingsConnectionMatcher
import com.yedc.settings.SettingsProvider
import javax.inject.Inject

class ManualProjectCreatorDialog :
    com.yedc.material.MaterialFullScreenDialogFragment(),
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
        toolbar.setTitle(com.yedc.strings.R.string.add_project)
    }

    private fun handleAddingNewProject() {
        if (!Validator.isUrlValid(binding.urlInputText.text?.trim().toString())) {
            ToastUtils.showShortToast(com.yedc.strings.R.string.url_error)
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
        _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            getString(com.yedc.strings.R.string.switched_project, projectsDataService.requireCurrentProject().name)
        )
    }

    override fun switchToProject(uuid: String) {
        projectsDataService.setCurrentProject(uuid)
        _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(activity, MainMenuActivity::class.java)
        ToastUtils.showLongToast(
            getString(
                com.yedc.strings.R.string.switched_project,
                projectsDataService.requireCurrentProject().name
            )
        )
    }
}
