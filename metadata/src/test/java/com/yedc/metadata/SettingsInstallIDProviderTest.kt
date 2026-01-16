package com.yedc.metadata

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.startsWith
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import com.yedc.shared.settings.InMemSettings
import com.yedc.shared.settings.Settings

class SettingsInstallIDProviderTest {
    private val metaPreferences: Settings = InMemSettings()
    private val provider = SettingsInstallIDProvider(metaPreferences, "blah")

    @Test
    fun returnsSameValueEveryTime() {
        val firstValue = provider.installID
        val secondValue = provider.installID

        assertThat(firstValue, equalTo(secondValue))
    }

    @Test
    fun returnsValueWithPrefix() {
        assertThat(provider.installID, startsWith("yedc:"))
    }

    @Test
    fun returns24CharacterValue() {
        assertThat(provider.installID.length, equalTo(24))
    }

    @Test
    fun clearingSharedPreferences_resetsInstallID() {
        val firstValue = provider.installID
        metaPreferences.clear()
        val secondValue = provider.installID

        assertThat(secondValue, notNullValue())
        assertThat(firstValue, not(equalTo(secondValue)))
    }
}
