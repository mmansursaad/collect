package com.yedc.android.support.rules

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.support.CollectHelpers
import com.yedc.android.views.DecoratedBarcodeView
import com.yedc.androidshared.ui.ToastUtils
import com.yedc.androidshared.ui.multiclicksafe.MultiClickGuard
import com.yedc.db.sqlite.DatabaseConnection
import com.yedc.material.BottomSheetBehavior
import java.io.IOException

private class ResetStateStatement(
    private val base: Statement,
    private val appDependencyModule: _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule? = null
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

    private fun clearPrefs(component: _root_ide_package_.com.yedc.android.injection.config.AppDependencyComponent) {
        val projectIds = component.projectsRepository().getAll().map { it.uuid }
        component.settingsProvider().clearAll(projectIds)
    }
}

class ResetStateRule @JvmOverloads constructor(
    private val appDependencyModule: _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule? = null
) : TestRule {

    override fun apply(base: Statement, description: Description): Statement =
        ResetStateStatement(base, appDependencyModule)
}
