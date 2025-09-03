package com.jed.optima.android.preferences.dialogs;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.jed.optima.android.R;
import com.jed.optima.android.TestSettingsProvider;
import com.jed.optima.fragmentstest.FragmentScenarioLauncherRule;
import com.jed.optima.settings.keys.ProjectKeys;
import com.jed.optima.shared.settings.Settings;
import com.jed.optima.testshared.RobolectricHelpers;

@RunWith(AndroidJUnit4.class)
public class ServerAuthDialogFragmentTest {

    private final Settings generalSettings = TestSettingsProvider.getUnprotectedSettings();

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule();

    @Test
    public void prefillsUsernameAndPassword() {
        generalSettings.save(ProjectKeys.KEY_USERNAME, "Alpen");
        generalSettings.save(ProjectKeys.KEY_PASSWORD, "swiss");

        FragmentScenario<ServerAuthDialogFragment> scenario = launcherRule.launch(ServerAuthDialogFragment.class);

        scenario.onFragment(fragment -> {
            EditText username = fragment.getDialogView().findViewById(R.id.username_edit);
            EditText password = fragment.getDialogView().findViewById(R.id.password_edit);

            assertThat(username.getText().toString(), is("Alpen"));
            assertThat(password.getText().toString(), is("swiss"));
        });
    }

    @Test
    public void clickingOK_savesUsernameAndPasswordToGeneralPrefs() {
        FragmentScenario<ServerAuthDialogFragment> scenario = launcherRule.launch(ServerAuthDialogFragment.class);

        scenario.onFragment(fragment -> {
            EditText username = fragment.getDialogView().findViewById(R.id.username_edit);
            EditText password = fragment.getDialogView().findViewById(R.id.password_edit);

            username.setText("Frederick Chilton");
            password.setText("chesapeake");
            ((AlertDialog) fragment.getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        });

        RobolectricHelpers.runLooper();
        assertThat(generalSettings.getString(ProjectKeys.KEY_USERNAME), is("Frederick Chilton"));
        assertThat(generalSettings.getString(ProjectKeys.KEY_PASSWORD), is("chesapeake"));
    }
}
