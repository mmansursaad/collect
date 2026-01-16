package com.yedc.android.widgets

import android.app.Activity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.Constants
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import com.yedc.android.support.WidgetTestActivity
import com.yedc.android.widgets.items.SelectOneFromMapWidget

@RunWith(AndroidJUnit4::class)
class WidgetFactoryTest {
    private val activity: Activity = _root_ide_package_.com.yedc.android.support.CollectHelpers.buildThemedActivity(WidgetTestActivity::class.java).get()

    private var widgetFactory = _root_ide_package_.com.yedc.android.widgets.WidgetFactory(
        activity,
        false,
        null,
        null,
        null,
        null,
        mock<_root_ide_package_.com.yedc.android.formentry.FormEntryViewModel>(),
        null,
        null,
        null,
        null,
        null,
        null,
        mock()
    )

    @Test
    fun testCreatingSelectOneMinimalWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something miNimal something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectOneMinimalWidget::class.java))
    }

    @Test
    fun testCreatingLikertWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something lIkErt something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.LikertWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneListNoLabelWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something LisT-nOLabeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.ListWidget::class.java))
        assertThat((widget as _root_ide_package_.com.yedc.android.widgets.items.ListWidget).shouldDisplayLabel(), equalTo(false))
    }

    @Test
    fun testCreatingSelectOneListWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something LisT something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.ListWidget::class.java))
        assertThat((widget as _root_ide_package_.com.yedc.android.widgets.items.ListWidget).shouldDisplayLabel(), equalTo(true))
    }

    @Test
    fun testCreatingSelectOneLabelWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something lAbeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.LabelWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneImageMapWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("something imaGe-Map something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectOneImageMapWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneWidget() {
        var prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("")
            .build()

        var widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectOneWidget::class.java))

        prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("lorem ipsum")
            .build()

        widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectOneWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleMinimalWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something miNimal something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectMultiMinimalWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleListNoLabelWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something LisT-nOLabeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.ListMultiWidget::class.java))
        assertThat((widget as _root_ide_package_.com.yedc.android.widgets.items.ListMultiWidget).shouldDisplayLabel(), equalTo(false))
    }

    @Test
    fun testCreatingSelectMultipleListWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something LisT something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.ListMultiWidget::class.java))
        assertThat((widget as _root_ide_package_.com.yedc.android.widgets.items.ListMultiWidget).shouldDisplayLabel(), equalTo(true))
    }

    @Test
    fun testCreatingSelectMultipleOneLabelWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something lAbeL something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.LabelWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleImageMapWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("something imaGe-Map something")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectMultiImageMapWidget::class.java))
    }

    @Test
    fun testCreatingSelectMultipleWidget() {
        var prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("")
            .build()

        var widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectMultiWidget::class.java))

        prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_MULTI)
            .withAppearance("lorem ipsum")
            .build()

        widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.items.SelectMultiWidget::class.java))
    }

    @Test
    fun testCreatingSelectOneFromMapWidget() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_SELECT_ONE)
            .withAppearance("map")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(SelectOneFromMapWidget::class.java))
    }

    @Test
    fun exStringWidgetShouldBeCreatedIfThePackageNameIsMixedWithOtherAppearances() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_TEXT)
            .withAppearance("masked ex:change.uw.android.BREATHCOUNT thousands-sep")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.ExStringWidget::class.java))
    }

    @Test
    fun exIntegerWidgetShouldBeCreatedIfThePackageNameIsMixedWithOtherAppearances() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_INTEGER)
            .withAppearance("masked ex:change.uw.android.BREATHCOUNT thousands-sep")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.ExIntegerWidget::class.java))
    }

    @Test
    fun exDecimalWidgetShouldBeCreatedIfThePackageNameIsMixedWithOtherAppearances() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withControlType(Constants.CONTROL_INPUT)
            .withDataType(Constants.DATATYPE_DECIMAL)
            .withAppearance("masked ex:change.uw.android.BREATHCOUNT thousands-sep")
            .build()

        val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
        assertThat(widget, instanceOf(_root_ide_package_.com.yedc.android.widgets.ExDecimalWidget::class.java))
    }

    @Test
    fun testCreatingCounterWidget() {
        listOf("counter", "CouNTer").forEach { appearance ->
            val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
                .withControlType(Constants.CONTROL_INPUT)
                .withDataType(Constants.DATATYPE_INTEGER)
                .withAppearance(appearance)
                .build()

            val widget = widgetFactory.createWidgetFromPrompt(prompt, null)
            assertThat(widget, instanceOf(CounterWidget::class.java))
        }
    }
}
