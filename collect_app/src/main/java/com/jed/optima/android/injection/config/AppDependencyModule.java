package com.jed.optima.android.injection.config;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.jed.optima.androidshared.data.AppStateKt.getState;
import static com.jed.optima.settings.keys.MetaKeys.KEY_INSTALL_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import android.app.Application;
import android.content.Context;
import android.content.RestrictionsManager;
import android.webkit.MimeTypeMap;

import androidx.work.WorkManager;

import com.google.gson.Gson;

import org.javarosa.core.reference.ReferenceManager;
import org.json.JSONArray;
import org.json.JSONObject;
import com.jed.optima.analytics.Analytics;
import com.jed.optima.analytics.BlockableFirebaseAnalytics;
import com.jed.optima.analytics.NoopAnalytics;
import com.jed.optima.android.BuildConfig;
import com.jed.optima.android.R;
import com.jed.optima.android.application.CollectSettingsChangeHandler;
import com.jed.optima.android.application.MapboxClassInstanceCreator;
import com.jed.optima.android.application.initialization.AnalyticsInitializer;
import com.jed.optima.android.application.initialization.ApplicationInitializer;
import com.jed.optima.android.application.initialization.CachedFormsCleaner;
import com.jed.optima.android.application.initialization.ExistingProjectMigrator;
import com.jed.optima.android.application.initialization.ExistingSettingsMigrator;
import com.jed.optima.android.application.initialization.GoogleDriveProjectsDeleter;
import com.jed.optima.android.application.initialization.MapsInitializer;
import com.jed.optima.android.application.initialization.SavepointsImporter;
import com.jed.optima.android.application.initialization.ScheduledWorkUpgrade;
import com.jed.optima.android.application.initialization.upgrade.UpgradeInitializer;
import com.jed.optima.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler;
import com.jed.optima.android.backgroundwork.FormUpdateScheduler;
import com.jed.optima.android.backgroundwork.InstanceSubmitScheduler;
import com.jed.optima.android.configure.qr.AppConfigurationGenerator;
import com.jed.optima.android.configure.qr.CachingQRCodeGenerator;
import com.jed.optima.android.configure.qr.QRCodeGenerator;
import com.jed.optima.android.configure.qr.SettingsBarcodeScannerViewFactory;
import com.jed.optima.android.database.itemsets.DatabaseFastExternalItemsetsRepository;
import com.jed.optima.android.entities.EntitiesRepositoryProvider;
import com.jed.optima.android.external.InstancesContract;
import com.jed.optima.android.formentry.AppStateFormSessionRepository;
import com.jed.optima.android.formentry.FormSessionRepository;
import com.jed.optima.android.formlists.blankformlist.BlankFormListViewModel;
import com.jed.optima.android.formmanagement.CollectFormEntryControllerFactory;
import com.jed.optima.android.formmanagement.FormsDataService;
import com.jed.optima.android.formmanagement.OpenRosaClientProvider;
import com.jed.optima.android.formmanagement.ServerFormsDetailsFetcher;
import com.jed.optima.android.geo.MapConfiguratorProvider;
import com.jed.optima.android.geo.MapFragmentFactoryImpl;
import com.jed.optima.android.instancemanagement.InstancesDataService;
import com.jed.optima.android.instancemanagement.autosend.AutoSendSettingsProvider;
import com.jed.optima.android.instancemanagement.send.ReadyToSendViewModel;
import com.jed.optima.android.itemsets.FastExternalItemsetsRepository;
import com.jed.optima.android.mainmenu.MainMenuViewModelFactory;
import com.jed.optima.android.notifications.NotificationManagerNotifier;
import com.jed.optima.android.notifications.Notifier;
import com.jed.optima.android.preferences.Defaults;
import com.jed.optima.android.preferences.PreferenceVisibilityHandler;
import com.jed.optima.android.preferences.ProjectPreferencesViewModel;
import com.jed.optima.android.preferences.source.SharedPreferencesSettingsProvider;
import com.jed.optima.android.projects.ProjectCreatorImpl;
import com.jed.optima.android.projects.ProjectDeleter;
import com.jed.optima.android.projects.ProjectResetter;
import com.jed.optima.android.projects.ProjectsDataService;
import com.jed.optima.android.projects.SettingsConnectionMatcherImpl;
import com.jed.optima.android.storage.StoragePathProvider;
import com.jed.optima.android.storage.StorageSubdirectory;
import com.jed.optima.android.tasks.FormLoaderTask;
import com.jed.optima.android.utilities.AdminPasswordProvider;
import com.jed.optima.android.utilities.AndroidUserAgent;
import com.jed.optima.android.utilities.ChangeLockProvider;
import com.jed.optima.android.utilities.ContentUriProvider;
import com.jed.optima.android.utilities.ExternalAppIntentProvider;
import com.jed.optima.android.utilities.FileProvider;
import com.jed.optima.android.utilities.FormsRepositoryProvider;
import com.jed.optima.android.utilities.ImageCompressionController;
import com.jed.optima.android.utilities.InstancesRepositoryProvider;
import com.jed.optima.android.utilities.MediaUtils;
import com.jed.optima.android.utilities.SavepointsRepositoryProvider;
import com.jed.optima.android.utilities.SoftKeyboardController;
import com.jed.optima.android.utilities.WebCredentialsUtils;
import com.jed.optima.android.version.VersionInformation;
import com.jed.optima.androidshared.bitmap.ImageCompressor;
import com.jed.optima.androidshared.system.BroadcastReceiverRegister;
import com.jed.optima.androidshared.system.BroadcastReceiverRegisterImpl;
import com.jed.optima.androidshared.system.IntentLauncher;
import com.jed.optima.androidshared.system.IntentLauncherImpl;
import com.jed.optima.androidshared.utils.ScreenUtils;
import com.jed.optima.async.CoroutineAndWorkManagerScheduler;
import com.jed.optima.async.Scheduler;
import com.jed.optima.async.network.ConnectivityProvider;
import com.jed.optima.async.network.NetworkStateProvider;
import com.jed.optima.audiorecorder.recording.AudioRecorder;
import com.jed.optima.audiorecorder.recording.AudioRecorderFactory;
import com.jed.optima.entities.storage.EntitiesRepository;
import com.jed.optima.forms.FormsRepository;
import com.jed.optima.imageloader.GlideImageLoader;
import com.jed.optima.imageloader.ImageLoader;
import com.jed.optima.location.GoogleFusedLocationClient;
import com.jed.optima.location.LocationClient;
import com.jed.optima.location.LocationClientProvider;
import com.jed.optima.maps.MapFragmentFactory;
import com.jed.optima.maps.layers.DirectoryReferenceLayerRepository;
import com.jed.optima.maps.layers.ReferenceLayerRepository;
import com.jed.optima.metadata.InstallIDProvider;
import com.jed.optima.metadata.PropertyManager;
import com.jed.optima.metadata.SettingsInstallIDProvider;
import com.jed.optima.mobiledevicemanagement.MDMConfigHandler;
import com.jed.optima.mobiledevicemanagement.MDMConfigHandlerImpl;
import com.jed.optima.mobiledevicemanagement.MDMConfigObserver;
import com.jed.optima.openrosa.http.CollectThenSystemContentTypeMapper;
import com.jed.optima.openrosa.http.OpenRosaHttpInterface;
import com.jed.optima.openrosa.http.okhttp.OkHttpConnection;
import com.jed.optima.permissions.ContextCompatPermissionChecker;
import com.jed.optima.permissions.PermissionsChecker;
import com.jed.optima.permissions.PermissionsProvider;
import com.jed.optima.projects.Project;
import com.jed.optima.projects.ProjectCreator;
import com.jed.optima.projects.ProjectsRepository;
import com.jed.optima.projects.SettingsConnectionMatcher;
import com.jed.optima.projects.SharedPreferencesProjectsRepository;
import com.jed.optima.qrcode.BarcodeScannerViewContainer;
import com.jed.optima.qrcode.zxing.QRCodeCreatorImpl;
import com.jed.optima.qrcode.zxing.QRCodeDecoder;
import com.jed.optima.qrcode.zxing.QRCodeDecoderImpl;
import com.jed.optima.settings.ODKAppSettingsImporter;
import com.jed.optima.settings.ODKAppSettingsMigrator;
import com.jed.optima.settings.SettingsProvider;
import com.jed.optima.settings.importing.ProjectDetailsCreatorImpl;
import com.jed.optima.settings.importing.SettingsChangeHandler;
import com.jed.optima.settings.keys.AppConfigurationKeys;
import com.jed.optima.settings.keys.MetaKeys;
import com.jed.optima.settings.keys.ProjectKeys;
import com.jed.optima.shared.strings.UUIDGenerator;
import com.jed.optima.utilities.UserAgentProvider;
import com.jed.optima.webpage.ExternalWebPageHelper;

