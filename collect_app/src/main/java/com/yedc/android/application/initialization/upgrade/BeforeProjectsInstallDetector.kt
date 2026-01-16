package com.yedc.android.application.initialization.upgrade

import android.content.Context
import androidx.preference.PreferenceManager
import com.yedc.upgrade.AppUpgrader
import com.yedc.upgrade.InstallDetector
import java.io.File

/**
 * Implementation of [InstallDetector] that looks for signs that a version of Collect
 * is installed from before Projects were introduced (< v2021.2). [AppUpgrader] was
 * introduced in that release as well so it and versions after it can use [AppUpgrader]'s
 * built in version tracking.
 */
class BeforeProjectsInstallDetector(private val context: Context) : InstallDetector {

    override fun installDetected(): Boolean {
        val legacyMetadataDir = File(context.getExternalFilesDir(null), "metadata")
        val hasLegacyMetadata = _root_ide_package_.com.yedc.android.utilities.FileUtils.listFiles(legacyMetadataDir).isNotEmpty()

        val hasLegacyGeneralPrefs =
            PreferenceManager.getDefaultSharedPreferences(context).all.isNotEmpty()
        val hasLegacyAdminPrefs =
            context.getSharedPreferences("admin_prefs", Context.MODE_PRIVATE).all.isNotEmpty()

        return hasLegacyMetadata || hasLegacyGeneralPrefs || hasLegacyAdminPrefs
    }
}
