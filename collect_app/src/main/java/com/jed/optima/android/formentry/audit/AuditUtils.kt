package com.jed.optima.android.formentry.audit

import org.javarosa.form.api.FormEntryController
import com.jed.optima.android.javarosawrapper.FormController
import com.jed.optima.android.javarosawrapper.FormControllerExt.getQuestionPrompts
import com.jed.optima.android.javarosawrapper.RepeatsInFieldListException

object AuditUtils {
    @JvmStatic
    fun logCurrentScreen(
        formController: FormController,
        auditEventLogger: com.jed.optima.android.formentry.audit.AuditEventLogger,
        currentTime: Long
    ) {
        if (formController.getEvent() == FormEntryController.EVENT_QUESTION ||
            formController.getEvent() == FormEntryController.EVENT_GROUP ||
            formController.getEvent() == FormEntryController.EVENT_REPEAT
        ) {
            try {
                for (question in formController.getQuestionPrompts()) {
                    val answer =
                        if (question.answerValue != null) {
                            question.answerValue!!.displayText
                        } else {
                            null
                        }

                    auditEventLogger.logEvent(
                        com.jed.optima.android.formentry.audit.AuditEvent.AuditEventType.QUESTION,
                        question.index,
                        true,
                        answer,
                        currentTime,
                        null
                    )
                }
            } catch (e: RepeatsInFieldListException) {
                // ignore
            }
        }
    }
}
