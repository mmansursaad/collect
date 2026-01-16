package com.yedc.android.formentry.saving;

import android.content.Context;

import androidx.annotation.NonNull;

import com.yedc.material.MaterialProgressDialogFragment;

public class SaveAnswerFileProgressDialogFragment extends MaterialProgressDialogFragment {

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setMessage(getString(com.yedc.strings.R.string.saving_file));
    }
}
