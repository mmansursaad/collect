package com.jed.optima.android.projects

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jed.optima.android.databinding.ProjectSettingsDialogLayoutBinding
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.mainmenu.CurrentProjectViewModel
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.android.preferences.screens.ProjectPreferencesActivity
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.settings.SettingsProvider
import javax.inject.Inject

class ProjectSettingsDialog(private val viewModelFactory: ViewModelProvider.Factory) :
    DialogFragment() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var settingsProvider: SettingsProvider

    lateinit var binding: ProjectSettingsDialogLayoutBinding

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)

        currentProjectViewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        )[CurrentProjectViewModel::class.java]
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = ProjectSettingsDialogLayoutBinding.inflate(LayoutInflater.from(context))

        currentProjectViewModel.currentProject.observe(this) {
            if (it != null) {
                binding.currentProject.setupView(it, settingsProvider.getUnprotectedSettings())
                binding.currentProject.contentDescription =
                    getString(com.jed.optima.strings.R.string.using_project, it.name)

                inflateListOfInActiveProjects(requireContext(), it)
            }
        }

        binding.closeIcon.setOnClickListener {
            dismiss()
        }

        binding.generalSettingsButton.setOnClickListener {
            startActivity(Intent(requireContext(), ProjectPreferencesActivity::class.java))
            dismiss()
        }

        binding.addProjectButton.setOnClickListener {
            DialogFragmentUtils.showIfNotShowing(
                QrCodeProjectCreatorDialog::class.java,
                requireActivity().supportFragmentManager
            )
            dismiss()
        }

        /*binding.aboutButton.setOnClickListener {
            startActivity(Intent(requireContext(), AboutActivity::class.java))
            dismiss()
        }*/

        return MaterialAlertDialogBuilder(requireContext())
            .setView(binding.root)
            .create()
    }

    private fun inflateListOfInActiveProjects(context: Context, currentProject: Project.Saved) {
        if (projectsRepository.getAll().none { it.uuid != currentProject.uuid }) {
            binding.topDivider.visibility = INVISIBLE
        } else {
            binding.topDivider.visibility = VISIBLE
        }

        projectsRepository.getAll().filter {
            it.uuid != currentProject.uuid
        }.forEach { project ->
            val projectView = ProjectListItemView(context)

            projectView.setOnClickListener {
                switchProject(project)
            }

            projectView.setupView(project, settingsProvider.getUnprotectedSettings(project.uuid))
            projectView.contentDescription =
                getString(com.jed.optima.strings.R.string.switch_to_project, project.name)
            binding.projectList.addView(projectView)
        }
    }

    private fun switchProject(project: Project.Saved) {
        currentProjectViewModel.setCurrentProject(project)

        com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
            requireActivity(),
            MainMenuActivity::class.java
        )
        ToastUtils.showLongToast(
            getString(com.jed.optima.strings.R.string.switched_project, project.name)
        )
        dismiss()
    }
}
