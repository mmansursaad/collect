package com.yedc.projects.support

import android.app.Application
import com.yedc.projects.DaggerProjectsDependencyComponent
import com.yedc.projects.ProjectsDependencyComponent
import com.yedc.projects.ProjectsDependencyComponentProvider

class RobolectricApplication : Application(), ProjectsDependencyComponentProvider {

    override lateinit var projectsDependencyComponent: ProjectsDependencyComponent

    override fun onCreate() {
        super.onCreate()
        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder().build()
    }
}
