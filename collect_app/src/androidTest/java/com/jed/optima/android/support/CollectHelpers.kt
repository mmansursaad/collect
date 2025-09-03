package com.jed.optima.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.injection.config.DaggerAppDependencyComponent
import com.jed.optima.projects.Project

object CollectHelpers {
    fun overrideAppDependencyModule(appDependencyModule: com.jed.optima.android.injection.config.AppDependencyModule): com.jed.optima.android.injection.config.AppDependencyComponent {
        val application = ApplicationProvider.getApplicationContext<com.jed.optima.android.application.Collect>()
        val testComponent = DaggerAppDependencyComponent.builder()
            .application(application)
            .appDependencyModule(appDependencyModule)
            .build()
        application.component = testComponent
        return testComponent
    }

    fun simulateProcessRestart(appDependencyModule: com.jed.optima.android.injection.config.AppDependencyModule? = null) {
        ApplicationProvider.getApplicationContext<com.jed.optima.android.application.Collect>().getState().clear()

        val newComponent =
            overrideAppDependencyModule(appDependencyModule ?: com.jed.optima.android.injection.config.AppDependencyModule())

        // Reinitialize any application state with new deps/state
        newComponent.applicationInitializer().initialize()
    }

    @JvmStatic
    fun addDemoProject() {
        val component =
            DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
        component.projectsRepository().save(Project.DEMO_PROJECT)
        component.currentProjectProvider().setCurrentProject(Project.DEMO_PROJECT_ID)
    }
}
