package com.jed.optima.mobiledevicemanagement

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionsManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.jed.optima.androidshared.system.BroadcastReceiverRegister
import com.jed.optima.async.Scheduler

/**
 * Manages configuration changes from a mobile device management system.
 *
 * See android.content.APP_RESTRICTIONS in AndroidManifest for supported configuration keys.
 */
class MDMConfigObserver(
    private val scheduler: Scheduler,
    private val mdmConfigHandler: MDMConfigHandler,
    private val broadcastReceiverRegister: BroadcastReceiverRegister,
    private val restrictionsManager: RestrictionsManager
) : DefaultLifecycleObserver {

    private val restrictionsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            scheduler.immediate {
                mdmConfigHandler.applyConfig(restrictionsManager.applicationRestrictions)
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        scheduler.immediate {
            mdmConfigHandler.applyConfig(restrictionsManager.applicationRestrictions)
        }

        val restrictionsFilter = IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED)
        broadcastReceiverRegister.registerReceiver(restrictionsReceiver, restrictionsFilter)
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        broadcastReceiverRegister.unregisterReceiver(restrictionsReceiver)
    }
}
