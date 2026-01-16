package com.yedc.android.widgets.range

import android.view.View.OnLongClickListener
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.RangeQuestion
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.yedc.android.widgets.support.QuestionWidgetHelpers.widgetDependencies
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class RangePickerDecimalWidgetTest {
    private val rangeQuestion: RangeQuestion = RangeQuestion().also {
        it.rangeStart = BigDecimal(1.5)
        it.rangeEnd = BigDecimal(5.5)
        it.rangeStep = BigDecimal(0.5)
    }

    @Test
    fun `answer returns null when prompt does not have answer`() {
        assertThat(
            createWidget(
                _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnlyAndQuestionDef(
                    rangeQuestion
                )
            ).answer,
            nullValue()
        )
    }

    @Test
    fun `answer returns answer when prompt has answer`() {
        assertThat(
            createWidget(
                _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                    rangeQuestion,
                    StringData("2.5")
                )
            ).answer!!.value,
            equalTo(2.5)
        )
    }

    @Test
    fun `clearAnswer clears widget answer`() {
        val widget = createWidget(
            _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(
                rangeQuestion,
                StringData("2.5")
            )
        )
        widget.clearAnswer()

        assertThat(widget.answer, Matchers.nullValue())
        assertThat(
            widget.binding.widgetAnswerText.text,
            equalTo(widget.context.getString(com.yedc.strings.R.string.no_value_selected))
        )
    }

    @Test
    fun `clearAnswer calls valueChangeListener`() {
        val widget = createWidget(_root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null))
        val valueChangedListener = _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener(widget)
        widget.clearAnswer()

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    @Test
    fun `setNumberPickerValue updates answer`() {
        val widget = createWidget(_root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null))
        widget.setNumberPickerValue(4)

        MatcherAssert.assertThat(widget.answer!!.displayText, Matchers.equalTo("3.5"))
    }

    @Test
    fun `clicking widget for long calls longClickListener`() {
        val listener = mock<OnLongClickListener>()
        val widget = createWidget(_root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null))

        widget.setOnLongClickListener(listener)
        widget.binding.widgetButton.performLongClick()
        widget.binding.widgetAnswerText.performLongClick()

        verify(listener).onLongClick(widget.binding.widgetButton)
        verify(listener).onLongClick(widget.binding.widgetAnswerText)
    }

    @Test
    fun setData_callsValueChangeListener() {
        val widget = createWidget(_root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithQuestionDefAndAnswer(rangeQuestion, null))
        val valueChangedListener = _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.mockValueChangedListener(widget)
        widget.setValueChangedListener(valueChangedListener)
        widget.setNumberPickerValue(3)

        verify(valueChangedListener).widgetValueChanged(widget)
    }

    private fun createWidget(prompt: FormEntryPrompt): _root_ide_package_.com.yedc.android.widgets.range.RangePickerDecimalWidget {
        return _root_ide_package_.com.yedc.android.widgets.range.RangePickerDecimalWidget(
            _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity(),
            _root_ide_package_.com.yedc.android.formentry.questions.QuestionDetails(prompt),
            widgetDependencies()
        )
    }
}
