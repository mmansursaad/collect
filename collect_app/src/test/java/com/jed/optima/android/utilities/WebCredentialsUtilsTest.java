package com.jed.optima.android.utilities;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import com.jed.optima.metadata.PropertyManager;
import com.jed.optima.settings.keys.ProjectKeys;
import com.jed.optima.shared.settings.Settings;

public class WebCredentialsUtilsTest {

    @Test
    public void saveCredentialsPreferencesMethod_shouldSaveNewCredentialsAndReloadPropertyManager() {
        Settings generalSettings = mock(Settings.class);
        WebCredentialsUtils webCredentialsUtils = new WebCredentialsUtils(generalSettings);
        PropertyManager propertyManager = mock(PropertyManager.class);

        webCredentialsUtils.saveCredentialsPreferences("username", "password", propertyManager);

        verify(generalSettings, times(1)).save(ProjectKeys.KEY_USERNAME, "username");
        verify(generalSettings, times(1)).save(ProjectKeys.KEY_PASSWORD, "password");
        verify(propertyManager, times(1)).reload();
    }
}
