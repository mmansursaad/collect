package com.yedc.metadata

import com.yedc.shared.settings.Settings
import com.yedc.shared.strings.RandomString
import com.yedc.shared.FlavorRegistry

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

        val idPrefix = FlavorRegistry.idPrefix
        val randomSuffix = RandomString.randomString(16)

        val installID = "$idPrefix:$randomSuffix"

        metaPreferences.save(preferencesKey, installID)
        return installID
    }
}
