package com.jed.optima.android.configure.qr

import android.content.Intent
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.jed.optima.android.R
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.androidshared.system.IntentLauncher
import com.jed.optima.androidshared.ui.ListFragmentStateAdapter
import com.jed.optima.androidshared.utils.AppBarUtils.setupAppBarLayout
import com.jed.optima.async.Scheduler
import com.jed.optima.permissions.PermissionListener
import com.jed.optima.permissions.PermissionsProvider
import com.jed.optima.qrcode.zxing.QRCodeDecoder
import com.jed.optima.settings.ODKAppSettingsImporter
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.strings.localization.LocalizedActivity
import javax.inject.Inject

class QRCodeTabsActivity : LocalizedActivity() {
    @Inject
    lateinit var qrCodeGenerator: QRCodeGenerator

    @Inject
    lateinit var intentLauncher: IntentLauncher

    @Inject
    lateinit var fileProvider: com.jed.optima.android.utilities.FileProvider

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var qrCodeDecoder: QRCodeDecoder

    @Inject
    lateinit var settingsImporter: ODKAppSettingsImporter

    @Inject
    lateinit var appConfigurationGenerator: AppConfigurationGenerator

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var settingsProvider: SettingsProvider

    private lateinit var activityResultDelegate: QRCodeActivityResultDelegate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.tabs_layout)
        setupAppBarLayout(this, getString(com.jed.optima.strings.R.string.reconfigure_with_qr_code_settings_title))

        activityResultDelegate = QRCodeActivityResultDelegate(
            this,
            settingsImporter,
            qrCodeDecoder,
            projectsDataService.requireCurrentProject()
        )

        val menuProvider = QRCodeMenuProvider(
            this,
            intentLauncher,
            qrCodeGenerator,
            appConfigurationGenerator,
            fileProvider,
            settingsProvider,
            scheduler
        )
        addMenuProvider(menuProvider, this)

        permissionsProvider.requestCameraPermission(
            this,
            object : PermissionListener {
                override fun granted() {
                    setupViewPager()
                }

                override fun additionalExplanationClosed() {
                    finish()
                }
            }
        )
    }

    private fun setupViewPager() {
        val fragmentTitleList = arrayOf(
            getString(com.jed.optima.strings.R.string.scan_qr_code_fragment_title),
            getString(com.jed.optima.strings.R.string.view_qr_code_fragment_title)
        )

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager.adapter = ListFragmentStateAdapter(
            this,
            listOf(QRCodeScannerFragment::class.java, ShowQRCodeFragment::class.java)
        )

        TabLayoutMediator(tabLayout, viewPager) { tab: TabLayout.Tab, position: Int ->
            tab.text = fragmentTitleList[position]
        }.attach()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        activityResultDelegate.onActivityResult(requestCode, resultCode, data)
    }
}
