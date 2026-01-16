package com.yedc.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.yedc.android.formmanagement.FormsDataService
import com.yedc.android.injection.config.ProjectDependencyModuleFactory
import com.yedc.android.notifications.Notifier

@RunWith(AndroidJUnit4::class)
class SyncFormsTaskSpecTest {
    private val formsDataService = mock<FormsDataService>()

    @Before
    fun setup() {
        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesFormsUpdater(
                application: Application,
                notifier: Notifier,
                projectDependencyModuleFactory: ProjectDependencyModuleFactory
            ): FormsDataService {
                return formsDataService
            }
        })
    }

    @Test
    fun `when isLastUniqueExecution equals true task calls synchronize with notify true`() {
        val inputData = HashMap<String, String>()
        inputData[TaskData.DATA_PROJECT_ID] = "projectId"
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        verify(formsDataService).matchFormsWithServer("projectId", true)
    }

    @Test
    fun `when isLastUniqueExecution equals false task calls synchronize with notify false`() {
        val inputData = HashMap<String, String>()
        inputData[TaskData.DATA_PROJECT_ID] = "projectId"
        SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, false).get()
        verify(formsDataService).matchFormsWithServer("projectId", false)
    }

    @Test
    fun `task returns result from FormUpdater`() {
        val inputData = HashMap<String, String>()
        inputData[TaskData.DATA_PROJECT_ID] = "projectId"

        whenever(formsDataService.matchFormsWithServer("projectId", true)).thenReturn(true)
        var result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, true).get()
        assertThat(result, `is`(true))

        whenever(formsDataService.matchFormsWithServer("projectId")).thenReturn(false)
        result = SyncFormsTaskSpec().getTask(ApplicationProvider.getApplicationContext(), inputData, false).get()
        assertThat(result, `is`(false))
    }

    @Test
    fun `maxRetries should be limited`() {
        assertThat(SyncFormsTaskSpec().maxRetries, `is`(3))
    }
}
