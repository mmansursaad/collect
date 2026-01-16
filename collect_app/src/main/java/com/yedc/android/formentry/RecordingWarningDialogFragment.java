package com.yedc.android.formentry;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class RecordingWarningDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(com.yedc.strings.R.string.recording)
                .setMessage(com.yedc.strings.R.string.recording_warning)
                .setPositiveButton(com.yedc.strings.R.string.ok, null)
                .create();
    }
}
