package com.jed.optima.draw

import dagger.Component
import dagger.Module
import dagger.Provides
import com.jed.optima.async.Scheduler
import com.jed.optima.settings.SettingsProvider
import javax.inject.Singleton

interface DrawDependencyComponentProvider {
    val drawDependencyComponent: DrawDependencyComponent
}

@Component(modules = [DrawDependencyModule::class])
@Singleton
interface DrawDependencyComponent {
    fun inject(drawActivity: _root_ide_package_.com.jed.optima.draw.DrawActivity)
    fun inject(drawActivity: DrawView)
}

@Module
open class DrawDependencyModule {

    @Provides
    open fun providesScheduler(): Scheduler {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesSettingsProvider(): SettingsProvider {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    open fun providesImagePath(): String {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }
}
