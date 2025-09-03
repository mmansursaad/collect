package com.jed.optima.projects.support

import android.app.Application
import com.jed.optima.projects.DaggerProjectsDependencyComponent
import com.jed.optima.projects.ProjectsDependencyComponent
import com.jed.optima.projects.ProjectsDependencyComponentProvider

class RobolectricApplication : Application(), ProjectsDependencyComponentProvider {

    override lateinit var projectsDependencyComponent: ProjectsDependencyComponent

    override fun onCreate() {
        super.onCreate()
        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder().build()
    }
}