import java.io.File;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * Add dependency providers here (annotated with @Provides)
 * for objects you need to inject
 */
@Module
public class AppDependencyModule {

    @Provides
    Context context(Application application) {
        return application;
    }

    @Provides
    MimeTypeMap provideMimeTypeMap() {
        return MimeTypeMap.getSingleton();
    }

    @Provides
    @Singleton
    UserAgentProvider providesUserAgent() {
        return new AndroidUserAgent();
    }

    @Provides
    @Singleton
    public OpenRosaHttpInterface provideHttpInterface(MimeTypeMap mimeTypeMap, UserAgentProvider userAgentProvider, Application application, VersionInformation versionInformation) {
        String cacheDir = application.getCacheDir().getAbsolutePath();
        return new OkHttpConnection(
                cacheDir,
                new CollectThenSystemContentTypeMapper(mimeTypeMap),
                userAgentProvider.getUserAgent()
        );
    }

    @Provides
    WebCredentialsUtils provideWebCredentials(SettingsProvider settingsProvider) {
        return new WebCredentialsUtils(settingsProvider.getUnprotectedSettings());
    }

    @Provides
    @Singleton
    public Analytics providesAnalytics(Application application) {
        try {
            return new BlockableFirebaseAnalytics(application);
        } catch (IllegalStateException e) {
            // Couldn't setup Firebase so use no-op instance
            return new NoopAnalytics();
        }
    }

