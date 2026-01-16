package com.yedc.android.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.yedc.android.mainmenu.MainMenuActivity
import com.yedc.crashhandler.CrashHandler
import com.yedc.strings.localization.LocalizedActivity

class CrashHandlerActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashHandler = CrashHandler.getInstance(this)!!
        val crashView = crashHandler.getCrashView(this) {
            _root_ide_package_.com.yedc.android.activities.ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
        }

        if (crashView != null) {
            setContentView(crashView)
        } else {
            finish()
        }

        onBackPressedDispatcher.addCallback(
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    crashView?.dismiss()
                }
            }
        )
    }
}
