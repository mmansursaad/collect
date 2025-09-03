package com.jed.optima.android.widgets.items

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkManager
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.javarosa.core.model.data.SelectOneData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.jed.optima.android.databinding.SelectOneFromMapDialogLayoutBinding
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.android.utilities.Appearances
import com.jed.optima.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_FORM_INDEX
import com.jed.optima.android.widgets.items.SelectOneFromMapDialogFragment.Companion.ARG_SELECTED_INDEX
import com.jed.optima.android.widgets.support.FormElementFixtures.selectChoice
import com.jed.optima.android.widgets.support.FormElementFixtures.treeElement
import com.jed.optima.android.widgets.support.NoOpMapFragment
import com.jed.optima.androidshared.ui.FragmentFactoryBuilder
import com.jed.optima.async.Scheduler
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule
import com.jed.optima.geo.selection.IconifiedText
import com.jed.optima.geo.selection.MappableSelectItem
import com.jed.optima.geo.selection.SelectionMapFragment
import com.jed.optima.geo.selection.SelectionMapFragment.Companion.REQUEST_SELECT_ITEM
import com.jed.optima.maps.MapFragment
import com.jed.optima.maps.MapFragmentFactory
import com.jed.optima.maps.MapPoint
import com.jed.optima.maps.layers.ReferenceLayerRepository
import com.jed.optima.settings.SettingsProvider
import com.jed.optima.testshared.FakeScheduler

@RunWith(AndroidJUnit4::class)
class SelectOneFromMapDialogFragmentTest {

    private val selectChoices = listOf(
        selectChoice(
            value = "a",
            item = treeElement(children = listOf(treeElement(SelectChoicesMapData.GEOMETRY, "12.0 -1.0 305 0")))
        ),
        selectChoice(
            value = "b",
            item = treeElement(children = listOf(treeElement(SelectChoicesMapData.GEOMETRY, "13.0 -1.0 305 0")))
        )
    )

    private val prompt = _root_ide_package_.com.jed.optima.android.support.MockFormEntryPromptBuilder()
        .withLongText("Which is your favourite place?")
        .withSelectChoices(
            selectChoices
        )
        .withSelectChoiceText(
            mapOf(
                selectChoices[0] to "A",
                selectChoices[1] to "B"
            )
        )
        .build()

    private val formEntryViewModel = mock<_root_ide_package_.com.jed.optima.android.formentry.FormEntryViewModel> {
        on { getQuestionPrompt(prompt.index) } doReturn prompt
    }

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val scheduler = FakeScheduler()

