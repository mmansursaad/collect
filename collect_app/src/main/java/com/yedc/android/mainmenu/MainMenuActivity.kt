package com.yedc.android.mainmenu

import android.os.Build
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import com.yedc.android.R
import com.yedc.android.activities.CrashHandlerActivity
import com.yedc.android.activities.FirstLaunchActivity
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.projects.ProjectSettingsDialog
import com.yedc.androidshared.ui.FragmentFactoryBuilder
import com.yedc.crashhandler.CrashHandler
import com.yedc.mobiledevicemanagement.MDMConfigObserver
import com.yedc.permissions.PermissionsProvider
import com.yedc.settings.SettingsProvider
import com.yedc.strings.localization.LocalizedActivity
import javax.inject.Inject

class MainMenuActivity : LocalizedActivity() {

    @Inject
    lateinit var viewModelFactory: MainMenuViewModelFactory

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var mdmConfigObserver: MDMConfigObserver

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        initSplashScreen()

        /*
        Don't reopen if the app is already open - allows entry points like notifications to use
        this Activity as a target to reopen the app without interrupting an ongoing session
         */
        if (!isTaskRoot) {
            super.onCreate(null)
            finish()
            return
        }

        CrashHandler.getInstance(this)?.also {
            if (it.hasCrashed(this)) {
                super.onCreate(null)
                _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(this, CrashHandlerActivity::class.java)
                return
            }
        }

        DaggerUtils.getComponent(this).inject(this)

        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        currentProjectViewModel = viewModelProvider[CurrentProjectViewModel::class.java]

        _root_ide_package_.com.yedc.android.utilities.ThemeUtils(this).setDarkModeForCurrentProject()

        if (!currentProjectViewModel.hasCurrentProject()) {
            super.onCreate(null)
            _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(this, FirstLaunchActivity::class.java)
            return
        } else {
            this.supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
                .forClass(PermissionsDialogFragment::class) {
                    PermissionsDialogFragment(
                        permissionsProvider,
                        viewModelProvider[RequestPermissionsViewModel::class.java]
                    )
                }
                .forClass(ProjectSettingsDialog::class) {
                    ProjectSettingsDialog(viewModelFactory)
                }
                .forClass(MainMenuFragment::class) {
                    MainMenuFragment(viewModelFactory, settingsProvider)
                }
                .build()

            super.onCreate(savedInstanceState)
            setContentView(R.layout.main_menu_activity)
            lifecycle.addObserver(mdmConfigObserver)
        }
    }

    private fun initSplashScreen() {
        /*
        We don't need the `installSplashScreen` call on Android 12+ (the system handles the
        splash screen for us) and it causes problems if we later switch between dark/light themes
        with the ThemeUtils#setDarkModeForCurrentProject call.
         */
        if (Build.VERSION.SDK_INT < 31) {
            installSplashScreen()
        } else {
            setTheme(R.style.Theme_Collect)
        }
    }
}
