package com.yedc.androidshared.system

import android.content.Context
import android.os.Bundle
import com.yedc.androidshared.data.getState
import com.yedc.shared.strings.UUIDGenerator

object ProcessRestoreDetector {

    @JvmStatic
    fun registerOnSaveInstanceState(context: Context, outState: Bundle) {
        val uuid = UUIDGenerator().generateUUID()
        context.getState().set("${getKey()}:$uuid", Any())
        outState.putString(getKey(), uuid)
    }

    @JvmStatic
    fun isProcessRestoring(context: Context, savedInstanceState: Bundle?): Boolean {
        return if (savedInstanceState != null) {
            val bundleUuid = savedInstanceState.getString(getKey())
            context.getState().get<Any>("${getKey()}:$bundleUuid") == null
        } else {
            false
        }
    }

    private fun getKey() = this::class.qualifiedName
}
