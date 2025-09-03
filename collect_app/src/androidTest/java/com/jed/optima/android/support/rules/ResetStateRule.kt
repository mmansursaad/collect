package com.jed.optima.android.support.rules

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.support.CollectHelpers
import com.jed.optima.android.views.DecoratedBarcodeView
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.androidshared.ui.multiclicksafe.MultiClickGuard
import com.jed.optima.db.sqlite.DatabaseConnection
import com.jed.optima.material.BottomSheetBehavior
import java.io.IOException

private class ResetStateStatement(
    private val base: Statement,
    private val appDependencyModule: com.jed.optima.android.injection.config.AppDependencyModule? = null
) : Statement() {

    override fun evaluate() {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val oldComponent = DaggerUtils.getComponent(application)

        clearPrefs(oldComponent)
        clearDisk()
        setTestState()
        CollectHelpers.simulateProcessRestart(appDependencyModule)
        base.evaluate()
    }

    private fun setTestState() {
        MultiClickGuard.test = true
        DecoratedBarcodeView.test = true
        ToastUtils.recordToasts = true
        BottomSheetBehavior.DRAGGING_ENABLED = false
    }

    private fun clearDisk() {
        try {
            val internalFilesDir = ApplicationProvider.getApplicationContext<Application>().filesDir
            internalFilesDir.deleteRecursively()

            val externalFilesDir =
                ApplicationProvider.getApplicationContext<Application>().getExternalFilesDir(null)!!
            externalFilesDir.deleteRecursively()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        DatabaseConnection.closeAll()
    }

    private fun clearPrefs(component: com.jed.optima.android.injection.config.AppDependencyComponent) {
        val projectIds = component.projectsRepository().getAll().map { it.uuid }
        component.settingsProvider().clearAll(projectIds)
    }
}

class ResetStateRule @JvmOverloads constructor(
    private val appDependencyModule: com.jed.optima.android.injection.config.AppDependencyModule? = null
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        ResetStateStatement(base, appDependencyModule)
}