    private val viewModelFactory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            return formEntryViewModel as T
        }
    }

    @get:Rule
    val launcherRule =
        FragmentScenarioLauncherRule(
            FragmentFactoryBuilder()
                .forClass(SelectOneFromMapDialogFragment::class.java) {
                    SelectOneFromMapDialogFragment(viewModelFactory)
                }.build()
        )

    @Before
    fun setup() {
        _root_ide_package_.com.jed.optima.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.jed.optima.android.injection.config.AppDependencyModule() {
            override fun providesMapFragmentFactory(settingsProvider: SettingsProvider): MapFragmentFactory {
                return object : MapFragmentFactory {
                    override fun createMapFragment(): MapFragment {
                        return NoOpMapFragment()
                    }
                }
            }

            override fun providesScheduler(workManager: WorkManager?): Scheduler {
                return scheduler
            }

            override fun providesReferenceLayerRepository(
                storagePathProvider: StoragePathProvider,
                settingsProvider: SettingsProvider
            ): ReferenceLayerRepository {
                return mock()
            }
        })
    }

    @Test
    fun `pressing back dismisses dialog`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scheduler.flush()

        scenario.onFragment {
            Espresso.pressBack()
            assertThat(it.isVisible, equalTo(false))
        }
    }

    @Test
    fun `sets up SelectionMapFragment`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment, notNullValue())
            assertThat(fragment.skipSummary, equalTo(false))
            assertThat(fragment.showNewItemButton, equalTo(false))

            val dialogBackPressedDispatcher =
                (it.requireDialog() as ComponentDialog).onBackPressedDispatcher
            assertThat(fragment.onBackPressedDispatcher?.invoke(), equalTo(dialogBackPressedDispatcher))
        }
    }

    @Test
    fun `gives SelectionMapFragment correct data`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()

            val data = fragment.selectionMapData
            scheduler.flush()

            assertThat(data.getMapTitle().value, equalTo(prompt.longText))
            assertThat(data.getItemCount().value, equalTo(prompt.selectChoices.size))
            val firstFeatureGeometry = selectChoices[0].getChild(SelectChoicesMapData.GEOMETRY)!!.split(" ")
            val secondFeatureGeometry = selectChoices[1].getChild(SelectChoicesMapData.GEOMETRY)!!.split(" ")
            assertThat(
                data.getMappableItems().value,
                equalTo(
                    listOf(
                        MappableSelectItem.MappableSelectPoint(
                            0,
                            "A",
                            point = MapPoint(
                                firstFeatureGeometry[0].toDouble(),
                                firstFeatureGeometry[1].toDouble(),
                                firstFeatureGeometry[2].toDouble(),
                                firstFeatureGeometry[3].toDouble()
                            ),
                            smallIcon = com.jed.optima.icons.R.drawable.ic_map_marker_with_hole_small,
                            largeIcon = com.jed.optima.icons.R.drawable.ic_map_marker_with_hole_big,
                            action = IconifiedText(
                                com.jed.optima.icons.R.drawable.ic_save,
                                application.getString(com.jed.optima.strings.R.string.select_item)
                            )
                        ),
                        MappableSelectItem.MappableSelectPoint(
                            1,
                            "B",
                            point = MapPoint(
                                secondFeatureGeometry[0].toDouble(),
                                secondFeatureGeometry[1].toDouble(),
                                secondFeatureGeometry[2].toDouble(),
                                secondFeatureGeometry[3].toDouble()
                            ),
                            smallIcon = com.jed.optima.icons.R.drawable.ic_map_marker_with_hole_small,
                            largeIcon = com.jed.optima.icons.R.drawable.ic_map_marker_with_hole_big,
                            action = IconifiedText(
                                com.jed.optima.icons.R.drawable.ic_save,
                                application.getString(com.jed.optima.strings.R.string.select_item)
                            )
                        )
                    )
                )
            )
        }
    }

    @Test
    fun `contains SelectionMapFragment with correct data with selected index`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
                it.putSerializable(ARG_SELECTED_INDEX, selectChoices[1].index)
            }
        )

        scheduler.flush()

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment, notNullValue())
            assertThat(fragment.skipSummary, equalTo(false))
            assertThat(fragment.showNewItemButton, equalTo(false))

            val data = fragment.selectionMapData
            assertThat(data.getMappableItems().value!![1].selected, equalTo(true))
        }
    }

    @Test
    fun `contains SelectionMapFragment with correct data for quick appearance`() {
        val prompt = _root_ide_package_.com.jed.optima.android.support.MockFormEntryPromptBuilder()
            .withAppearance("${Appearances.MAP} ${Appearances.QUICK}")
            .build()
        whenever(formEntryViewModel.getQuestionPrompt(prompt.index)).thenReturn(prompt)

        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scheduler.runBackground()

        scenario.onFragment {
            val binding = SelectOneFromMapDialogLayoutBinding.bind(it.view!!)
            val fragment = binding.selectionMap.getFragment<SelectionMapFragment>()
            assertThat(fragment.skipSummary, equalTo(true))
        }
    }

    @Test
    fun `selecting a choice on the map answers question and dismisses`() {
        val scenario = launcherRule.launch(
            SelectOneFromMapDialogFragment::class.java,
            Bundle().also {
                it.putSerializable(ARG_FORM_INDEX, prompt.index)
            }
        )

        scheduler.runBackground()

        scenario.onFragment {
            val result = bundleOf(SelectionMapFragment.RESULT_SELECTED_ITEM to 1L)
            it.childFragmentManager.setFragmentResult(REQUEST_SELECT_ITEM, result)
            assertThat(it.isVisible, equalTo(false))
        }

        verify(formEntryViewModel).answerQuestion(
            prompt.index,
            SelectOneData(selectChoices[1].selection())
        )
    }
}
