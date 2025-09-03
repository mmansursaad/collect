package com.jed.optima.android.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.jed.optima.android.mainmenu.MainMenuActivity
import com.jed.optima.crashhandler.CrashHandler
import com.jed.optima.strings.localization.LocalizedActivity

class CrashHandlerActivity : LocalizedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val crashHandler = CrashHandler.getInstance(this)!!
        val crashView = crashHandler.getCrashView(this) {
            com.jed.optima.android.activities.ActivityUtils.startActivityAndCloseAllOthers(this, MainMenuActivity::class.java)
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
