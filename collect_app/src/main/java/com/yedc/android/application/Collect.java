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

package com.yedc.android.application;

import static com.yedc.settings.keys.MetaKeys.KEY_GOOGLE_BUG_154855417_FIXED;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import com.yedc.android.dynamicpreload.ExternalDataManager;
import com.yedc.qrcode.mlkit.MlKitBarcodeScannerViewFactory;
import com.yedc.android.injection.DaggerUtils;
import com.yedc.android.injection.config.AppDependencyComponent;
import com.yedc.android.injection.config.CollectDrawDependencyModule;
import com.yedc.android.injection.config.CollectGeoDependencyModule;
import com.yedc.android.injection.config.CollectGoogleMapsDependencyModule;
import com.yedc.android.injection.config.CollectOsmDroidDependencyModule;
import com.yedc.android.injection.config.CollectProjectsDependencyModule;
import com.yedc.android.injection.config.CollectSelfieCameraDependencyModule;
import com.yedc.android.injection.config.DaggerAppDependencyComponent;
import com.yedc.android.utilities.CollectStrictMode;
import com.yedc.android.utilities.FormsRepositoryProvider;
import com.yedc.android.utilities.LocaleHelper;
import com.yedc.androidshared.data.AppState;
import com.yedc.androidshared.data.StateStore;
import com.yedc.androidshared.system.ExternalFilesUtils;
import com.yedc.async.Scheduler;
import com.yedc.async.network.NetworkStateProvider;
import com.yedc.audiorecorder.AudioRecorderDependencyComponent;
import com.yedc.audiorecorder.AudioRecorderDependencyComponentProvider;
import com.yedc.audiorecorder.DaggerAudioRecorderDependencyComponent;
import com.yedc.crashhandler.CrashHandler;
import com.yedc.draw.DaggerDrawDependencyComponent;
import com.yedc.draw.DrawDependencyComponent;
import com.yedc.draw.DrawDependencyComponentProvider;
import com.yedc.entities.DaggerEntitiesDependencyComponent;
import com.yedc.entities.EntitiesDependencyComponent;
import com.yedc.entities.EntitiesDependencyComponentProvider;
import com.yedc.entities.EntitiesDependencyModule;
import com.yedc.entities.storage.EntitiesRepository;
import com.yedc.forms.Form;
import com.yedc.geo.DaggerGeoDependencyComponent;
import com.yedc.geo.GeoDependencyComponent;
import com.yedc.geo.GeoDependencyComponentProvider;
import com.yedc.googlemaps.DaggerGoogleMapsDependencyComponent;
import com.yedc.googlemaps.GoogleMapsDependencyComponent;
import com.yedc.googlemaps.GoogleMapsDependencyComponentProvider;
import com.yedc.location.LocationClient;
import com.yedc.maps.layers.ReferenceLayerRepository;
import com.yedc.osmdroid.DaggerOsmDroidDependencyComponent;
import com.yedc.osmdroid.OsmDroidDependencyComponent;
import com.yedc.osmdroid.OsmDroidDependencyComponentProvider;
import com.yedc.projects.DaggerProjectsDependencyComponent;
import com.yedc.projects.ProjectsDependencyComponent;
import com.yedc.projects.ProjectsDependencyComponentProvider;
import com.yedc.selfiecamera.DaggerSelfieCameraDependencyComponent;
import com.yedc.selfiecamera.SelfieCameraDependencyComponent;
import com.yedc.selfiecamera.SelfieCameraDependencyComponentProvider;
import com.yedc.settings.SettingsProvider;
import com.yedc.settings.keys.ProjectKeys;
import com.yedc.shared.injection.ObjectProvider;
import com.yedc.shared.injection.ObjectProviderHost;
import com.yedc.shared.injection.SupplierObjectProvider;
import com.yedc.shared.settings.Settings;
import com.yedc.shared.strings.Md5;
import com.yedc.strings.localization.LocalizedApplication;

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
