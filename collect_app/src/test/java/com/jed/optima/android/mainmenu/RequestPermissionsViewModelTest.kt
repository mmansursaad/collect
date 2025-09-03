package com.jed.optima.android.mainmenu

import android.Manifest.permission.POST_NOTIFICATIONS
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.jed.optima.permissions.PermissionsChecker
import com.jed.optima.settings.InMemSettingsProvider
import com.jed.optima.settings.keys.MetaKeys
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
class RequestPermissionsViewModelTest {

    private val permissionChecker = mock<PermissionsChecker> {
        on { isPermissionGranted(any()) } doReturn false
    }

    private val settingsProvider = InMemSettingsProvider()

    @Test
    fun shouldAskForPermissions_returnsTrue() {
        val viewModel = RequestPermissionsViewModel(settingsProvider, permissionChecker)
        assertThat(viewModel.shouldAskForPermissions(), equalTo(true))
    }

    @Test
    @Config(sdk = [32])
    fun shouldAskForPermissions_whenAPIDoesNotNeedNotificationPermissions_returnsFalse() {
        val viewModel = RequestPermissionsViewModel(settingsProvider, permissionChecker)
        assertThat(viewModel.shouldAskForPermissions(), equalTo(false))
    }

    @Test
    fun shouldAskForPermissions_whenPermissionsHaveAlreadyBeenGranted_returnsFalse() {
        whenever(permissionChecker.isPermissionGranted(POST_NOTIFICATIONS)).doReturn(true)

        val viewModel = RequestPermissionsViewModel(settingsProvider, permissionChecker)
        assertThat(viewModel.shouldAskForPermissions(), equalTo(false))
    }

    @Test
    fun shouldAskForPermissions_whenPermissionsHaveAlreadyBeenAskFor_returnsFalse() {
        settingsProvider.getMetaSettings().save(MetaKeys.PERMISSIONS_REQUESTED, true)

        val viewModel = RequestPermissionsViewModel(settingsProvider, permissionChecker)
        assertThat(viewModel.shouldAskForPermissions(), equalTo(false))
    }

    @Test
    fun permissionsRequested_marksPermissionsAsRequestedInSettings() {
        val viewModel = RequestPermissionsViewModel(settingsProvider, permissionChecker)
        viewModel.permissionsRequested()

        assertThat(
            settingsProvider.getMetaSettings().getBoolean(MetaKeys.PERMISSIONS_REQUESTED),
            equalTo(true)
        )
    }
}
