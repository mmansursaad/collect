package com.jed.optima.android.injection.config

import com.jed.optima.async.Scheduler
import com.jed.optima.draw.DrawDependencyModule
import com.jed.optima.settings.SettingsProvider

class CollectDrawDependencyModule(
    private val applicationComponent: com.jed.optima.android.injection.config.AppDependencyComponent
) : DrawDependencyModule() {
    override fun providesScheduler(): Scheduler {
        return applicationComponent.scheduler()
    }

    override fun providesSettingsProvider(): SettingsProvider {
        return applicationComponent.settingsProvider()
    }

    override fun providesImagePath(): String {
        return applicationComponent.storagePathProvider().getTmpImageFilePath()
    }
}
