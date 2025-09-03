package com.jed.optima.android.preferences.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.jed.optima.android.databinding.ServerAuthDialogBinding;
import com.jed.optima.android.injection.DaggerUtils;
import com.jed.optima.settings.SettingsProvider;
import com.jed.optima.settings.keys.ProjectKeys;
import com.jed.optima.shared.settings.Settings;

import javax.inject.Inject;

public class ServerAuthDialogFragment extends DialogFragment {

    @Inject
    SettingsProvider settingsProvider;

    private View dialogView;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        ServerAuthDialogBinding binding = ServerAuthDialogBinding.inflate(requireActivity().getLayoutInflater());
        dialogView = binding.getRoot();

        Settings generalSettings = settingsProvider.getUnprotectedSettings();
        binding.usernameEdit.setText(generalSettings.getString(ProjectKeys.KEY_USERNAME));
        binding.passwordEdit.setText(generalSettings.getString(ProjectKeys.KEY_PASSWORD));

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle(com.jed.optima.strings.R.string.server_requires_auth)
                .setMessage(requireContext().getString(com.jed.optima.strings.R.string.server_auth_credentials, generalSettings.getString(ProjectKeys.KEY_SERVER_URL)))
                .setView(dialogView)
                .setPositiveButton(com.jed.optima.strings.R.string.ok, (dialogInterface, i) -> {
                    generalSettings.save(ProjectKeys.KEY_USERNAME, binding.usernameEdit.getText().toString());
                    generalSettings.save(ProjectKeys.KEY_PASSWORD, binding.passwordEdit.getText().toString());
                })
                .create();
    }

    public View getDialogView() {
        return dialogView;
    }
}
