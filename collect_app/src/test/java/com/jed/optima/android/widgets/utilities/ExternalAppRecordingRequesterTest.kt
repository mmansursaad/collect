package com.jed.optima.android.widgets.utilities

import android.app.Activity
import android.provider.MediaStore
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import com.jed.optima.android.R
import com.jed.optima.android.fakes.FakePermissionsProvider
import com.jed.optima.androidshared.system.IntentLauncher
import com.jed.optima.androidshared.system.IntentLauncherImpl
import com.jed.optima.testshared.ErrorIntentLauncher
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class ExternalAppRecordingRequesterTest {
    private lateinit var intentLauncher: IntentLauncher
    private val permissionsProvider = FakePermissionsProvider()
    private val waitingForDataRegistry =
        _root_ide_package_.com.jed.optima.android.widgets.support.FakeWaitingForDataRegistry()
    private lateinit var activity: Activity
    private lateinit var requester: ExternalAppRecordingRequester

    @Before
    fun setup() {
        activity = Robolectric.buildActivity(Activity::class.java).get()
    }

    private fun setupRequester() {
        requester = ExternalAppRecordingRequester(activity, intentLauncher, waitingForDataRegistry, permissionsProvider)
    }

    @Test
    fun requestRecording_whenIntentIsNotAvailable_doesNotStartAnyIntentAndCancelsWaitingForData() {
        intentLauncher = ErrorIntentLauncher()
        setupRequester()
        permissionsProvider.setPermissionGranted(true)
        requester.requestRecording(_root_ide_package_.com.jed.optima.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer(null))
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        val toastMessage = ShadowToast.getTextOfLatestToast()
        assertThat(startedActivity, nullValue())
        assertThat(waitingForDataRegistry.waiting.isEmpty(), `is`(true))
        assertThat(
            toastMessage,
            equalTo(
                activity.getString(
                    com.jed.optima.strings.R.string.activity_not_found,
                    activity.getString(com.jed.optima.strings.R.string.capture_audio)
                )
            )
        )
    }

    @Test
    fun requestRecording_whenPermissionIsNotGranted_doesNotStartAnyIntentAndCancelsWaitingForData() {
        intentLauncher = IntentLauncherImpl
        setupRequester()
        permissionsProvider.setPermissionGranted(false)
        requester.requestRecording(_root_ide_package_.com.jed.optima.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer(null))
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        assertThat(startedActivity, nullValue())
        assertThat(waitingForDataRegistry.waiting.isEmpty(), equalTo(true))
    }

    @Test
    fun requestRecording_whenPermissionIsGranted_startsRecordSoundIntentAndSetsWidgetWaitingForData() {
        intentLauncher = IntentLauncherImpl
        setupRequester()
        permissionsProvider.setPermissionGranted(true)
        val prompt = _root_ide_package_.com.jed.optima.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer(null)
        requester.requestRecording(prompt)
        val startedActivity = Shadows.shadowOf(activity).nextStartedActivity
        assertThat(
            startedActivity.action,
            equalTo(MediaStore.Audio.Media.RECORD_SOUND_ACTION)
        )
        assertThat(
            startedActivity.getStringExtra(MediaStore.EXTRA_OUTPUT),
            equalTo(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    .toString()
            )
        )
        val intentForResult = Shadows.shadowOf(activity).nextStartedActivityForResult
        assertThat(
            intentForResult.requestCode,
            equalTo(_root_ide_package_.com.jed.optima.android.utilities.ApplicationConstants.RequestCodes.AUDIO_CAPTURE)
        )
        assertThat(
            waitingForDataRegistry.waiting.contains(prompt.index),
            equalTo(true)
        )
    }
}
