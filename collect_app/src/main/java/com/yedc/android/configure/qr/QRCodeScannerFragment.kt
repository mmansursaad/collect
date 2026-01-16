package com.yedc.android.configure.qr

import android.content.Context
import com.yedc.analytics.Analytics
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.storage.StoragePathProvider
import com.yedc.androidshared.ui.ToastUtils.showLongToast
import com.yedc.androidshared.utils.CompressionUtils
import com.yedc.projects.ProjectConfigurationResult
import com.yedc.settings.ODKAppSettingsImporter
import java.io.File
import java.io.IOException
import java.util.zip.DataFormatException
import javax.inject.Inject

class QRCodeScannerFragment : _root_ide_package_.com.yedc.android.fragments.BarCodeScannerFragment() {

    @Inject
    lateinit var settingsImporter: ODKAppSettingsImporter

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var storagePathProvider: StoragePathProvider

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerUtils.getComponent(context).inject(this)
    }

    @Throws(IOException::class, DataFormatException::class)
    override fun handleScanningResult(result: String) {
        val oldProjectName = projectsDataService.requireCurrentProject().name

        val settingsImportingResult = settingsImporter.fromJSON(
            CompressionUtils.decompress(result),
            projectsDataService.requireCurrentProject()
        )

        when (settingsImportingResult) {
            ProjectConfigurationResult.SUCCESS -> {
                Analytics.log(AnalyticsEvents.RECONFIGURE_PROJECT)

                val newProjectName = projectsDataService.requireCurrentProject().name
                if (newProjectName != oldProjectName) {
                    File(storagePathProvider.getProjectRootDirPath() + File.separator + oldProjectName).delete()
                    File(storagePathProvider.getProjectRootDirPath() + File.separator + newProjectName).createNewFile()
                }

                showLongToast(
                    getString(com.yedc.strings.R.string.successfully_imported_settings)
                )
                _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    MainMenuActivity::class.java
                )
            }

            ProjectConfigurationResult.INVALID_SETTINGS -> showLongToast(
                getString(
                    com.yedc.strings.R.string.invalid_qrcode
                )
            )

            ProjectConfigurationResult.GD_PROJECT -> showLongToast(
                getString(com.yedc.strings.R.string.settings_with_gd_protocol)
            )
        }
    }

    override fun isQrOnly(): Boolean {
        return true
    }
}
