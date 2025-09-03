package com.jed.optima.android.support.rules

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.junit.rules.ExternalResource
import com.jed.optima.android.formmanagement.FormFillingIntentFactory
import com.jed.optima.android.injection.DaggerUtils
import com.jed.optima.android.support.CollectHelpers
import com.jed.optima.android.support.StorageUtils
import com.jed.optima.android.support.pages.Page
import com.jed.optima.android.support.pages.SavepointRecoveryDialogPage
import timber.log.Timber
import java.io.IOException

open class FormEntryActivityTestRule :
    ExternalResource() {

    private lateinit var intent: Intent
    private lateinit var scenario: ActivityScenario<Activity>

    override fun after() {
        try {
            scenario.close()
        } catch (e: Throwable) {
            Timber.e(Error("Error closing ActivityScenario: $e"))
        }
    }

    @JvmOverloads
    fun setUpProjectAndCopyForm(
        formFilename: String,
        mediaFilePaths: List<String>? = null
    ): FormEntryActivityTestRule {
        try {
            // Set up demo project
            CollectHelpers.addDemoProject()
            StorageUtils.copyFormToDemoProject(formFilename, mediaFilePaths, true)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return this
    }

    fun <D : Page<D>> fillNewForm(formFilename: String, destination: D): D {
        intent = createNewFormIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return destination.assertOnPage()
    }

    fun fillNewForm(formFilename: String, formName: String): com.jed.optima.android.support.pages.FormEntryPage {
        return fillNewForm(formFilename,
            com.jed.optima.android.support.pages.FormEntryPage(formName)
        )
    }

    fun fillNewFormWithSavepoint(formFilename: String): SavepointRecoveryDialogPage {
        intent = createNewFormIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return SavepointRecoveryDialogPage().assertOnPage()
    }

    fun editForm(formFilename: String, instanceName: String): com.jed.optima.android.support.pages.FormHierarchyPage {
        intent = createEditFormIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return com.jed.optima.android.support.pages.FormHierarchyPage(instanceName).async().assertOnPage()
    }

    fun editFormWithSavepoint(formFilename: String): SavepointRecoveryDialogPage {
        intent = createEditFormIntent(formFilename)
        scenario = ActivityScenario.launch(intent)
        return SavepointRecoveryDialogPage().assertOnPage()
    }

    fun simulateProcessRestart(): FormEntryActivityTestRule {
        CollectHelpers.simulateProcessRestart()
        return this
    }

    private fun createNewFormIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.FORMS) + "/" + formFilename
        val form = DaggerUtils.getComponent(application).formsRepositoryProvider().create()
            .getOneByPath(formPath)
        val projectId = DaggerUtils.getComponent(application).currentProjectProvider()
            .requireCurrentProject().uuid

        return FormFillingIntentFactory.newFormIntent(
            application,
            com.jed.optima.android.external.FormsContract.getUri(projectId, form!!.dbId)
        )
    }

    private fun createEditFormIntent(formFilename: String): Intent {
        val application = ApplicationProvider.getApplicationContext<Application>()
        val formPath = DaggerUtils.getComponent(application).storagePathProvider()
            .getOdkDirPath(com.jed.optima.android.storage.StorageSubdirectory.FORMS) + "/" + formFilename
        val form = DaggerUtils.getComponent(application).formsRepositoryProvider().create()
            .getOneByPath(formPath)
        val instance = DaggerUtils.getComponent(application).instancesRepositoryProvider().create()
            .getAllByFormId(form!!.formId).first()
        val projectId = DaggerUtils.getComponent(application).currentProjectProvider()
            .requireCurrentProject().uuid

        return FormFillingIntentFactory.editDraftFormIntent(
            application,
            projectId,
            instance.dbId
        )
    }
}