    @Provides
    public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
        return new PermissionsProvider(permissionsChecker);
    }

    @Provides
    public ReferenceManager providesReferenceManager() {
        return ReferenceManager.instance();
    }

    @Provides
    @Singleton
    public SettingsProvider providesSettingsProvider(Context context) {
        return new SharedPreferencesSettingsProvider(context);
    }


    @Provides
    public InstallIDProvider providesInstallIDProvider(SettingsProvider settingsProvider) {
        return new SettingsInstallIDProvider(settingsProvider.getMetaSettings(), KEY_INSTALL_ID);
    }

    @Provides
    public StoragePathProvider providesStoragePathProvider(Context context, ProjectsDataService projectsDataService, ProjectsRepository projectsRepository) {
        File externalFilesDir = context.getExternalFilesDir(null);

        if (externalFilesDir != null) {
            return new StoragePathProvider(projectsDataService, projectsRepository, externalFilesDir.getAbsolutePath());
        } else {
            throw new IllegalStateException("Storage is not available!");
        }
    }

    @Provides
    public AdminPasswordProvider providesAdminPasswordProvider(SettingsProvider settingsProvider) {
        return new AdminPasswordProvider(settingsProvider.getProtectedSettings());
    }

    @Provides
    public FormUpdateScheduler providesFormUpdateManger(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application);
    }

    @Provides
    public InstanceSubmitScheduler providesFormSubmitManager(Scheduler scheduler, SettingsProvider settingsProvider, Application application) {
        return new FormUpdateAndInstanceSubmitScheduler(scheduler, settingsProvider, application);
    }

    @Provides
    public NetworkStateProvider providesNetworkStateProvider(Context context) {
        return new ConnectivityProvider(context);
    }

    @Provides
    public QRCodeGenerator providesQRCodeGenerator() {
        return new CachingQRCodeGenerator(new QRCodeCreatorImpl());
    }

    @Provides
    public VersionInformation providesVersionInformation() {
        return new VersionInformation(() -> BuildConfig.VERSION_NAME);
    }

    @Provides
    public FileProvider providesFileProvider(Context context) {
        return filePath -> getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(filePath));
    }

    @Provides
    public WorkManager providesWorkManager(Context context) {
        return WorkManager.getInstance(context);
    }

    @Provides
    public Scheduler providesScheduler(WorkManager workManager) {
        return new CoroutineAndWorkManagerScheduler(workManager);
    }

    @Provides
    public ODKAppSettingsMigrator providesPreferenceMigrator(SettingsProvider settingsProvider) {
        return new ODKAppSettingsMigrator(settingsProvider.getMetaSettings());
    }

    @Provides
    @Singleton
    public PropertyManager providesPropertyManager(InstallIDProvider installIDProvider, SettingsProvider settingsProvider) {
        return new PropertyManager(installIDProvider, settingsProvider);
    }

    @Provides
    public SettingsChangeHandler providesSettingsChangeHandler(PropertyManager propertyManager, FormUpdateScheduler formUpdateScheduler, FormsDataService formsDataService) {
        return new CollectSettingsChangeHandler(propertyManager, formUpdateScheduler, formsDataService);
    }

    @Provides
    public ODKAppSettingsImporter providesODKAppSettingsImporter(Context context, ProjectsRepository projectsRepository, SettingsProvider settingsProvider, SettingsChangeHandler settingsChangeHandler) {
        JSONObject deviceUnsupportedSettings = new JSONObject();
        if (!MapboxClassInstanceCreator.isMapboxAvailable()) {
            try {
                deviceUnsupportedSettings.put(
                        AppConfigurationKeys.GENERAL,
                        new JSONObject().put(ProjectKeys.KEY_BASEMAP_SOURCE, new JSONArray(singletonList(ProjectKeys.BASEMAP_SOURCE_MAPBOX)))
                );
            } catch (Throwable ignored) {
                // ignore
            }
        }

        return new ODKAppSettingsImporter(
                projectsRepository,
                settingsProvider,
                Defaults.getUnprotected(),
                Defaults.getProtected(),
                asList(context.getResources().getStringArray(R.array.project_colors)),
                settingsChangeHandler,
                deviceUnsupportedSettings
        );
    }

    @Provides
    public QRCodeDecoder providesQRCodeDecoder() {
        return new QRCodeDecoderImpl();
    }

    @Provides
    public ServerFormsDetailsFetcher providesServerFormDetailsFetcher(FormsRepositoryProvider formsRepositoryProvider, OpenRosaClientProvider formSourceProvider, ProjectsDataService projectsDataService) {
        Project.Saved currentProject = projectsDataService.requireCurrentProject();
        FormsRepository formsRepository = formsRepositoryProvider.create(currentProject.getUuid());
        return new ServerFormsDetailsFetcher(formsRepository, formSourceProvider.create(currentProject.getUuid()));
    }

    @Provides
    public Notifier providesNotifier(Application application, SettingsProvider settingsProvider, ProjectsRepository projectsRepository) {
        return new NotificationManagerNotifier(application, settingsProvider, projectsRepository);
    }

    @Provides
    @Singleton
    public ChangeLockProvider providesChangeLockProvider() {
        return new ChangeLockProvider();
    }

    @Provides
    ScreenUtils providesScreenUtils(Context context) {
        return new ScreenUtils(context);
    }

    @Provides
    public AudioRecorder providesAudioRecorder(Application application) {
        return new AudioRecorderFactory(application).create();
    }

    @Provides
    public EntitiesRepositoryProvider provideEntitiesRepositoryProvider(Context context, StoragePathProvider storagePathProvider) {
        return new EntitiesRepositoryProvider(context, storagePathProvider);
    }

    @Provides
    public SoftKeyboardController provideSoftKeyboardController() {
        return SoftKeyboardController.INSTANCE;
    }

    @Provides
    public AppConfigurationGenerator providesJsonPreferencesGenerator(SettingsProvider settingsProvider, ProjectsDataService projectsDataService) {
        return new AppConfigurationGenerator(settingsProvider, projectsDataService);
    }

    @Provides
    @Singleton
    public PermissionsChecker providesPermissionsChecker(Context context) {
        return new ContextCompatPermissionChecker(context);
    }

    @Provides
    @Singleton
    public ExternalAppIntentProvider providesExternalAppIntentProvider() {
        return new ExternalAppIntentProvider();
    }

    @Provides
    public FormSessionRepository providesFormSessionStore(Application application) {
        return new AppStateFormSessionRepository(application);
    }

    @Provides
    public ExternalWebPageHelper providesExternalWebPageHelper() {
        return new ExternalWebPageHelper();
    }

    @Provides
    @Singleton
    public ProjectsRepository providesProjectsRepository(UUIDGenerator uuidGenerator, Gson gson, SettingsProvider settingsProvider) {
        return new SharedPreferencesProjectsRepository(uuidGenerator, gson, settingsProvider.getMetaSettings(), MetaKeys.KEY_PROJECTS);
    }

    @Provides
    public ProjectCreator providesProjectCreator(ProjectsRepository projectsRepository, ProjectsDataService projectsDataService,
                                                 ODKAppSettingsImporter settingsImporter, SettingsProvider settingsProvider) {
        return new ProjectCreatorImpl(projectsRepository, projectsDataService, settingsImporter, settingsProvider);
    }

    @Provides
    public Gson providesGson() {
        return new Gson();
    }

    @Provides
    @Singleton
    public UUIDGenerator providesUUIDGenerator() {
        return new UUIDGenerator();
    }

    @Provides
    public InstancesDataService providesInstancesDataService(Application application, ProjectsDataService projectsDataService, InstanceSubmitScheduler instanceSubmitScheduler, ProjectDependencyModuleFactory projectsDependencyProviderFactory, Notifier notifier, PropertyManager propertyManager, OpenRosaHttpInterface httpInterface) {
        Function0<Unit> onUpdate = () -> {
            application.getContentResolver().notifyChange(
                    InstancesContract.getUri(projectsDataService.requireCurrentProject().getUuid()),
                    null
            );

            return null;
        };

        return new InstancesDataService(getState(application), instanceSubmitScheduler, projectsDependencyProviderFactory, notifier, propertyManager, httpInterface, onUpdate);
    }

    @Provides
    public FastExternalItemsetsRepository providesItemsetsRepository() {
        return new DatabaseFastExternalItemsetsRepository();
    }

    @Provides
    public ProjectsDataService providesCurrentProjectProvider(Application application, SettingsProvider settingsProvider, ProjectsRepository projectsRepository, AnalyticsInitializer analyticsInitializer, Context context, MapsInitializer mapsInitializer) {
        return new ProjectsDataService(getState(application), settingsProvider, projectsRepository, analyticsInitializer, mapsInitializer);
    }

    @Provides
    public FormsRepositoryProvider providesFormsRepositoryProvider(Application application) {
        return new FormsRepositoryProvider(application);
    }

    @Provides
    public InstancesRepositoryProvider providesInstancesRepositoryProvider(Context context, StoragePathProvider storagePathProvider) {
        return new InstancesRepositoryProvider(context, storagePathProvider);
    }

    @Provides
    public SavepointsRepositoryProvider providesSavepointsRepositoryProvider(Context context, StoragePathProvider storagePathProvider) {
        return new SavepointsRepositoryProvider(context, storagePathProvider);
    }

    @Provides
    public ProjectPreferencesViewModel.Factory providesProjectPreferencesViewModel(AdminPasswordProvider adminPasswordProvider) {
        return new ProjectPreferencesViewModel.Factory(adminPasswordProvider);
    }

    @Provides
    public ReadyToSendViewModel.Factory providesReadyToSendViewModel(InstancesRepositoryProvider instancesRepositoryProvider, Scheduler scheduler) {
        return new ReadyToSendViewModel.Factory(instancesRepositoryProvider.create(), scheduler, System::currentTimeMillis);
    }

    @Provides
    public MainMenuViewModelFactory providesMainMenuViewModelFactory(VersionInformation versionInformation, Application application,
                                                                     SettingsProvider settingsProvider, InstancesDataService instancesDataService,
                                                                     Scheduler scheduler, ProjectsDataService projectsDataService,
                                                                     AnalyticsInitializer analyticsInitializer, PermissionsChecker permissionChecker,
                                                                     FormsRepositoryProvider formsRepositoryProvider, InstancesRepositoryProvider instancesRepositoryProvider,
                                                                     AutoSendSettingsProvider autoSendSettingsProvider) {
        return new MainMenuViewModelFactory(versionInformation, application, settingsProvider, instancesDataService, scheduler, projectsDataService, permissionChecker, formsRepositoryProvider, instancesRepositoryProvider, autoSendSettingsProvider);
    }

    @Provides
    public AnalyticsInitializer providesAnalyticsInitializer(Analytics analytics, VersionInformation versionInformation, SettingsProvider settingsProvider) {
        return new AnalyticsInitializer(analytics, versionInformation, settingsProvider);
    }

    @Provides
    public OpenRosaClientProvider providesFormSourceProvider(SettingsProvider settingsProvider, OpenRosaHttpInterface openRosaHttpInterface) {
        return new OpenRosaClientProvider(settingsProvider::getUnprotectedSettings, openRosaHttpInterface);
    }

    @Provides
    public FormsDataService providesFormsUpdater(Application application, Notifier notifier, ProjectDependencyModuleFactory projectDependencyModuleFactory) {
        return new FormsDataService(getState(application), notifier, projectDependencyModuleFactory, System::currentTimeMillis);
    }

    @Provides
    public AutoSendSettingsProvider providesAutoSendSettingsProvider(Application application, SettingsProvider settingsProvider, NetworkStateProvider networkStateProvider) {
        return new AutoSendSettingsProvider(application, networkStateProvider, settingsProvider);
    }

    @Provides
    public ExistingProjectMigrator providesExistingProjectMigrator(Context context, StoragePathProvider storagePathProvider, ProjectsRepository projectsRepository, SettingsProvider settingsProvider, ProjectsDataService projectsDataService) {
        return new ExistingProjectMigrator(context, storagePathProvider, projectsRepository, settingsProvider, projectsDataService, new ProjectDetailsCreatorImpl(asList(context.getResources().getStringArray(R.array.project_colors)), Defaults.getUnprotected()));
    }

    @Provides
    public ScheduledWorkUpgrade providesFormUpdatesUpgrader(Scheduler scheduler, ProjectsRepository projectsRepository, FormUpdateScheduler formUpdateScheduler, InstanceSubmitScheduler instanceSubmitScheduler) {
        return new ScheduledWorkUpgrade(scheduler, projectsRepository, formUpdateScheduler, instanceSubmitScheduler);
    }

    @Provides
    public ExistingSettingsMigrator providesExistingSettingsMigrator(ProjectsRepository projectsRepository, SettingsProvider settingsProvider, ODKAppSettingsMigrator settingsMigrator) {
        return new ExistingSettingsMigrator(projectsRepository, settingsProvider, settingsMigrator);
    }

    @Provides
    public GoogleDriveProjectsDeleter providesGoogleDriveProjectsDeleter(ProjectsRepository projectsRepository, SettingsProvider settingsProvider, ProjectDeleter projectDeleter) {
        return new GoogleDriveProjectsDeleter(projectsRepository, settingsProvider, projectDeleter);
    }

    @Provides
    public UpgradeInitializer providesUpgradeInitializer(Context context, SettingsProvider settingsProvider, ExistingProjectMigrator existingProjectMigrator, ExistingSettingsMigrator existingSettingsMigrator, ScheduledWorkUpgrade scheduledWorkUpgrade, GoogleDriveProjectsDeleter googleDriveProjectsDeleter, ProjectsRepository projectsRepository, ProjectDependencyModuleFactory projectDependencyModuleFactory) {
        return new UpgradeInitializer(
                context,
                settingsProvider,
                existingProjectMigrator,
                existingSettingsMigrator,
                scheduledWorkUpgrade,
                googleDriveProjectsDeleter,
                new SavepointsImporter(projectsRepository, projectDependencyModuleFactory),
                new CachedFormsCleaner(projectsRepository, projectDependencyModuleFactory)
        );
    }

    @Provides
    public ApplicationInitializer providesApplicationInitializer(Application context, PropertyManager propertyManager, Analytics analytics, UpgradeInitializer upgradeInitializer, AnalyticsInitializer analyticsInitializer, ProjectsRepository projectsRepository, SettingsProvider settingsProvider, MapsInitializer mapsInitializer, EntitiesRepositoryProvider entitiesRepositoryProvider, ProjectsDataService projectsDataService) {
        return new ApplicationInitializer(context, propertyManager, analytics, upgradeInitializer, analyticsInitializer, mapsInitializer, projectsRepository, settingsProvider, entitiesRepositoryProvider, projectsDataService);
    }

    @Provides
    public ProjectDeleter providesProjectDeleter(ProjectsRepository projectsRepository, ProjectsDataService projectsDataService, FormUpdateScheduler formUpdateScheduler, InstanceSubmitScheduler instanceSubmitScheduler, InstancesRepositoryProvider instancesRepositoryProvider, StoragePathProvider storagePathProvider, ChangeLockProvider changeLockProvider, SettingsProvider settingsProvider) {
        return new ProjectDeleter(projectsRepository, projectsDataService, formUpdateScheduler, instanceSubmitScheduler, instancesRepositoryProvider, storagePathProvider, changeLockProvider, settingsProvider);
    }

    @Provides
    public ProjectResetter providesProjectResetter(StoragePathProvider storagePathProvider, PropertyManager propertyManager, SettingsProvider settingsProvider, FormsRepositoryProvider formsRepositoryProvider, SavepointsRepositoryProvider savepointsRepositoryProvider, InstancesDataService instancesDataService, ProjectsDataService projectsDataService) {
        return new ProjectResetter(storagePathProvider, propertyManager, settingsProvider, formsRepositoryProvider, savepointsRepositoryProvider, instancesDataService, projectsDataService.requireCurrentProject().getUuid());
    }

    @Provides
    public PreferenceVisibilityHandler providesDisabledPreferencesRemover(SettingsProvider settingsProvider, VersionInformation versionInformation) {
        return new PreferenceVisibilityHandler(settingsProvider, versionInformation);
    }

    @Provides
    public ReferenceLayerRepository providesReferenceLayerRepository(StoragePathProvider storagePathProvider, SettingsProvider settingsProvider) {
        return new DirectoryReferenceLayerRepository(
                storagePathProvider.getOdkDirPath(StorageSubdirectory.SHARED_LAYERS),
                storagePathProvider.getOdkDirPath(StorageSubdirectory.LAYERS),
                () -> MapConfiguratorProvider.getConfigurator(
                        settingsProvider.getUnprotectedSettings().getString(ProjectKeys.KEY_BASEMAP_SOURCE)
                )
        );
    }

    @Provides
    public IntentLauncher providesIntentLauncher() {
        return IntentLauncherImpl.INSTANCE;
    }

    @Provides
    public LocationClient providesLocationClient(Application application) {
        return LocationClientProvider.getClient(application);
    }

    @Provides
    @Named("fused")
    public LocationClient providesFusedLocationClient(Application application) {
        return new GoogleFusedLocationClient(application);
    }

    @Provides
    public MediaUtils providesMediaUtils(IntentLauncher intentLauncher) {
        return new MediaUtils(intentLauncher, new ContentUriProvider());
    }

    @Provides
    public MapFragmentFactory providesMapFragmentFactory(SettingsProvider settingsProvider) {
        return new MapFragmentFactoryImpl(settingsProvider);
    }

    @Provides
    public ImageLoader providesImageLoader() {
        return new GlideImageLoader();
    }

    @Provides
    public BlankFormListViewModel.Factory providesBlankFormListViewModel(FormsRepositoryProvider formsRepositoryProvider, InstancesRepositoryProvider instancesRepositoryProvider, Application application, FormsDataService formsDataService, Scheduler scheduler, SettingsProvider settingsProvider, ChangeLockProvider changeLockProvider, ProjectsDataService projectsDataService) {
        return new BlankFormListViewModel.Factory(instancesRepositoryProvider.create(), application, formsDataService, scheduler, settingsProvider.getUnprotectedSettings(), projectsDataService.requireCurrentProject().getUuid());
    }

    @Provides
    @Singleton
    public ImageCompressionController providesImageCompressorManager() {
        return new ImageCompressionController(ImageCompressor.INSTANCE);
    }

    @Provides
    public FormLoaderTask.FormEntryControllerFactory formEntryControllerFactory(ProjectsDataService projectsDataService, EntitiesRepositoryProvider entitiesRepositoryProvider, SettingsProvider settingsProvider) {
        String projectId = projectsDataService.requireCurrentProject().getUuid();
        EntitiesRepository entitiesRepository = entitiesRepositoryProvider.create(projectId);
        return new CollectFormEntryControllerFactory(entitiesRepository, settingsProvider.getUnprotectedSettings(projectId));
    }

    @Provides
    public BroadcastReceiverRegister providesBroadcastReceiverRegister(Context context) {
        return new BroadcastReceiverRegisterImpl(context);
    }

    @Provides
    public RestrictionsManager providesRestrictionsManager(Context context) {
        return (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
    }

    @Provides
    public MDMConfigObserver providesMDMConfigObserver(
            Scheduler scheduler,
            SettingsProvider settingsProvider,
            ProjectsRepository projectsRepository,
            ProjectCreator projectCreator,
            ODKAppSettingsImporter settingsImporter,
            BroadcastReceiverRegister broadcastReceiverRegister,
            RestrictionsManager restrictionsManager
    ) {
        SettingsConnectionMatcher settingsConnectionMatcher = new SettingsConnectionMatcherImpl(
                projectsRepository,
                settingsProvider
        );

        MDMConfigHandler mdmConfigHandler = new MDMConfigHandlerImpl(
                settingsProvider,
                projectsRepository,
                projectCreator,
                settingsImporter,
                settingsConnectionMatcher
        );

        return new MDMConfigObserver(
                scheduler,
                mdmConfigHandler,
                broadcastReceiverRegister,
                restrictionsManager
        );
    }

    @Provides
    public BarcodeScannerViewContainer.Factory providesBarcodeScannerViewFactory(SettingsProvider settingsProvider) {
        return new SettingsBarcodeScannerViewFactory(settingsProvider.getUnprotectedSettings());
    }
}
