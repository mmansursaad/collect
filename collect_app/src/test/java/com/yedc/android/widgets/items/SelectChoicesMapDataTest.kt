package com.yedc.android.widgets.items

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.yedc.android.R
import com.yedc.android.widgets.support.FormElementFixtures.selectChoice
import com.yedc.android.widgets.support.FormElementFixtures.treeElement
import com.yedc.androidtest.getOrAwaitValue
import com.yedc.geo.selection.IconifiedText
import com.yedc.geo.selection.MappableSelectItem
import com.yedc.maps.MapPoint
import com.yedc.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class SelectChoicesMapDataTest {

    private val scheduler = FakeScheduler()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `choices with geo trace format geometry have multiple points`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 3 4; 12.1 -1.0 3 4"))
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getItemCount().getOrAwaitValue(), equalTo(1))

        val mappableItems = data.getMappableItems().getOrAwaitValue()!!
        assertThat(mappableItems.size, equalTo(1))

        val points = (mappableItems[0] as MappableSelectItem.MappableSelectLine).points
        assertThat(
            points,
            equalTo(listOf(MapPoint(12.0, -1.0, 3.0, 4.0), MapPoint(12.1, -1.0, 3.0, 4.0)))
        )
    }

    @Test
    fun `choices with geo shape format geometry have multiple points`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 3 4; 12.1 -1.0 3 4; 12.0 -1.0 3 4"))
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getItemCount().getOrAwaitValue(), equalTo(1))

        val mappableItems = data.getMappableItems().getOrAwaitValue()!!
        assertThat(mappableItems.size, equalTo(1))

        val points = (mappableItems[0] as MappableSelectItem.MappableSelectPolygon).points
        assertThat(
            points,
            equalTo(listOf(MapPoint(12.0, -1.0, 3.0, 4.0), MapPoint(12.1, -1.0, 3.0, 4.0), MapPoint(12.0, -1.0, 3.0, 4.0)))
        )
    }

    @Test
    fun `choices without geometry are not included in mappable items`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(children = listOf(treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 305 0")))
            ),
            selectChoice(
                value = "b",
                item = treeElement(children = emptyList())
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A", choices[1] to "B"))
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getItemCount().value, equalTo(2))
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("A"))
    }

    @Test
    fun `additional children are returned as properties`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 305 0"),
                        treeElement("property", "blah")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        val properties = data.getMappableItems().value!![0].properties
        assertThat(properties.size, equalTo(1))
        assertThat(properties[0], equalTo(IconifiedText(null, "property: blah")))
    }

    @Test
    fun `isLoading is true and items is null when items are being loaded from choices`() {
        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withSelectChoices(emptyList())
            .withSelectChoiceText(emptyMap())
            .build()

        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(resources, scheduler, prompt, null)
        assertThat(data.isLoading().value, equalTo(true))
        assertThat(data.getMappableItems().value, equalTo(null))

        scheduler.runBackground()
        assertThat(data.isLoading().value, equalTo(true))

        scheduler.runForeground()
        assertThat(data.isLoading().value, equalTo(false))
    }

    @Test
    fun `choices with incorrect geometry are ignored`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "0 170.00 0 0")
                    )
                )
            ),
            // Invalid
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "blah")
                    )
                )
            ),
            // Out of bounds
            selectChoice(
                value = "c",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "0 180.1 0 0")
                    )
                )
            ),
            // Second point out of bounds
            selectChoice(
                value = "c",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "0 180 0 0; 0 180.1 0 0")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)
        assertThat(data.getMappableItems().value!!.size, equalTo(1))
        assertThat(data.getMappableItems().value!![0].name, equalTo("a"))
    }

    /**
     * Attributes names come from properties defined at
     * https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0.
     */
    @Test
    fun `marker symbol and color are pulled from simple style attributes`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 305 0"),
                        treeElement(SelectChoicesMapData.MARKER_SYMBOL, "A"),
                        treeElement(SelectChoicesMapData.MARKER_COLOR, "#ffffff")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPoint
        assertThat(item.symbol, equalTo("A"))
        assertThat(item.color, equalTo("#ffffff"))
    }

    /**
     * Attributes names come from properties defined at
     * https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0.
     */
    @Test
    fun `line stroke color is pulled from simple style attributes`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 3 4; 12.1 -1.0 3 4"),
                        treeElement(SelectChoicesMapData.STROKE_WIDTH, "10"),
                        treeElement(SelectChoicesMapData.STROKE, "#ffffff")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectLine
        assertThat(item.strokeWidth, equalTo("10"))
        assertThat(item.strokeColor, equalTo("#ffffff"))
    }

    /**
     * Attributes names come from properties defined at
     * https://github.com/mapbox/simplestyle-spec/tree/master/1.1.0.
     */
    @Test
    fun `polygon stroke width, stroke color and fill color are pulled from simple style attributes`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 3 4; 12.1 -1.0 3 4; 12.0 -1.0 3 4"),
                        treeElement(SelectChoicesMapData.STROKE_WIDTH, "10"),
                        treeElement(SelectChoicesMapData.STROKE, "#000000"),
                        treeElement(SelectChoicesMapData.FILL, "#ffffff")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPolygon
        assertThat(item.strokeWidth, equalTo("10"))
        assertThat(item.strokeColor, equalTo("#000000"))
        assertThat(item.fillColor, equalTo("#ffffff"))
    }

    @Test
    fun `uses marker without cutout if marker-symbol is defined`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 305 0"),
                        treeElement(SelectChoicesMapData.MARKER_SYMBOL, "A")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .withSelectChoiceText(mapOf(choices[0] to "A"))
            .build()

        val data = loadDataForPrompt(prompt)
        val item = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPoint
        assertThat(item.smallIcon, equalTo(com.yedc.icons.R.drawable.ic_map_marker_small))
        assertThat(item.largeIcon, equalTo(com.yedc.icons.R.drawable.ic_map_marker_big))
    }

    @Test
    fun `uses marker with cutout if marker-symbol is not defined or blank`() {
        val choices = listOf(
            selectChoice(
                value = "a",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 305 0")
                    )
                )
            ),
            selectChoice(
                value = "b",
                item = treeElement(
                    children = listOf(
                        treeElement(SelectChoicesMapData.GEOMETRY, "0 170.00 0 0"),
                        treeElement(SelectChoicesMapData.MARKER_SYMBOL, " ")
                    )
                )
            )
        )

        val prompt = _root_ide_package_.com.yedc.android.support.MockFormEntryPromptBuilder()
            .withLongText("Which is your favourite place?")
            .withSelectChoices(choices)
            .build()

        val data = loadDataForPrompt(prompt)

        val firstItem = data.getMappableItems().getOrAwaitValue()!![0] as MappableSelectItem.MappableSelectPoint
        assertThat(firstItem.smallIcon, equalTo(com.yedc.icons.R.drawable.ic_map_marker_with_hole_small))
        assertThat(firstItem.largeIcon, equalTo(com.yedc.icons.R.drawable.ic_map_marker_with_hole_big))

        val secondItem = data.getMappableItems().getOrAwaitValue()!![1] as MappableSelectItem.MappableSelectPoint
        assertThat(secondItem.smallIcon, equalTo(com.yedc.icons.R.drawable.ic_map_marker_with_hole_small))
        assertThat(secondItem.largeIcon, equalTo(com.yedc.icons.R.drawable.ic_map_marker_with_hole_big))
    }

    private fun loadDataForPrompt(prompt: FormEntryPrompt): SelectChoicesMapData {
        val resources = ApplicationProvider.getApplicationContext<Application>().resources
        val data = SelectChoicesMapData(resources, scheduler, prompt, null)
        scheduler.flush()
        return data
    }
}
