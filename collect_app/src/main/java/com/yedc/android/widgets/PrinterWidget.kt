package com.yedc.android.widgets

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import com.yedc.android.R
import com.yedc.android.widgets.interfaces.Printer
import com.yedc.androidshared.ui.multiclicksafe.MultiClickSafeMaterialButton

class PrinterWidget(
    context: Context,
    questionDetails: _root_ide_package_.com.yedc.android.formentry.questions.QuestionDetails,
    private val printer: Printer,
    private val questionMediaManager: _root_ide_package_.com.yedc.android.utilities.QuestionMediaManager,
    dependencies: Dependencies
) : _root_ide_package_.com.yedc.android.widgets.QuestionWidget(context, dependencies, questionDetails) {

    init {
        render()
    }

    override fun onCreateAnswerView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int): View {
        val answerView = LayoutInflater.from(context).inflate(R.layout.printer_widget, null)
        answerView
            .findViewById<MultiClickSafeMaterialButton>(R.id.printer_button)
            .setOnClickListener {
                print()
            }
        return answerView
    }

    override fun setOnLongClickListener(listener: OnLongClickListener?) = Unit

    override fun getAnswer(): IAnswerData? = formEntryPrompt.answerValue

    override fun clearAnswer() = Unit

    override fun registerToClearAnswerOnLongPress(activity: Activity?, viewGroup: ViewGroup?) = Unit

    private fun print() {
        formEntryPrompt.answerText?.let {
            printer.parseAndPrint(it, questionMediaManager, context)
        }
    }
}
