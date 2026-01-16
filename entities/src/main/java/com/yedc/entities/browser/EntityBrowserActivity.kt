package com.yedc.entities.browser

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.yedc.androidshared.ui.FragmentFactoryBuilder
import com.yedc.async.Scheduler
import com.yedc.entities.EntitiesDependencyComponentProvider
import com.yedc.entities.R
import com.yedc.entities.storage.EntitiesRepository
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject

class EntityBrowserActivity : LocalizedActivity() {

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var entitiesRepository: EntitiesRepository

    val viewModelFactory = viewModelFactory {
        addInitializer(EntitiesViewModel::class) {
            EntitiesViewModel(scheduler, entitiesRepository)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(EntityListsFragment::class) { EntityListsFragment(viewModelFactory, ::getToolbar) }
            .forClass(EntitiesFragment::class) { EntitiesFragment(viewModelFactory) }
            .build()

        super.onCreate(savedInstanceState)
        (applicationContext as EntitiesDependencyComponentProvider)
            .entitiesDependencyComponent.inject(this)

        setContentView(R.layout.entities_layout)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        val appBarConfiguration = AppBarConfiguration(navController.graph)
        getToolbar().setupWithNavController(navController, appBarConfiguration)
    }

    private fun getToolbar() = findViewById<Toolbar>(com.yedc.androidshared.R.id.toolbar)
}
