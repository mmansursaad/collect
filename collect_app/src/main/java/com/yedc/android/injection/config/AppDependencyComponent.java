package com.yedc.android.injection.config;

import android.app.Application;

import org.javarosa.core.reference.ReferenceManager;
import com.yedc.android.activities.AboutActivity;
import com.yedc.android.activities.AppListActivity;
import com.yedc.android.activities.DeleteFormsActivity;
import com.yedc.android.activities.FirstLaunchActivity;
import com.yedc.android.activities.FormDownloadListActivity;
import com.yedc.android.activities.FormFillingActivity;
import com.yedc.android.activities.FormMapActivity;
import com.yedc.android.activities.InstanceChooserList;
import com.yedc.android.application.Collect;
import com.yedc.android.application.initialization.ApplicationInitializer;
import com.yedc.android.application.initialization.ExistingProjectMigrator;
import com.yedc.android.audio.AudioRecordingControllerFragment;
import com.yedc.android.audio.AudioRecordingErrorDialogFragment;
import com.yedc.android.backgroundwork.AutoUpdateTaskSpec;
import com.yedc.android.backgroundwork.SendFormsTaskSpec;
import com.yedc.android.backgroundwork.SyncFormsTaskSpec;
import com.yedc.android.configure.qr.QRCodeScannerFragment;
import com.yedc.android.configure.qr.QRCodeTabsActivity;
import com.yedc.android.configure.qr.ShowQRCodeFragment;
import com.yedc.android.entities.EntitiesRepositoryProvider;
import com.yedc.android.external.AndroidShortcutsActivity;
import com.yedc.android.external.FormUriActivity;
import com.yedc.android.external.FormsProvider;
import com.yedc.android.external.InstanceProvider;
import com.yedc.android.formentry.BackgroundAudioPermissionDialogFragment;
import com.yedc.android.formentry.ODKView;
import com.yedc.android.formentry.repeats.DeleteRepeatDialogFragment;
import com.yedc.android.formentry.saving.SaveAnswerFileErrorDialogFragment;
import com.yedc.android.formentry.saving.SaveFormProgressDialogFragment;
import com.yedc.android.formhierarchy.FormHierarchyFragmentHostActivity;
import com.yedc.android.formlists.blankformlist.BlankFormListActivity;
import com.yedc.android.formmanagement.OpenRosaClientProvider;
import com.yedc.android.formmanagement.FormsDataService;
import com.yedc.android.fragments.BarCodeScannerFragment;
import com.yedc.android.fragments.dialogs.FormsDownloadResultDialog;
import com.yedc.android.fragments.dialogs.SelectMinimalDialog;
import com.yedc.android.instancemanagement.send.InstanceUploaderActivity;
import com.yedc.android.instancemanagement.send.InstanceUploaderListActivity;
import com.yedc.android.mainmenu.MainMenuActivity;
import com.yedc.android.preferences.dialogs.AdminPasswordDialogFragment;
import com.yedc.android.preferences.dialogs.ChangeAdminPasswordDialog;
import com.yedc.android.preferences.dialogs.ResetDialogPreferenceFragmentCompat;
import com.yedc.android.preferences.dialogs.ServerAuthDialogFragment;
import com.yedc.android.preferences.screens.BasePreferencesFragment;
import com.yedc.android.preferences.screens.BaseProjectPreferencesFragment;
import com.yedc.android.preferences.screens.ExperimentalPreferencesFragment;
import com.yedc.android.preferences.screens.FormManagementPreferencesFragment;
import com.yedc.android.preferences.screens.FormMetadataPreferencesFragment;
import com.yedc.android.preferences.screens.IdentityPreferencesFragment;
import com.yedc.android.preferences.screens.MapsPreferencesFragment;
import com.yedc.android.preferences.screens.ProjectDisplayPreferencesFragment;
import com.yedc.android.preferences.screens.ProjectManagementPreferencesFragment;
import com.yedc.android.preferences.screens.ProjectPreferencesActivity;
import com.yedc.android.preferences.screens.ProjectPreferencesFragment;
import com.yedc.android.preferences.screens.ServerPreferencesFragment;
import com.yedc.android.preferences.screens.UserInterfacePreferencesFragment;
import com.yedc.android.projects.ManualProjectCreatorDialog;
import com.yedc.android.projects.ProjectResetter;
import com.yedc.android.projects.ProjectSettingsDialog;
import com.yedc.android.projects.ProjectsDataService;
import com.yedc.android.projects.QrCodeProjectCreatorDialog;
import com.yedc.android.storage.StoragePathProvider;
import com.yedc.android.tasks.DownloadFormListTask;
import com.yedc.android.tasks.InstanceUploaderTask;
import com.yedc.android.tasks.MediaLoadingTask;
import com.yedc.android.utilities.AuthDialogUtility;
import com.yedc.android.utilities.FormsRepositoryProvider;
import com.yedc.android.utilities.InstancesRepositoryProvider;
import com.yedc.android.utilities.SavepointsRepositoryProvider;
import com.yedc.android.utilities.ThemeUtils;
import com.yedc.android.widgets.QuestionWidget;
import com.yedc.android.widgets.items.SelectOneFromMapDialogFragment;
import com.yedc.async.Scheduler;
import com.yedc.async.network.NetworkStateProvider;
import com.yedc.draw.DrawActivity;
import com.yedc.googlemaps.GoogleMapFragment;
import com.yedc.location.LocationClient;
import com.yedc.maps.MapFragmentFactory;
import com.yedc.maps.layers.ReferenceLayerRepository;
import com.yedc.openrosa.http.OpenRosaHttpInterface;
import com.yedc.permissions.PermissionsChecker;
import com.yedc.permissions.PermissionsProvider;
import com.yedc.projects.ProjectsRepository;
import com.yedc.settings.ODKAppSettingsImporter;
import com.yedc.settings.SettingsProvider;
import com.yedc.webpage.ExternalWebPageHelper;

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
