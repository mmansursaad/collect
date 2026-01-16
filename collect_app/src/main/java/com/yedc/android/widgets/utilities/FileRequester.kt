package com.yedc.android.widgets.utilities

import android.app.Activity
import org.javarosa.form.api.FormEntryPrompt
import com.yedc.android.javarosawrapper.FormController
import com.yedc.androidshared.system.IntentLauncher
import com.yedc.androidshared.ui.ToastUtils.showLongToast
import java.lang.Error
import java.lang.Exception

class FileRequesterImpl(
    val intentLauncher: IntentLauncher,
    val externalAppIntentProvider: _root_ide_package_.com.yedc.android.utilities.ExternalAppIntentProvider,
    private val formController: FormController
) : FileRequester {

    override fun launch(
        activity: Activity,
        requestCode: Int,
        formEntryPrompt: FormEntryPrompt
    ) {
        try {
            val intent = externalAppIntentProvider.getIntentToRunExternalApp(formController, formEntryPrompt)
            val intentWithoutDefaultCategory =
                externalAppIntentProvider.getIntentToRunExternalAppWithoutDefaultCategory(
                    formController,
                    formEntryPrompt,
                    activity.packageManager
                )

            intentLauncher.launchForResult(
                activity,
                intent,
                requestCode
            ) {
                intentLauncher.launchForResult(
                    activity,
                    intentWithoutDefaultCategory,
                    requestCode
                ) {
                    showLongToast(getErrorMessage(formEntryPrompt, activity))
                }
            }
        } catch (e: Exception) {
            showLongToast(e.message!!)
        } catch (e: Error) {
            showLongToast(e.message!!)
        }
    }

    private fun getErrorMessage(formEntryPrompt: FormEntryPrompt, activity: Activity): String {
        val customErrorMessage = formEntryPrompt.getSpecialFormQuestionText("noAppErrorString")
        return customErrorMessage ?: activity.getString(com.yedc.strings.R.string.no_app)
    }
}

interface FileRequester {
    fun launch(
        activity: Activity,
        requestCode: Int,
        formEntryPrompt: FormEntryPrompt
    )
}
