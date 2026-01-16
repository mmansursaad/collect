package com.yedc.android.widgets.utilities;

import androidx.activity.ComponentActivity;

import org.javarosa.form.api.FormEntryPrompt;
import com.yedc.android.utilities.FormEntryPromptUtils;
import com.yedc.audiorecorder.recorder.Output;
import com.yedc.audiorecorder.recording.AudioRecorder;
import com.yedc.permissions.PermissionsProvider;

public class InternalRecordingRequester implements RecordingRequester {

    private final ComponentActivity activity;
    private final AudioRecorder audioRecorder;
    private final PermissionsProvider permissionsProvider;

    public InternalRecordingRequester(ComponentActivity activity, AudioRecorder audioRecorder, PermissionsProvider permissionsProvider) {
        this.activity = activity;
        this.audioRecorder = audioRecorder;
        this.permissionsProvider = permissionsProvider;
    }

    @Override
    public void requestRecording(FormEntryPrompt prompt) {
        permissionsProvider.requestRecordAudioPermission(activity, () -> {
            String quality = FormEntryPromptUtils.getBindAttribute(prompt, "quality");
            if (quality != null && quality.equals("voice-only")) {
                audioRecorder.start(prompt.getIndex(), Output.AMR);
            } else if (quality != null && quality.equals("low")) {
                audioRecorder.start(prompt.getIndex(), Output.AAC_LOW);
            } else {
                audioRecorder.start(prompt.getIndex(), Output.AAC);
            }
        });
    }
}
