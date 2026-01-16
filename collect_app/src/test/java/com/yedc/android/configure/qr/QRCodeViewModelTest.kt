package com.yedc.android.configure.qr

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.yedc.android.TestSettingsProvider.getProtectedSettings
import com.yedc.android.TestSettingsProvider.getUnprotectedSettings
import com.yedc.settings.keys.ProjectKeys
import com.yedc.settings.keys.ProtectedProjectKeys
import com.yedc.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class QRCodeViewModelTest {
    private val qrCodeGenerator = mock<QRCodeGenerator>()
    private val appConfigurationGenerator = mock<AppConfigurationGenerator>()
    private val fakeScheduler = FakeScheduler()
    private val generalSettings = getUnprotectedSettings()
    private val adminSettings = getProtectedSettings()

    @Test
    fun setIncludedKeys_generatesQRCodeWithKeys() {
        val viewModel = createViewModel()
        viewModel.setIncludedKeys(listOf("foo", "bar"))
        fakeScheduler.runBackground()

        verify(qrCodeGenerator).generateQRCode(listOf("foo", "bar"), appConfigurationGenerator)
    }

    @Test
    fun warning_whenNeitherServerOrAdminPasswordSet_isNull() {
        val viewModel = createViewModel()
        assertThat(viewModel.warning.value, nullValue())
    }

    @Test
    fun warning_whenServerAndAdminPasswordSet_isForBoth() {
        generalSettings.save(ProjectKeys.KEY_PASSWORD, "blah")
        adminSettings.save(ProtectedProjectKeys.KEY_ADMIN_PW, "blah")
        val viewModel = createViewModel()

        assertThat(viewModel.warning.value, equalTo(com.yedc.strings.R.string.qrcode_with_both_passwords))
    }

    private fun createViewModel(): QRCodeViewModel {
        val viewModel = QRCodeViewModel(
            qrCodeGenerator,
            appConfigurationGenerator,
            generalSettings,
            adminSettings,
            fakeScheduler
        )

        fakeScheduler.flush() // Run initial QR generation
        return viewModel
    }
}
