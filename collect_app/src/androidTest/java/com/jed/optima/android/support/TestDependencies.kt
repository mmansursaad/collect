package com.jed.optima.android.support

import android.app.Application
import android.content.Context
import android.content.RestrictionsManager
import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.work.WorkManager
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.androidshared.system.BroadcastReceiverRegister
import com.jed.optima.async.Scheduler
import com.jed.optima.async.network.NetworkStateProvider
import com.jed.optima.qrcode.BarcodeScannerViewContainer
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.testshared.FakeBarcodeScannerViewFactory
import com.jed.optima.testshared.FakeBroadcastReceiverRegister

open class TestDependencies @JvmOverloads constructor(
    private val useRealServer: Boolean = false
) : com.jed.optima.android.injection.config.AppDependencyModule() {
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
        userAgentProvider: com.jed.optima.utilities.UserAgentProvider,
        application: Application,
        versionInformation: com.jed.optima.android.version.VersionInformation
    ): com.jed.optima.openrosa.http.OpenRosaHttpInterface {
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
