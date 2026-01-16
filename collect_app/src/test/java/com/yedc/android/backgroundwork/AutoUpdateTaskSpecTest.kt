package com.yedc.android.backgroundwork

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.yedc.android.formmanagement.FormsDataService
import com.yedc.android.injection.config.ProjectDependencyModuleFactory
import com.yedc.android.notifications.Notifier

@RunWith(AndroidJUnit4::class)
class AutoUpdateTaskSpecTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formUpdateChecker = mock<FormsDataService>()

    @Before
    fun setup() {
        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesFormsUpdater(
                application: Application,
                notifier: Notifier,
                projectDependencyModuleFactory: ProjectDependencyModuleFactory
            ): FormsDataService {
                return formUpdateChecker
            }
        })
    }

    @Test
    fun `calls checkForUpdates with project from tag`() {
        val autoUpdateTaskSpec = AutoUpdateTaskSpec()
        val task = autoUpdateTaskSpec.getTask(context, mapOf(TaskData.DATA_PROJECT_ID to "projectId"), true)

        task.get()
        verify(formUpdateChecker).downloadUpdates("projectId")
    }

    @Test
    fun `maxRetries should not be limited`() {
        assertThat(AutoUpdateTaskSpec().maxRetries, `is`(nullValue()))
    }
}
