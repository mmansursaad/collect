package com.yedc.metadata

import com.yedc.shared.settings.Settings

interface InstallIDProvider {
    val installID: String
}

class SettingsInstallIDProvider(
    private val metaPreferences: Settings,
    private val preferencesKey: String
) : InstallIDProvider {

    override val installID: String
        get() {
            return if (metaPreferences.contains(preferencesKey)) {
                metaPreferences.getString(preferencesKey) ?: generateAndStoreInstallID()
            } else {
                generateAndStoreInstallID()
            }
        }

    private fun generateAndStoreInstallID(): String {
        val installID = "yed:" + _root_ide_package_.com.yedc.shared.strings.RandomString.randomString(16)
        metaPreferences.save(preferencesKey, installID)
        return installID
    }
}
