package com.yedc.android.configure.qr

import android.app.Activity
import android.content.Intent
import android.widget.Toast
import com.yedc.analytics.Analytics.Companion.log
import com.yedc.android.analytics.AnalyticsEvents
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.projects.Project.Saved
import com.yedc.projects.ProjectConfigurationResult
import com.yedc.qrcode.zxing.QRCodeDecoder
import com.yedc.settings.ODKAppSettingsImporter
import java.io.FileNotFoundException
import java.io.InputStream

class QRCodeActivityResultDelegate(
    private val activity: Activity,
    private val settingsImporter: ODKAppSettingsImporter,
    private val qrCodeDecoder: QRCodeDecoder,
    private val project: Saved
) {
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == QRCodeMenuProvider.SELECT_PHOTO && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            if (imageUri != null) {
                val imageStream: InputStream? = try {
                    activity.contentResolver.openInputStream(imageUri)
                } catch (e: FileNotFoundException) {
                    // Not sure how this could happen? If you work it out: write a test!
                    return
                }
                try {
                    val response = qrCodeDecoder.decode(imageStream)

                    when (settingsImporter.fromJSON(response, project)) {
                        ProjectConfigurationResult.SUCCESS -> {
                            log(AnalyticsEvents.RECONFIGURE_PROJECT)
                            showToast(com.yedc.strings.R.string.successfully_imported_settings)
                            _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(
                                activity,
                                MainMenuActivity::class.java
                            )
                        }
                        ProjectConfigurationResult.INVALID_SETTINGS -> showToast(com.yedc.strings.R.string.invalid_qrcode)
                        ProjectConfigurationResult.GD_PROJECT -> showToast(com.yedc.strings.R.string.settings_with_gd_protocol)
                    }
                } catch (e: QRCodeDecoder.QRCodeInvalidException) {
                    showToast(com.yedc.strings.R.string.invalid_qrcode)
                } catch (e: QRCodeDecoder.QRCodeNotFoundException) {
                    showToast(com.yedc.strings.R.string.qr_code_not_found)
                }
            }
        }
    }

    private fun showToast(string: Int) {
        Toast.makeText(activity, activity.getString(string), Toast.LENGTH_LONG).show()
    }
}
