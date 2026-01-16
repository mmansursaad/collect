package com.yedc.android.audio;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.yedc.android.injection.DaggerUtils;
import com.yedc.audiorecorder.recording.AudioRecorder;
import com.yedc.audiorecorder.recording.MicInUseException;
import com.yedc.androidshared.data.Consumable;

import javax.inject.Inject;

public class AudioRecordingErrorDialogFragment extends DialogFragment {

    @Inject
    AudioRecorder audioRecorder;

    @Nullable
    Consumable<Exception> exception;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
        exception = audioRecorder.failedToStart().getValue();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext())
                .setPositiveButton(com.yedc.strings.R.string.ok, null);

        if (exception != null && exception.getValue() instanceof MicInUseException) {
            dialogBuilder.setMessage(com.yedc.strings.R.string.mic_in_use);
        } else {
            dialogBuilder.setMessage(com.yedc.strings.R.string.start_recording_failed);
        }

        return dialogBuilder.create();
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (exception != null) {
            exception.consume();
        }
    }
}
