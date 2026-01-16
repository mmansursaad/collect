package com.yedc.android.preferences.screens

import android.app.Application
import android.content.Context
import androidx.preference.Preference
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.yedc.android.application.initialization.AnalyticsInitializer
import com.yedc.android.application.initialization.MapsInitializer
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.storage.StoragePathProvider
import com.yedc.fragmentstest.FragmentScenarioLauncherRule
import com.yedc.maps.layers.ReferenceLayer
import com.yedc.maps.layers.ReferenceLayerRepository
import com.yedc.projects.Project
import com.yedc.projects.ProjectsRepository
import com.yedc.settings.InMemSettingsProvider
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys
import com.yedc.shared.TempFiles
import com.yedc.shared.strings.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class MapsPreferencesFragmentTest {

    @get:Rule
    val launcherRule = FragmentScenarioLauncherRule()

    private val project = Project.DEMO_PROJECT
    private val projectsDataService = mock<ProjectsDataService>().apply {
        whenever(requireCurrentProject()).thenReturn(project)
    }
    private val projectsRepository = mock<ProjectsRepository>().apply {
        whenever(get(project.uuid)).thenReturn(project)
    }
    private val referenceLayerRepository = mock<ReferenceLayerRepository>()
    private val settingsProvider = InMemSettingsProvider()

    @Before
    fun setup() {
        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesCurrentProjectProvider(
                application: Application,
                settingsProvider: SettingsProvider,
                projectsRepository: ProjectsRepository,
                analyticsInitializer: AnalyticsInitializer,
                context: Context,
                mapsInitializer: MapsInitializer
            ): ProjectsDataService {
                return projectsDataService
            }

            override fun providesProjectsRepository(
                uuidGenerator: UUIDGenerator,
                gson: Gson,
                settingsProvider: SettingsProvider
            ): ProjectsRepository {
                return projectsRepository
            }

            override fun providesSettingsProvider(context: Context): SettingsProvider {
                return settingsProvider
            }

            override fun providesReferenceLayerRepository(
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider
            ): ReferenceLayerRepository {
                return referenceLayerRepository
            }
        })
    }

    @Test
    fun `if saved layer does not exist it is set to 'none'`() {
        val settings = settingsProvider.getUnprotectedSettings()
        settings.save(ProjectKeys.KEY_REFERENCE_LAYER, "blah")

        launcherRule.launch(MapsPreferencesFragment::class.java)

        assertThat(settings.getString(ProjectKeys.KEY_REFERENCE_LAYER), equalTo(null))
    }

    @Test
    fun `if saved layer exist its name is displayed`() {
        val settings = settingsProvider.getUnprotectedSettings()
        settings.save(ProjectKeys.KEY_REFERENCE_LAYER, "blah")
        val layer = ReferenceLayer("blah", TempFiles.createTempFile(), "blah")
        whenever(referenceLayerRepository.get("blah")).thenReturn(layer)

        val scenario = launcherRule.launch(MapsPreferencesFragment::class.java)

        scenario.onFragment {
            assertThat(
                it.findPreference<Preference>(ProjectKeys.KEY_REFERENCE_LAYER)!!.summary,
                equalTo("blah")
            )
        }
    }
}
