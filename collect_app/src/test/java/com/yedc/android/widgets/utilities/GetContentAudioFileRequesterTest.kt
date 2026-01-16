package com.yedc.android.widgets.utilities

import android.app.Activity
import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.yedc.android.R
import com.yedc.androidshared.system.IntentLauncher
import com.yedc.androidshared.system.IntentLauncherImpl
import com.yedc.testshared.ErrorIntentLauncher
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class GetContentAudioFileRequesterTest {
    private lateinit var intentLauncher: IntentLauncher
    private val waitingForDataRegistry =
        _root_ide_package_.com.yedc.android.widgets.support.FakeWaitingForDataRegistry()
    private lateinit var activity: Activity
    private lateinit var requester: GetContentAudioFileRequester

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
    }

    private fun setupRequester() {
        requester = GetContentAudioFileRequester(activity, intentLauncher, waitingForDataRegistry)
    }

    @Test
    fun requestFile_whenIntentIsNotAvailable_doesNotStartAnyIntentAndCancelsWaitingForData() {
        intentLauncher = ErrorIntentLauncher()
        setupRequester()
        requester.requestFile(_root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer(null))
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        val toastMessage = ShadowToast.getTextOfLatestToast()
        assertThat(startedActivity, nullValue())
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true))
        assertThat(
            toastMessage,
            equalTo(
                activity.getString(
                    com.yedc.strings.R.string.activity_not_found,
                    activity.getString(com.yedc.strings.R.string.choose_sound)
                )
            )
        )
    }

    @Test
    fun requestFile_startsChooseAudioFileActivityAndSetsWidgetWaitingForData() {
        intentLauncher = IntentLauncherImpl
        setupRequester()
        val prompt = _root_ide_package_.com.yedc.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer(null)
        requester.requestFile(prompt)
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        assertThat(
            startedActivity.action,
            equalTo(Intent.ACTION_GET_CONTENT)
        )
        assertThat(startedActivity.type, equalTo("audio/*"))
        val intentForResult = Shadows.shadowOf(activity).nextStartedActivityForResult
        assertThat(
            intentForResult.requestCode,
            equalTo(_root_ide_package_.com.yedc.android.utilities.ApplicationConstants.RequestCodes.AUDIO_CHOOSER)
        )
        assertThat(
            waitingForDataRegistry.waiting.contains(prompt.index),
            equalTo(true)
        )
    }
}
