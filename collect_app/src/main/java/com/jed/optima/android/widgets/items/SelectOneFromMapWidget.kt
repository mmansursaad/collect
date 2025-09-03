package com.jed.optima.android.widgets.items

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.FragmentActivity
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.SelectOneData
import org.javarosa.core.model.data.helper.Selection
import org.javarosa.form.api.FormEntryPrompt
import com.jed.optima.android.databinding.SelectOneFromMapWidgetAnswerBinding
import com.jed.optima.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_FORM_INDEX
import com.jed.optima.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_SELECTED_INDEX
import com.jed.optima.androidshared.ui.DialogFragmentUtils
import com.jed.optima.permissions.PermissionListener

@SuppressLint("ViewConstructor")
class SelectOneFromMapWidget(
    context: Context,
    questionDetails: com.jed.optima.android.formentry.questions.QuestionDetails,
    private val autoAdvance: Boolean,
    private val autoAdvanceListener: com.jed.optima.android.listeners.AdvanceToNextListener,
    dependencies: Dependencies
) : com.jed.optima.android.widgets.QuestionWidget(context, dependencies, questionDetails),
    com.jed.optima.android.widgets.interfaces.WidgetDataReceiver {

    init {
        render()
    }

    lateinit var binding: SelectOneFromMapWidgetAnswerBinding
    private var answer: SelectOneData? = null

    override fun onCreateAnswerView(
        context: Context,
        prompt: FormEntryPrompt,
        answerFontSize: Int
    ): View {
        binding = SelectOneFromMapWidgetAnswerBinding.inflate(LayoutInflater.from(context))

        binding.button.setOnClickListener {
            permissionsProvider.requestEnabledLocationPermissions(
                context as Activity,
                object : PermissionListener {
                    override fun granted() {
                        DialogFragmentUtils.showIfNotShowing(
                            SelectOneFromMapDialogFragment::class.java,
                            Bundle().also {
                                it.putSerializable(ARG_FORM_INDEX, prompt.index)
                                (answer?.value as? Selection)?.index?.let { index ->
                                    it.putInt(ARG_SELECTED_INDEX, index)
                                }
                            },
                            (context as FragmentActivity).supportFragmentManager
                        )
                    }
                }
            )
        }

        binding.answer.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize.toFloat())
        updateAnswer(questionDetails.prompt.answerValue as? SelectOneData)

        return binding.root
    }

    override fun getAnswer(): IAnswerData? {
        return answer
    }

    override fun clearAnswer() {
        updateAnswer(null)
        widgetValueChanged()
    }

    override fun setOnLongClickListener(l: OnLongClickListener?) {}

    override fun setData(answer: Any?) {
        updateAnswer(answer as SelectOneData)
        widgetValueChanged()
        if (autoAdvance) {
            autoAdvanceListener.advance()
        }
    }

    private fun updateAnswer(answer: SelectOneData?) {
        this.answer = answer

        binding.answer.text = if (answer != null) {
            val choice = (answer.value as Selection).choice
            formEntryPrompt.getSelectChoiceText(choice)
        } else {
            ""
        }
        if (binding.answer.text.isBlank()) {
            binding.answer.visibility = GONE
        } else {
            binding.answer.visibility = VISIBLE
        }
    }
}
