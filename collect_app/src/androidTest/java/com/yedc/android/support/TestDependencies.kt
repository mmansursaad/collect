package com.yedc.android.support

import android.app.Application
import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.work.WorkManager
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import com.yedc.android.storage.StoragePathProvider
import com.yedc.androidshared.system.BroadcastReceiverRegister
import com.yedc.async.Scheduler
import com.yedc.async.network.NetworkStateProvider
import com.yedc.qrcode.BarcodeScannerViewContainer
import com.yedc.settings.SettingsProvider
import com.yedc.testshared.FakeBarcodeScannerViewFactory
import com.yedc.testshared.FakeBroadcastReceiverRegister

open class TestDependencies @JvmOverloads constructor(
    private val useRealServer: Boolean = false
) : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
    @JvmField val server: StubOpenRosaServer = StubOpenRosaServer()

    @JvmField val storagePathProvider: StoragePathProvider = StoragePathProvider()

    val networkStateProvider: FakeNetworkStateProvider = FakeNetworkStateProvider()
    val scheduler: TestScheduler = TestScheduler(networkStateProvider)
    val fakeBarcodeScannerViewFactory = FakeBarcodeScannerViewFactory()
    val broadcastReceiverRegister: FakeBroadcastReceiverRegister = FakeBroadcastReceiverRegister()
    val restrictionsManager: RestrictionsManager = mock<RestrictionsManager>().apply {
        whenever(applicationRestrictions).thenReturn(Bundle())
    }

    override fun provideHttpInterface(
        mimeTypeMap: MimeTypeMap,
        userAgentProvider: _root_ide_package_.com.yedc.utilities.UserAgentProvider,
        application: Application,
        versionInformation: _root_ide_package_.com.yedc.android.version.VersionInformation
    ): com.yedc.openrosa.http.OpenRosaHttpInterface {
        return if (useRealServer) {
            super.provideHttpInterface(
                mimeTypeMap,
                userAgentProvider,
                application,
                versionInformation
            )
        } else {
            server
        }
    }

    override fun providesScheduler(workManager: WorkManager): Scheduler {
        return scheduler
    }

    override fun providesBarcodeScannerViewFactory(settingsProvider: SettingsProvider): BarcodeScannerViewContainer.Factory {
        return fakeBarcodeScannerViewFactory
    }

    override fun providesNetworkStateProvider(context: Context): NetworkStateProvider {
        return networkStateProvider
    }

    override fun providesBroadcastReceiverRegister(context: Context): BroadcastReceiverRegister {
        return broadcastReceiverRegister
    }

    override fun providesRestrictionsManager(context: Context): RestrictionsManager {
        return restrictionsManager
    }
}
