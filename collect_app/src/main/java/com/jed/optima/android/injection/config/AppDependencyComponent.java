package com.jed.optima.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import com.jed.optima.android.activities.AboutActivity;
import com.jed.optima.android.activities.AppListActivity;
import com.jed.optima.android.activities.DeleteFormsActivity;
import com.jed.optima.android.activities.FirstLaunchActivity;
import com.jed.optima.android.activities.FormDownloadListActivity;
import com.jed.optima.android.activities.FormFillingActivity;
import com.jed.optima.android.activities.FormMapActivity;
import com.jed.optima.android.activities.InstanceChooserList;
import com.jed.optima.android.application.Collect;
import com.jed.optima.android.application.initialization.ApplicationInitializer;
import com.jed.optima.android.application.initialization.ExistingProjectMigrator;
import com.jed.optima.android.audio.AudioRecordingControllerFragment;
import com.jed.optima.android.audio.AudioRecordingErrorDialogFragment;
import com.jed.optima.android.backgroundwork.AutoUpdateTaskSpec;
import com.jed.optima.android.backgroundwork.SendFormsTaskSpec;
import com.jed.optima.android.backgroundwork.SyncFormsTaskSpec;
import com.jed.optima.android.configure.qr.QRCodeScannerFragment;
import com.jed.optima.android.configure.qr.QRCodeTabsActivity;
import com.jed.optima.android.configure.qr.ShowQRCodeFragment;
import com.jed.optima.android.entities.EntitiesRepositoryProvider;
import com.jed.optima.android.external.AndroidShortcutsActivity;
import com.jed.optima.android.external.FormUriActivity;
import com.jed.optima.android.external.FormsProvider;
import com.jed.optima.android.external.InstanceProvider;
import com.jed.optima.android.formentry.BackgroundAudioPermissionDialogFragment;
import com.jed.optima.android.formentry.ODKView;
import com.jed.optima.android.formentry.repeats.DeleteRepeatDialogFragment;
import com.jed.optima.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import com.jed.optima.android.formentry.saving.SaveFormProgressDialogFragment;
import com.jed.optima.android.formhierarchy.FormHierarchyFragmentHostActivity;
import com.jed.optima.android.formlists.blankformlist.BlankFormListActivity;
import com.jed.optima.android.formmanagement.OpenRosaClientProvider;
import com.jed.optima.android.formmanagement.FormsDataService;
import com.jed.optima.android.fragments.BarCodeScannerFragment;
import com.jed.optima.android.fragments.dialogs.FormsDownloadResultDialog;
import com.jed.optima.android.fragments.dialogs.SelectMinimalDialog;
import com.jed.optima.android.instancemanagement.send.InstanceUploaderActivity;
import com.jed.optima.android.instancemanagement.send.InstanceUploaderListActivity;
import com.jed.optima.android.mainmenu.MainMenuActivity;
import com.jed.optima.android.preferences.dialogs.AdminPasswordDialogFragment;
import com.jed.optima.android.preferences.dialogs.ChangeAdminPasswordDialog;
import com.jed.optima.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import com.jed.optima.android.preferences.dialogs.ServerAuthDialogFragment;
import com.jed.optima.android.preferences.screens.BasePreferencesFragment;
import com.jed.optima.android.preferences.screens.BaseProjectPreferencesFragment;
import com.jed.optima.android.preferences.screens.ExperimentalPreferencesFragment;
import com.jed.optima.android.preferences.screens.FormManagementPreferencesFragment;
import com.jed.optima.android.preferences.screens.FormMetadataPreferencesFragment;
import com.jed.optima.android.preferences.screens.IdentityPreferencesFragment;
import com.jed.optima.android.preferences.screens.MapsPreferencesFragment;
import com.jed.optima.android.preferences.screens.ProjectDisplayPreferencesFragment;
import com.jed.optima.android.preferences.screens.ProjectManagementPreferencesFragment;
import com.jed.optima.android.preferences.screens.ProjectPreferencesActivity;
import com.jed.optima.android.preferences.screens.ProjectPreferencesFragment;
import com.jed.optima.android.preferences.screens.ServerPreferencesFragment;
import com.jed.optima.android.preferences.screens.UserInterfacePreferencesFragment;
import com.jed.optima.android.projects.ManualProjectCreatorDialog;
import com.jed.optima.android.projects.ProjectResetter;
import com.jed.optima.android.projects.ProjectSettingsDialog;
import com.jed.optima.android.projects.ProjectsDataService;
import com.jed.optima.android.projects.QrCodeProjectCreatorDialog;
import com.jed.optima.android.storage.StoragePathProvider;
import com.jed.optima.android.tasks.DownloadFormListTask;
import com.jed.optima.android.tasks.InstanceUploaderTask;
import com.jed.optima.android.tasks.MediaLoadingTask;
import com.jed.optima.android.utilities.AuthDialogUtility;
import com.jed.optima.android.utilities.FormsRepositoryProvider;
import com.jed.optima.android.utilities.InstancesRepositoryProvider;
import com.jed.optima.android.utilities.SavepointsRepositoryProvider;
import com.jed.optima.android.utilities.ThemeUtils;
import com.jed.optima.android.widgets.QuestionWidget;
import com.jed.optima.android.widgets.items.SelectOneFromMapDialogFragment;
import com.jed.optima.async.Scheduler;
import com.jed.optima.async.network.NetworkStateProvider;
import com.jed.optima.draw.DrawActivity;
import com.jed.optima.googlemaps.GoogleMapFragment;
import com.jed.optima.location.LocationClient;
import com.jed.optima.maps.MapFragmentFactory;
import com.jed.optima.maps.layers.ReferenceLayerRepository;
import com.jed.optima.openrosa.http.OpenRosaHttpInterface;
import com.jed.optima.permissions.PermissionsChecker;
import com.jed.optima.permissions.PermissionsProvider;
import com.jed.optima.projects.ProjectsRepository;
import com.jed.optima.settings.ODKAppSettingsImporter;
import com.jed.optima.settings.SettingsProvider;
import com.jed.optima.webpage.ExternalWebPageHelper;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;

