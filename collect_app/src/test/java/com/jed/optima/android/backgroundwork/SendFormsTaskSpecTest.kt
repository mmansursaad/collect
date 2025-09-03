package com.jed.optima.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.jed.optima.android.injection.config.ProjectDependencyModuleFactory
import com.jed.optima.android.instancemanagement.InstancesDataService
import com.jed.optima.android.notifications.Notifier
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.metadata.PropertyManager
import com.jed.optima.testshared.RobolectricHelpers

@RunWith(AndroidJUnit4::class)
class SendFormsTaskSpecTest {

    private val instancesDataService = mock<InstancesDataService>()
    private lateinit var projectId: String

    @Before
    fun setup() {
        com.jed.optima.android.support.CollectHelpers.overrideAppDependencyModule(object : com.jed.optima.android.injection.config.AppDependencyModule() {
            override fun providesInstancesDataService(
                application: Application?,
                projectsDataService: ProjectsDataService?,
                instanceSubmitScheduler: com.jed.optima.android.backgroundwork.InstanceSubmitScheduler?,
                projectsDependencyProviderFactory: ProjectDependencyModuleFactory?,
                notifier: Notifier?,
                propertyManager: PropertyManager?,
                httpInterface: com.jed.optima.openrosa.http.OpenRosaHttpInterface
            ): InstancesDataService {
                return instancesDataService
            }
        })

        RobolectricHelpers.mountExternalStorage()
        projectId = com.jed.optima.android.support.CollectHelpers.setupDemoProject()
    }

    @Test
    fun `returns false if sending instances fails`() {
        whenever(instancesDataService.sendInstances(projectId)).doReturn(false)

        val inputData = mapOf(TaskData.DATA_PROJECT_ID to projectId)
        val spec = SendFormsTaskSpec()
        val task = spec.getTask(ApplicationProvider.getApplicationContext(), inputData, true)
        assertThat(task.get(), equalTo(false))
    }

    @Test
    fun `returns true if sending instances succeeds`() {
        whenever(instancesDataService.sendInstances(projectId)).doReturn(true)

        val inputData = mapOf(TaskData.DATA_PROJECT_ID to projectId)
        val spec = SendFormsTaskSpec()
        val task = spec.getTask(ApplicationProvider.getApplicationContext(), inputData, true)
        assertThat(task.get(), equalTo(true))
    }
}
