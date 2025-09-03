package com.jed.optima.android.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;

import com.jed.optima.material.MaterialProgressDialogFragment;

public class SaveAnswerFileProgressDialogFragment extends MaterialProgressDialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setMessage(getString(com.jed.optima.strings.R.string.saving_file));
    }
}
