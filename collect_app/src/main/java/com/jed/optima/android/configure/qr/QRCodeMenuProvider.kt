package com.jed.optima.android.configure.qr

import android.content.Intent
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.jed.optima.android.R
import com.jed.optima.androidshared.system.IntentLauncher
import com.jed.optima.androidshared.ui.ToastUtils.showShortToast
import com.jed.optima.androidshared.ui.enableIconsVisibility
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard
import com.jed.optima.async.Scheduler
import com.jed.optima.settings.SettingsProvider
import timber.log.Timber

class QRCodeMenuProvider internal constructor(
    private val activity: FragmentActivity,
    private val intentLauncher: IntentLauncher,
    qrCodeGenerator: QRCodeGenerator,
    appConfigurationGenerator: AppConfigurationGenerator,
    private val fileProvider: com.jed.optima.android.utilities.FileProvider,
    settingsProvider: SettingsProvider,
    scheduler: Scheduler
) : MenuProvider {
    private var qrFilePath: String? = null

    init {
        val qrCodeViewModel = ViewModelProvider(
            activity,
            QRCodeViewModel.Factory(
                qrCodeGenerator,
                appConfigurationGenerator,
                settingsProvider,
                scheduler
            )
        )[QRCodeViewModel::class.java]

        qrCodeViewModel.filePath.observe(activity) { filePath: String? ->
            if (filePath != null) {
                qrFilePath = filePath
            }
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.qr_code_scan_menu, menu)
        menu.enableIconsVisibility()
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean {
        if (!MultiClickGuard.allowClick(javaClass.name)) {
            return true
        }

        when (item.itemId) {
            R.id.menu_item_scan_sd_card -> {
                val photoPickerIntent = Intent(Intent.ACTION_GET_CONTENT)
                photoPickerIntent.type = "image/*"
                intentLauncher.launchForResult(activity, photoPickerIntent, SELECT_PHOTO) {
                    showShortToast(
                        activity.getString(
                            com.jed.optima.strings.R.string.activity_not_found,
                            activity.getString(com.jed.optima.strings.R.string.choose_image)
                        )
                    )
                    Timber.w(
                        activity.getString(
                            com.jed.optima.strings.R.string.activity_not_found,
                            activity.getString(com.jed.optima.strings.R.string.choose_image)
                        )
                    )
                }
                return true
            }
            R.id.menu_item_share -> {
                if (qrFilePath != null) {
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, fileProvider.getURIForFile(qrFilePath))
                    }
                    activity.startActivity(intent)
                }
                return true
            }
        }
        return false
    }

    companion object {
        const val SELECT_PHOTO = 111
    }
}
