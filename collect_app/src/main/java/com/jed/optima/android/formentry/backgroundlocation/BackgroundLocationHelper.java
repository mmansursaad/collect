package com.jed.optima.android.formentry.backgroundlocation;

import static com.jed.optima.settings.keys.ProjectKeys.KEY_BACKGROUND_LOCATION;

import android.location.Location;

import com.jed.optima.android.activities.FormFillingActivity;
import com.jed.optima.android.application.Collect;
import com.jed.optima.android.formentry.FormSession;
import com.jed.optima.android.formentry.FormSessionRepository;
import com.jed.optima.android.formentry.audit.AuditConfig;
import com.jed.optima.android.formentry.audit.AuditEvent;
import com.jed.optima.android.javarosawrapper.FormController;
import com.jed.optima.androidshared.system.PlayServicesChecker;
import com.jed.optima.permissions.PermissionsProvider;
import com.jed.optima.shared.settings.Settings;

import javax.annotation.Nullable;

/**
 * Wrapper on resources needed by {@link BackgroundLocationManager} to make testing easier.
 *
 * Ideally this would be replaced by more coherent abstractions in the future.
 *
 * The methods on the {@link FormController} are wrapped here rather
 * than the form controller being passed in when constructing the {@link BackgroundLocationManager}
 * because the form controller isn't set until
 * {@link FormFillingActivity}'s onCreate.
 */
public class BackgroundLocationHelper {

    private final PermissionsProvider permissionsProvider;
    private final Settings generalSettings;
    private final FormSessionRepository formSessionRepository;
    private final String sessionId;

    public BackgroundLocationHelper(
            PermissionsProvider permissionsProvider,
            Settings generalSettings,
            FormSessionRepository formSessionRepository,
            String sessionId
    ) {
        this.permissionsProvider = permissionsProvider;
        this.generalSettings = generalSettings;
        this.formSessionRepository = formSessionRepository;
        this.sessionId = sessionId;
    }

    boolean isAndroidLocationPermissionGranted() {
        return permissionsProvider.areLocationPermissionsGranted();
    }

    boolean isBackgroundLocationPreferenceEnabled() {
        return generalSettings.getBoolean(KEY_BACKGROUND_LOCATION);
    }

    boolean arePlayServicesAvailable() {
        return new PlayServicesChecker().isGooglePlayServicesAvailable(Collect.getInstance().getApplicationContext());
    }

    /**
     * @return true if the global form controller has been initialized.
     */
    boolean isCurrentFormSet() {
        return getFormController() != null;
    }

    /**
     * @return true if the current form definition requests any kind of background location.
     *
     * Precondition: the global form controller has been initialized.
     */
    boolean currentFormCollectsBackgroundLocation() {
        return getFormController().currentFormCollectsBackgroundLocation();
    }

    /**
     * @return true if the current form definition requests a background location audit, false
     * otherwise.
     *
     * Precondition: the global form controller has been initialized.
     */
    boolean currentFormAuditsLocation() {
        return getFormController().currentFormAuditsLocation();
    }

    /**
     * @return the configuration for the audit requested by the current form definition.
     *
     * Precondition: the global form controller has been initialized.
     */
    AuditConfig getCurrentFormAuditConfig() {
        return getFormController().getSubmissionMetadata().auditConfig;
    }

    /**
     * Logs an audit event of the given type.
     *
     * Precondition: the global form controller has been initialized.
     */
    void logAuditEvent(AuditEvent.AuditEventType eventType) {
        getFormController().getAuditEventLogger().logEvent(eventType, false, System.currentTimeMillis());
    }

    /**
     * Provides the location to the global audit event logger.
     *
     * Precondition: the global form controller has been initialized.
     */
    void provideLocationToAuditLogger(Location location) {
        getFormController().getAuditEventLogger().addLocation(location);
    }

    @Nullable
    private FormController getFormController() {
        FormSession formSession = formSessionRepository.get(sessionId).getValue();
        return formSession == null ? null : formSession.getFormController();
    }
}
