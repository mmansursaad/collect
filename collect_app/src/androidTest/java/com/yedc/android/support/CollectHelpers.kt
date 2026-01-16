package com.yedc.android.support

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.injection.config.DaggerAppDependencyComponent
import com.yedc.projects.Project

object CollectHelpers {
    fun overrideAppDependencyModule(appDependencyModule: _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule): _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent {
        val application = ApplicationProvider.getApplicationContext<_root_ide_package_.com.yedc.android.application.Collect>()
        val testComponent = DaggerAppDependencyComponent.builder()
            .application(application)
            .appDependencyModule(appDependencyModule)
            .build()
        application.component = testComponent
        return testComponent
    }

    fun simulateProcessRestart(appDependencyModule: _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule? = null) {
        ApplicationProvider.getApplicationContext<_root_ide_package_.com.yedc.android.application.Collect>().getState().clear()

        val newComponent =
            overrideAppDependencyModule(appDependencyModule ?: _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule())

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
