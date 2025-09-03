package com.jed.optima.android.configure.qr

import android.content.Context
import com.jed.optima.analytics.Analytics
import com.jed.optima.android.analytics.AnalyticsEvents
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.android.projects.ProjectsDataService
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.androidshared.ui.ToastUtils.showLongToast
import com.jed.optima.androidshared.utils.CompressionUtils
import com.jed.optima.projects.ProjectConfigurationResult
import com.jed.optima.settings.ODKAppSettingsImporter
import java.io.File
import java.io.IOException
import java.util.zip.DataFormatException
import javax.inject.Inject

class QRCodeScannerFragment : com.jed.optima.android.fragments.BarCodeScannerFragment() {

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
                    getString(com.jed.optima.strings.R.string.successfully_imported_settings)
                )
                com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                    requireActivity(),
                    MainMenuActivity::class.java
                )
            }

            ProjectConfigurationResult.INVALID_SETTINGS -> showLongToast(
                getString(
                    com.jed.optima.strings.R.string.invalid_qrcode
                )
            )

            ProjectConfigurationResult.GD_PROJECT -> showLongToast(
                getString(com.jed.optima.strings.R.string.settings_with_gd_protocol)
            )
        }
    }

    override fun isQrOnly(): Boolean {
        return true
    }
}
