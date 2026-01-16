package com.yedc.android.feature.maps

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.location.Location
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import com.yedc.android.R
import com.yedc.android.support.FakeClickableMapFragment
import com.yedc.android.support.TestDependencies
import com.yedc.android.support.rules.CollectTestRule
import com.yedc.android.support.rules.TestRuleChain
import com.yedc.androidtest.RecordedIntentsRule
import com.yedc.externalapp.ExternalAppUtils.getReturnIntent
import com.yedc.maps.MapFragment
import com.yedc.maps.MapFragmentFactory
import com.yedc.settings.SettingsProvider

@RunWith(AndroidJUnit4::class)
class FormMapTest {

    private val mapFragment = FakeClickableMapFragment()
    private val testDependencies = object : TestDependencies() {
        override fun providesMapFragmentFactory(settingsProvider: SettingsProvider): MapFragmentFactory {
            return object : MapFragmentFactory {
                override fun createMapFragment(): MapFragment {
                    return mapFragment
                }
            }
        }
    }
    private val rule = CollectTestRule()

    @get:Rule
    var copyFormChain: RuleChain = TestRuleChain.chain(testDependencies)
        .around(RecordedIntentsRule())
        .around(rule)

    @Test
    fun gettingBlankFormList_showsMapIcon_onlyForFormsWithGeometry() {
        rule.startAtMainMenu()
            .copyForm(SINGLE_GEOPOINT_FORM)
            .copyForm(NO_GEOPOINT_FORM)
            .clickFillBlankForm()
            .checkMapIconDisplayedForForm("Single geopoint")
            .checkMapIconNotDisplayedForForm("basic")
    }

    @Test
    fun fillingBlankForm_addsInstanceToMap() {
        stubGeopointIntent()

        rule.startAtMainMenu()
            .copyForm(SINGLE_GEOPOINT_FORM)
            .copyForm(NO_GEOPOINT_FORM)
            .clickFillBlankForm()
            .clickOnMapIconForForm("Single geopoint")
            .clickFillBlankFormButton("Single geopoint")

            .inputText("Foo")
            .swipeToNextQuestion("Location")
            .clickWidgetButton()
            .swipeToEndScreen()
            .clickSaveAndExitBackToMap()

            .assertText(
                getApplicationContext<Context>().resources.getString(
                    com.yedc.strings.R.string.select_item_count,
                    getApplicationContext<Context>().resources.getString(com.yedc.strings.R.string.saved_forms),
                    1,
                    1
                )
            )
            .selectForm(mapFragment, 1)
            .clickEditSavedForm("Single geopoint")
            .clickOnQuestion("Name")
            .assertText("Foo")
    }

    private fun stubGeopointIntent() {
        val location = Location("gps")
        location.latitude = 125.1
        location.longitude = 10.1
        location.altitude = 5.0

        val intent = getReturnIntent(com.yedc.geo.GeoUtils.formatLocationResultString(location))
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, intent)

        intending(hasComponent("com.yedc.geo.geopoint.GeoPointActivity"))
            .respondWith(result)
    }

    companion object {
        private const val SINGLE_GEOPOINT_FORM = "single-geopoint.xml"
        private const val NO_GEOPOINT_FORM = "basic.xml"
    }
}
