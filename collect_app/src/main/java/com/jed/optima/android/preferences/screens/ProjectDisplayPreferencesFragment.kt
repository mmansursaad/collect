package com.jed.optima.android.preferences.screens

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.R
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.androidshared.ColorPickerDialog
import com.jed.optima.androidshared.ColorPickerViewModel
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.androidshared.ui.OneSignTextWatcher
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard
import com.jed.optima.projects.Project
import com.jed.optima.projects.ProjectsRepository
import com.jed.optima.shared.PathUtils
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class ProjectDisplayPreferencesFragment :
    com.jed.optima.android.preferences.screens.BaseAdminPreferencesFragment(), Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var storagePathProvider: StoragePathProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
        val colorPickerViewModel = ViewModelProvider(requireActivity()).get(
            ColorPickerViewModel::class.java
        )
        colorPickerViewModel.pickedColor.observe(
            this,
            { color: String ->
                Analytics.log(AnalyticsEvents.CHANGE_PROJECT_COLOR)

                val (uuid, name, icon) = projectsDataService.requireCurrentProject()
                projectsRepository.save(Project.Saved(uuid, name, icon, color))
                findPreference<Preference>(PROJECT_COLOR_KEY)!!.summaryProvider =
                    ProjectDetailsSummaryProvider(
                        PROJECT_COLOR_KEY,
                        projectsDataService
                    )
            }
        )
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setPreferencesFromResource(R.xml.project_display_preferences, rootKey)
        findPreference<Preference>(PROJECT_COLOR_KEY)!!.onPreferenceClickListener = this

        findPreference<Preference>(PROJECT_NAME_KEY)!!.summaryProvider =
            ProjectDetailsSummaryProvider(
                PROJECT_NAME_KEY,
                projectsDataService
            )
        findPreference<Preference>(PROJECT_ICON_KEY)!!.summaryProvider =
            ProjectDetailsSummaryProvider(
                PROJECT_ICON_KEY,
                projectsDataService
            )
        findPreference<Preference>(PROJECT_COLOR_KEY)!!.summaryProvider =
            ProjectDetailsSummaryProvider(
                PROJECT_COLOR_KEY,
                projectsDataService
            )
        findPreference<Preference>(PROJECT_NAME_KEY)!!.onPreferenceChangeListener = this
        findPreference<Preference>(PROJECT_ICON_KEY)!!.onPreferenceChangeListener = this
        (findPreference<Preference>(PROJECT_NAME_KEY) as EditTextPreference).text =
            projectsDataService.requireCurrentProject().name
        (findPreference<Preference>(PROJECT_ICON_KEY) as EditTextPreference).text =
            projectsDataService.requireCurrentProject().icon
        (findPreference<Preference>(PROJECT_ICON_KEY) as EditTextPreference).setOnBindEditTextListener { editText: EditText ->
            editText.addTextChangedListener(
                OneSignTextWatcher(editText)
            )
        }
    }

    private class ProjectDetailsSummaryProvider(
        private val key: String,
        private val projectsDataService: ProjectsDataService
    ) : Preference.SummaryProvider<Preference> {
        override fun provideSummary(preference: Preference): CharSequence {
            return when (key) {
                PROJECT_NAME_KEY -> projectsDataService.requireCurrentProject().name
                PROJECT_ICON_KEY -> projectsDataService.requireCurrentProject().icon
                PROJECT_COLOR_KEY -> {
                    val summary: Spannable = SpannableString("■")
                    summary.setSpan(
                        ForegroundColorSpan(
                            Color.parseColor(
                                projectsDataService.requireCurrentProject().color
                            )
                        ),
                        0,
                        summary.length,
                        0
                    )
                    summary
                }
                else -> ""
            }
        }
    }

    override fun onPreferenceClick(preference: Preference): Boolean {
        if (MultiClickGuard.allowClick(javaClass.name)) {
            when (preference.key) {
                PROJECT_COLOR_KEY -> {
                    val (_, _, icon, color) = projectsDataService.requireCurrentProject()
                    val bundle = Bundle()
                    bundle.putString(ColorPickerDialog.CURRENT_COLOR, color)
                    bundle.putString(ColorPickerDialog.CURRENT_ICON, icon)
                    DialogFragmentUtils.showIfNotShowing(
                        ColorPickerDialog::class.java,
                        bundle,
                        requireActivity().supportFragmentManager
                    )
                }
            }
            return true
        }
        return false
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        val (uuid, name, icon, color) = projectsDataService.requireCurrentProject()
        when (preference.key) {
            PROJECT_NAME_KEY -> {
                Analytics.log(AnalyticsEvents.CHANGE_PROJECT_NAME)

                val sanitizedOldProjectName = PathUtils.getPathSafeFileName(name)
                try {
                    File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedOldProjectName).delete()
                } catch (e: Exception) {
                    Timber.e(
                        Error(
                            com.jed.optima.android.utilities.FileUtils.getFilenameError(
                                name
                            )
                        )
                    )
                }

                val sanitizedNewProjectName = PathUtils.getPathSafeFileName(newValue.toString())
                try {
                    File(storagePathProvider.getProjectRootDirPath() + File.separator + sanitizedNewProjectName).createNewFile()
                } catch (e: Exception) {
                    Timber.e(
                        Error(
                            com.jed.optima.android.utilities.FileUtils.getFilenameError(
                                newValue as String
                            )
                        )
                    )
                }

                projectsRepository.save(
                    Project.Saved(
                        uuid,
                        newValue.toString(),
                        icon,
                        color
                    )
                )
            }
            PROJECT_ICON_KEY -> {
                Analytics.log(AnalyticsEvents.CHANGE_PROJECT_ICON)

                projectsRepository.save(
                    Project.Saved(
                        uuid,
                        name,
                        newValue.toString(),
                        color
                    )
                )
            }
        }
        return true
    }

    companion object {
        const val PROJECT_NAME_KEY = "project_name"
        const val PROJECT_ICON_KEY = "project_icon"
        const val PROJECT_COLOR_KEY = "project_color"
    }
}