/**
 * Dagger component for the application. Should include
 * application level Dagger Modules and be built with Application
 * object.
 * <p>
 * Add an `inject(MyClass myClass)` method here for objects you want
 * to inject into so Dagger knows to wire it up.
 * <p>
 * Annotated with @Singleton so modules can include @Singletons that will
 * be retained at an application level (as this an instance of this components
 * is owned by the Application object).
 * <p>
 * If you need to call a provider directly from the component (in a test
 * for example) you can add a method with the type you are looking to fetch
 * (`MyType myType()`) to this interface.
 * <p>
 * To read more about Dagger visit: https://google.github.io/dagger/users-guide
 **/

@Singleton
@Component(modules = {
        AppDependencyModule.class
})
public interface AppDependencyComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        Builder appDependencyModule(AppDependencyModule testDependencyModule);

        AppDependencyComponent build();
    }

    void inject(Collect collect);

    void inject(AboutActivity aboutActivity);

    void inject(FormFillingActivity formFillingActivity);

    void inject(InstanceUploaderTask uploader);

    void inject(ServerPreferencesFragment serverPreferencesFragment);

    void inject(ProjectDisplayPreferencesFragment projectDisplayPreferencesFragment);

    void inject(ProjectManagementPreferencesFragment projectManagementPreferencesFragment);

    void inject(AuthDialogUtility authDialogUtility);

    void inject(FormDownloadListActivity formDownloadListActivity);

    void inject(InstanceUploaderListActivity activity);

    /**
     * @deprecated should use {@link QuestionWidget.Dependencies} instead
     */
    @Deprecated
    void inject(QuestionWidget questionWidget);

    void inject(ODKView odkView);

    void inject(FormMetadataPreferencesFragment formMetadataPreferencesFragment);

    void inject(FormMapActivity formMapActivity);

    void inject(GoogleMapFragment mapFragment);

    void inject(MainMenuActivity mainMenuActivity);

    void inject(QRCodeTabsActivity qrCodeTabsActivity);

    void inject(ShowQRCodeFragment showQRCodeFragment);

    void inject(SendFormsTaskSpec sendFormsTaskSpec);

    void inject(AdminPasswordDialogFragment adminPasswordDialogFragment);

    void inject(FormManagementPreferencesFragment formManagementPreferencesFragment);

    void inject(IdentityPreferencesFragment identityPreferencesFragment);

    void inject(UserInterfacePreferencesFragment userInterfacePreferencesFragment);

    void inject(SaveFormProgressDialogFragment saveFormProgressDialogFragment);

    void inject(BarCodeScannerFragment barCodeScannerFragment);

    void inject(QRCodeScannerFragment qrCodeScannerFragment);

    void inject(ProjectPreferencesActivity projectPreferencesActivity);

    void inject(ResetDialogPreferenceFragmentCompat resetDialogPreferenceFragmentCompat);

    void inject(SyncFormsTaskSpec syncWork);

    void inject(ExperimentalPreferencesFragment experimentalPreferencesFragment);

    void inject(AutoUpdateTaskSpec autoUpdateTaskSpec);

    void inject(ServerAuthDialogFragment serverAuthDialogFragment);

    void inject(BasePreferencesFragment basePreferencesFragment);

    void inject(InstanceUploaderActivity instanceUploaderActivity);

    void inject(ProjectPreferencesFragment projectPreferencesFragment);

    void inject(DeleteFormsActivity deleteFormsActivity);

    void inject(SelectMinimalDialog selectMinimalDialog);

    void inject(AudioRecordingControllerFragment audioRecordingControllerFragment);

    void inject(SaveAnswerFileErrorDialogFragment saveAnswerFileErrorDialogFragment);

    void inject(AudioRecordingErrorDialogFragment audioRecordingErrorDialogFragment);

    void inject(InstanceChooserList instanceChooserList);

    void inject(FormsProvider formsProvider);

    void inject(InstanceProvider instanceProvider);

    void inject(BackgroundAudioPermissionDialogFragment backgroundAudioPermissionDialogFragment);

    void inject(ChangeAdminPasswordDialog changeAdminPasswordDialog);

    void inject(MediaLoadingTask mediaLoadingTask);

    void inject(ThemeUtils themeUtils);

    void inject(BaseProjectPreferencesFragment baseProjectPreferencesFragment);

    void inject(AndroidShortcutsActivity androidShortcutsActivity);

    void inject(ProjectSettingsDialog projectSettingsDialog);

    void inject(ManualProjectCreatorDialog manualProjectCreatorDialog);

    void inject(QrCodeProjectCreatorDialog qrCodeProjectCreatorDialog);

    void inject(FirstLaunchActivity firstLaunchActivity);

    void inject(FormUriActivity formUriActivity);

    void inject(MapsPreferencesFragment mapsPreferencesFragment);

    void inject(FormsDownloadResultDialog formsDownloadResultDialog);

    void inject(SelectOneFromMapDialogFragment selectOneFromMapDialogFragment);

    void inject(DrawActivity drawActivity);

    void inject(BlankFormListActivity blankFormListActivity);

    void inject(DeleteRepeatDialogFragment deleteRepeatDialogFragment);

    void inject(AppListActivity appListActivity);

    void inject(DownloadFormListTask downloadFormListTask);

    void inject(FormHierarchyFragmentHostActivity formHierarchyFragmentHostActivity);

    OpenRosaHttpInterface openRosaHttpInterface();

    ReferenceManager referenceManager();

    SettingsProvider settingsProvider();

    ApplicationInitializer applicationInitializer();

    ODKAppSettingsImporter settingsImporter();

    ProjectsRepository projectsRepository();

    ProjectsDataService currentProjectProvider();

    StoragePathProvider storagePathProvider();

    FormsRepositoryProvider formsRepositoryProvider();

    InstancesRepositoryProvider instancesRepositoryProvider();

    SavepointsRepositoryProvider savepointsRepositoryProvider();

    OpenRosaClientProvider formSourceProvider();

    ExistingProjectMigrator existingProjectMigrator();

    ProjectResetter projectResetter();

    MapFragmentFactory mapFragmentFactory();

    Scheduler scheduler();

    LocationClient locationClient();

    PermissionsProvider permissionsProvider();

    PermissionsChecker permissionsChecker();

    ReferenceLayerRepository referenceLayerRepository();

    NetworkStateProvider networkStateProvider();

    EntitiesRepositoryProvider entitiesRepositoryProvider();

    FormsDataService formsDataService();

    ProjectDependencyModuleFactory projectDependencyModuleFactory();

    ExternalWebPageHelper externalWebPageHelper();
}
