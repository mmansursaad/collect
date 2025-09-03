/*
 * Copyright (C) 2017 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.jed.optima.android.application;

import static com.jed.optima.settings.keys.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import com.jed.optima.android.dynamicpreload.ExternalDataManager;
import com.jed.optima.qrcode.mlkit.MlKitBarcodeScannerViewFactory;
import com.jed.optima.android.injection.DaggerUtils;
import com.jed.optima.android.injection.config.AppDependencyComponent;
import com.jed.optima.android.injection.config.CollectDrawDependencyModule;
import com.jed.optima.android.injection.config.CollectGeoDependencyModule;
import com.jed.optima.android.injection.config.CollectGoogleMapsDependencyModule;
import com.jed.optima.android.injection.config.CollectOsmDroidDependencyModule;
import com.jed.optima.android.injection.config.CollectProjectsDependencyModule;
import com.jed.optima.android.injection.config.CollectSelfieCameraDependencyModule;
import com.jed.optima.android.injection.config.DaggerAppDependencyComponent;
import com.jed.optima.android.utilities.CollectStrictMode;
import com.jed.optima.android.utilities.FormsRepositoryProvider;
import com.jed.optima.android.utilities.LocaleHelper;
import com.jed.optima.androidshared.data.AppState;
import com.jed.optima.androidshared.data.StateStore;
import com.jed.optima.androidshared.system.ExternalFilesUtils;
import com.jed.optima.async.Scheduler;
import com.jed.optima.async.network.NetworkStateProvider;
import com.jed.optima.audiorecorder.AudioRecorderDependencyComponent;
import com.jed.optima.audiorecorder.AudioRecorderDependencyComponentProvider;
import com.jed.optima.audiorecorder.DaggerAudioRecorderDependencyComponent;
import com.jed.optima.crashhandler.CrashHandler;
import com.jed.optima.draw.DaggerDrawDependencyComponent;
import com.jed.optima.draw.DrawDependencyComponent;
import com.jed.optima.draw.DrawDependencyComponentProvider;
import com.jed.optima.entities.DaggerEntitiesDependencyComponent;
import com.jed.optima.entities.EntitiesDependencyComponent;
import com.jed.optima.entities.EntitiesDependencyComponentProvider;
import com.jed.optima.entities.EntitiesDependencyModule;
import com.jed.optima.entities.storage.EntitiesRepository;
import com.jed.optima.forms.Form;
import com.jed.optima.geo.DaggerGeoDependencyComponent;
import com.jed.optima.geo.GeoDependencyComponent;
import com.jed.optima.geo.GeoDependencyComponentProvider;
import com.jed.optima.googlemaps.DaggerGoogleMapsDependencyComponent;
import com.jed.optima.googlemaps.GoogleMapsDependencyComponent;
import com.jed.optima.googlemaps.GoogleMapsDependencyComponentProvider;
import com.jed.optima.location.LocationClient;
import com.jed.optima.maps.layers.ReferenceLayerRepository;
import com.jed.optima.osmdroid.DaggerOsmDroidDependencyComponent;
import com.jed.optima.osmdroid.OsmDroidDependencyComponent;
import com.jed.optima.osmdroid.OsmDroidDependencyComponentProvider;
import com.jed.optima.projects.DaggerProjectsDependencyComponent;
import com.jed.optima.projects.ProjectsDependencyComponent;
import com.jed.optima.projects.ProjectsDependencyComponentProvider;
import com.jed.optima.selfiecamera.DaggerSelfieCameraDependencyComponent;
import com.jed.optima.selfiecamera.SelfieCameraDependencyComponent;
import com.jed.optima.selfiecamera.SelfieCameraDependencyComponentProvider;
import com.jed.optima.settings.SettingsProvider;
import com.jed.optima.settings.keys.ProjectKeys;
import com.jed.optima.shared.injection.ObjectProvider;
import com.jed.optima.shared.injection.ObjectProviderHost;
import com.jed.optima.shared.injection.SupplierObjectProvider;
import com.jed.optima.shared.settings.Settings;
import com.jed.optima.shared.strings.Md5;
import com.jed.optima.strings.localization.LocalizedApplication;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Locale;

public class Collect extends Application implements
        LocalizedApplication,
        AudioRecorderDependencyComponentProvider,
        ProjectsDependencyComponentProvider,
        GeoDependencyComponentProvider,
        OsmDroidDependencyComponentProvider,
        StateStore,
        ObjectProviderHost,
        EntitiesDependencyComponentProvider,
        SelfieCameraDependencyComponentProvider,
        GoogleMapsDependencyComponentProvider,
        DrawDependencyComponentProvider {

    public static String defaultSysLanguage;
    private static Collect singleton;

    private final AppState appState = new AppState();
    private final SupplierObjectProvider objectProvider = new SupplierObjectProvider();

    private ExternalDataManager externalDataManager;
    private AppDependencyComponent applicationComponent;

    private AudioRecorderDependencyComponent audioRecorderDependencyComponent;
    private ProjectsDependencyComponent projectsDependencyComponent;
    private GeoDependencyComponent geoDependencyComponent;
    private OsmDroidDependencyComponent osmDroidDependencyComponent;
    private EntitiesDependencyComponent entitiesDependencyComponent;
    private SelfieCameraDependencyComponent selfieCameraDependencyComponent;
    private GoogleMapsDependencyComponent googleMapsDependencyComponent;
    private DrawDependencyComponent drawDependencyComponent;

    /**
     * @deprecated we shouldn't have to reference a static singleton of the application. Code doing this
     * should either have a {@link Context} instance passed to it (or have any references removed if
     * possible).
     */
    @Deprecated
    public static Collect getInstance() {
        return singleton;
    }

    public ExternalDataManager getExternalDataManager() {
        return externalDataManager;
    }

    public void setExternalDataManager(ExternalDataManager externalDataManager) {
        this.externalDataManager = externalDataManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;

        CrashHandler.install(this).launchApp(
                () -> ExternalFilesUtils.testExternalFilesAccess(this),
                () -> {
                    setupDagger();
                    DaggerUtils.getComponent(this).inject(this);

                    applicationComponent.applicationInitializer().initialize();
                    fixGoogleBug154855417();
                    CollectStrictMode.enable();
                    MlKitBarcodeScannerViewFactory.init(this);
                }
        );
    }

    private void setupDagger() {
        applicationComponent = DaggerAppDependencyComponent.builder()
                .application(this)
                .build();

        audioRecorderDependencyComponent = DaggerAudioRecorderDependencyComponent.builder()
                .application(this)
                .build();

        projectsDependencyComponent = DaggerProjectsDependencyComponent.builder()
                .projectsDependencyModule(new CollectProjectsDependencyModule(applicationComponent))
                .build();

        selfieCameraDependencyComponent = DaggerSelfieCameraDependencyComponent.builder()
                .selfieCameraDependencyModule(new CollectSelfieCameraDependencyModule(applicationComponent))
                .build();

        drawDependencyComponent = DaggerDrawDependencyComponent.builder()
                .drawDependencyModule(new CollectDrawDependencyModule(applicationComponent))
                .build();

        // Mapbox dependencies
        objectProvider.addSupplier(SettingsProvider.class, applicationComponent::settingsProvider);
        objectProvider.addSupplier(NetworkStateProvider.class, applicationComponent::networkStateProvider);
        objectProvider.addSupplier(ReferenceLayerRepository.class, applicationComponent::referenceLayerRepository);
        objectProvider.addSupplier(LocationClient.class, applicationComponent::locationClient);
    }

    @NotNull
    @Override
    public AudioRecorderDependencyComponent getAudioRecorderDependencyComponent() {
        return audioRecorderDependencyComponent;
    }

    @NotNull
    @Override
    public ProjectsDependencyComponent getProjectsDependencyComponent() {
        return projectsDependencyComponent;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //noinspection deprecation
        defaultSysLanguage = newConfig.locale.getLanguage();
    }

    @Nullable
    public AppDependencyComponent getComponent() {
        return applicationComponent;
    }

    public void setComponent(AppDependencyComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
        applicationComponent.inject(this);
    }

    /**
     * Gets a unique, privacy-preserving identifier for a form based on its id and version.
     *
     * @param formId      id of a form
     * @param formVersion version of a form
     * @return md5 hash of the form title, a space, the form ID
     */
    public static String getFormIdentifierHash(String formId, String formVersion) {
        Form form = new FormsRepositoryProvider(Collect.getInstance()).create().getLatestByFormIdAndVersion(formId, formVersion);

        String formTitle = form != null ? form.getDisplayName() : "";

        String formIdentifier = formTitle + " " + formId;
        return Md5.getMd5Hash(new ByteArrayInputStream(formIdentifier.getBytes()));
    }

    // https://issuetracker.google.com/issues/154855417
    private void fixGoogleBug154855417() {
        try {
            Settings metaSharedPreferences = applicationComponent.settingsProvider().getMetaSettings();

            boolean hasFixedGoogleBug154855417 = metaSharedPreferences.getBoolean(KEY_GOOGLE_BUG_154855417_FIXED);

            if (!hasFixedGoogleBug154855417) {
                File corruptedZoomTables = new File(getFilesDir(), "ZoomTables.data");
                corruptedZoomTables.delete();

                metaSharedPreferences.save(KEY_GOOGLE_BUG_154855417_FIXED, true);
            }
        } catch (Exception ignored) {
            // ignored
        }
    }

    @NotNull
    @Override
    public Locale getLocale() {
        if (this.applicationComponent != null) {
            return LocaleHelper.getLocale(applicationComponent.settingsProvider().getUnprotectedSettings().getString(ProjectKeys.KEY_APP_LANGUAGE));
        } else {
            return getResources().getConfiguration().locale;
        }
    }

    @NotNull
    @Override
    public AppState getState() {
        return appState;
    }

    @NonNull
    @Override
    public GeoDependencyComponent getGeoDependencyComponent() {
        if (geoDependencyComponent == null) {
            geoDependencyComponent = DaggerGeoDependencyComponent.builder()
                    .application(this)
                    .geoDependencyModule(new CollectGeoDependencyModule(applicationComponent))
                    .build();
        }

        return geoDependencyComponent;
    }

    @NonNull
    @Override
    public OsmDroidDependencyComponent getOsmDroidDependencyComponent() {
        if (osmDroidDependencyComponent == null) {
            osmDroidDependencyComponent = DaggerOsmDroidDependencyComponent.builder()
                    .osmDroidDependencyModule(new CollectOsmDroidDependencyModule(applicationComponent))
                    .build();
        }

        return osmDroidDependencyComponent;
    }

    @NonNull
    @Override
    public ObjectProvider getObjectProvider() {
        return objectProvider;
    }

    @NonNull
    @Override
    public EntitiesDependencyComponent getEntitiesDependencyComponent() {
        if (entitiesDependencyComponent == null) {
            entitiesDependencyComponent = DaggerEntitiesDependencyComponent.builder()
                    .entitiesDependencyModule(new EntitiesDependencyModule() {
                        @NonNull
                        @Override
                        public EntitiesRepository providesEntitiesRepository() {
                            String projectId = applicationComponent.currentProjectProvider().requireCurrentProject().getUuid();
                            return applicationComponent.entitiesRepositoryProvider().create(projectId);
                        }

                        @NonNull
                        @Override
                        public Scheduler providesScheduler() {
                            return applicationComponent.scheduler();
                        }
                    })
                    .build();
        }

        return entitiesDependencyComponent;
    }

    @NonNull
    @Override
    public SelfieCameraDependencyComponent getSelfieCameraDependencyComponent() {
        return selfieCameraDependencyComponent;
    }

    @NonNull
    @Override
    public GoogleMapsDependencyComponent getGoogleMapsDependencyComponent() {
        if (googleMapsDependencyComponent == null) {
            googleMapsDependencyComponent = DaggerGoogleMapsDependencyComponent.builder()
                    .googleMapsDependencyModule(new CollectGoogleMapsDependencyModule(applicationComponent))
                    .build();
        }

        return googleMapsDependencyComponent;
    }

    @NonNull
    @Override
    public DrawDependencyComponent getDrawDependencyComponent() {
        return drawDependencyComponent;
    }
}
