package com.yedc.android.injection.config

import com.yedc.async.Scheduler
import com.yedc.draw.DrawDependencyModule
import com.yedc.settings.SettingsProvider

class CollectDrawDependencyModule(
    private val applicationComponent: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent
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
