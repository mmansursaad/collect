package com.yedc.android.injection.config;

import static androidx.core.content.FileProvider.getUriForFile;
import static com.yedc.androidshared.data.AppStateKt.getState;
import static com.yedc.settings.keys.MetaKeys.KEY_INSTALL_ID;
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
import com.yedc.analytics.Analytics;
import com.yedc.analytics.BlockableFirebaseAnalytics;
import com.yedc.analytics.NoopAnalytics;
import com.yedc.android.BuildConfig;
import com.yedc.android.R;
import com.yedc.android.application.CollectSettingsChangeHandler;
import com.yedc.android.application.MapboxClassInstanceCreator;
import com.yedc.android.application.initialization.AnalyticsInitializer;
import com.yedc.android.application.initialization.ApplicationInitializer;
import com.yedc.android.application.initialization.CachedFormsCleaner;
import com.yedc.android.application.initialization.ExistingProjectMigrator;
import com.yedc.android.application.initialization.ExistingSettingsMigrator;
import com.yedc.android.application.initialization.GoogleDriveProjectsDeleter;
import com.yedc.android.application.initialization.MapsInitializer;
import com.yedc.android.application.initialization.SavepointsImporter;
import com.yedc.android.application.initialization.ScheduledWorkUpgrade;
import com.yedc.android.application.initialization.upgrade.UpgradeInitializer;
import com.yedc.android.backgroundwork.FormUpdateAndInstanceSubmitScheduler;
import com.yedc.android.backgroundwork.FormUpdateScheduler;
import com.yedc.android.backgroundwork.InstanceSubmitScheduler;
import com.yedc.android.configure.qr.AppConfigurationGenerator;
import com.yedc.android.configure.qr.CachingQRCodeGenerator;
import com.yedc.android.configure.qr.QRCodeGenerator;
import com.yedc.android.configure.qr.SettingsBarcodeScannerViewFactory;
import com.yedc.android.database.itemsets.DatabaseFastExternalItemsetsRepository;
import com.yedc.android.entities.EntitiesRepositoryProvider;
import com.yedc.android.external.InstancesContract;
import com.yedc.android.formentry.AppStateFormSessionRepository;
import com.yedc.android.formentry.FormSessionRepository;
import com.yedc.android.formlists.blankformlist.BlankFormListViewModel;
import com.yedc.android.formmanagement.CollectFormEntryControllerFactory;
import com.yedc.android.formmanagement.FormsDataService;
import com.yedc.android.formmanagement.OpenRosaClientProvider;
import com.yedc.android.formmanagement.ServerFormsDetailsFetcher;
import com.yedc.android.geo.MapConfiguratorProvider;
import com.yedc.android.geo.MapFragmentFactoryImpl;
import com.yedc.android.instancemanagement.InstancesDataService;
import com.yedc.android.instancemanagement.autosend.AutoSendSettingsProvider;
import com.yedc.android.instancemanagement.send.ReadyToSendViewModel;
import com.yedc.android.itemsets.FastExternalItemsetsRepository;
import com.yedc.android.mainmenu.MainMenuViewModelFactory;
import com.yedc.android.notifications.NotificationManagerNotifier;
import com.yedc.android.notifications.Notifier;
import com.yedc.android.preferences.Defaults;
import com.yedc.android.preferences.PreferenceVisibilityHandler;
import com.yedc.android.preferences.ProjectPreferencesViewModel;
import com.yedc.android.preferences.source.SharedPreferencesSettingsProvider;
import com.yedc.android.projects.ProjectCreatorImpl;
import com.yedc.android.projects.ProjectDeleter;
import com.yedc.android.projects.ProjectResetter;
import com.yedc.android.projects.ProjectsDataService;
import com.yedc.android.projects.SettingsConnectionMatcherImpl;
import com.yedc.android.storage.StoragePathProvider;
import com.yedc.android.storage.StorageSubdirectory;
import com.yedc.android.tasks.FormLoaderTask;
import com.yedc.android.utilities.AdminPasswordProvider;
import com.yedc.android.utilities.AndroidUserAgent;
import com.yedc.android.utilities.ChangeLockProvider;
import com.yedc.android.utilities.ContentUriProvider;
import com.yedc.android.utilities.ExternalAppIntentProvider;
import com.yedc.android.utilities.FileProvider;
import com.yedc.android.utilities.FormsRepositoryProvider;
import com.yedc.android.utilities.ImageCompressionController;
import com.yedc.android.utilities.InstancesRepositoryProvider;
import com.yedc.android.utilities.MediaUtils;
import com.yedc.android.utilities.SavepointsRepositoryProvider;
import com.yedc.android.utilities.SoftKeyboardController;
import com.yedc.android.utilities.WebCredentialsUtils;
import com.yedc.android.version.VersionInformation;
import com.yedc.androidshared.bitmap.ImageCompressor;
import com.yedc.androidshared.system.BroadcastReceiverRegister;
import com.yedc.androidshared.system.BroadcastReceiverRegisterImpl;
import com.yedc.androidshared.system.IntentLauncher;
import com.yedc.androidshared.system.IntentLauncherImpl;
import com.yedc.androidshared.utils.ScreenUtils;
import com.yedc.async.CoroutineAndWorkManagerScheduler;
import com.yedc.async.Scheduler;
import com.yedc.async.network.ConnectivityProvider;
import com.yedc.async.network.NetworkStateProvider;
import com.yedc.audiorecorder.recording.AudioRecorder;
import com.yedc.audiorecorder.recording.AudioRecorderFactory;
import com.yedc.entities.storage.EntitiesRepository;
import com.yedc.forms.FormsRepository;
import com.yedc.imageloader.GlideImageLoader;
import com.yedc.imageloader.ImageLoader;
import com.yedc.location.GoogleFusedLocationClient;
import com.yedc.location.LocationClient;
import com.yedc.location.LocationClientProvider;
import com.yedc.maps.MapFragmentFactory;
import com.yedc.maps.layers.DirectoryReferenceLayerRepository;
import com.yedc.maps.layers.ReferenceLayerRepository;
import com.yedc.metadata.InstallIDProvider;
import com.yedc.metadata.PropertyManager;
import com.yedc.metadata.SettingsInstallIDProvider;
import com.yedc.mobiledevicemanagement.MDMConfigHandler;
import com.yedc.mobiledevicemanagement.MDMConfigHandlerImpl;
import com.yedc.mobiledevicemanagement.MDMConfigObserver;
import com.yedc.openrosa.http.CollectThenSystemContentTypeMapper;
import com.yedc.openrosa.http.OpenRosaHttpInterface;
import com.yedc.openrosa.http.okhttp.OkHttpConnection;
import com.yedc.permissions.ContextCompatPermissionChecker;
import com.yedc.permissions.PermissionsChecker;
import com.yedc.permissions.PermissionsProvider;
import com.yedc.projects.Project;
import com.yedc.projects.ProjectCreator;
import com.yedc.projects.ProjectsRepository;
import com.yedc.projects.SettingsConnectionMatcher;
import com.yedc.projects.SharedPreferencesProjectsRepository;
import com.yedc.qrcode.BarcodeScannerViewContainer;
import com.yedc.qrcode.zxing.QRCodeCreatorImpl;
import com.yedc.qrcode.zxing.QRCodeDecoder;
import com.yedc.qrcode.zxing.QRCodeDecoderImpl;
import com.yedc.settings.ODKAppSettingsImporter;
import com.yedc.settings.ODKAppSettingsMigrator;
import com.yedc.settings.SettingsProvider;
import com.yedc.settings.importing.ProjectDetailsCreatorImpl;
import com.yedc.settings.importing.SettingsChangeHandler;
import com.yedc.settings.keys.AppConfigurationKeys;
import com.yedc.settings.keys.MetaKeys;
import com.yedc.settings.keys.ProjectKeys;
import com.yedc.shared.strings.UUIDGenerator;
import com.yedc.utilities.UserAgentProvider;
import com.yedc.webpage.ExternalWebPageHelper;

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
