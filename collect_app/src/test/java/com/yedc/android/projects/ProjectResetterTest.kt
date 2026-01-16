package com.yedc.android.projects

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import com.yedc.android.injection.DaggerUtils
import com.yedc.android.preferences.Defaults
import com.yedc.android.storage.StoragePathProvider
import com.yedc.android.utilities.ChangeLockProvider
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.android.utilities.SavepointsRepositoryProvider
import com.yedc.forms.savepoints.Savepoint
import com.yedc.metadata.InstallIDProvider
import com.yedc.metadata.PropertyManager
import com.yedc.projects.Project
import com.yedc.settings.SettingsProvider
import com.yedc.settings.keys.ProjectKeys
import com.yedc.settings.keys.ProtectedProjectKeys
import com.yedc.shared.locks.BooleanChangeLock
import com.yedc.shared.settings.Settings
import java.io.File

@RunWith(AndroidJUnit4::class)
class ProjectResetterTest {
    private lateinit var projectResetter: ProjectResetter
    private lateinit var storagePathProvider: StoragePathProvider
    private lateinit var settingsProvider: SettingsProvider
    private lateinit var formsRepositoryProvider: FormsRepositoryProvider
    private lateinit var instancesRepositoryProvider: InstancesRepositoryProvider
    private lateinit var savepointsRepositoryProvider: SavepointsRepositoryProvider
    private lateinit var currentProjectId: String
    private lateinit var anotherProjectId: String

    private val propertyManager = mock<PropertyManager>()
    private val changeLockProvider = ChangeLockProvider { BooleanChangeLock() }

    @Before
    fun setup() {
        com.yedc.android.support.CollectHelpers.overrideAppDependencyModule(object : _root_ide_package_.com.yedc.android.injection.config.AppDependencyModule() {
            override fun providesPropertyManager(
                installIDProvider: InstallIDProvider,
                settingsProvider: SettingsProvider?
            ): PropertyManager {
                return propertyManager
            }

            override fun providesChangeLockProvider(): ChangeLockProvider {
                return changeLockProvider
            }
        })

        currentProjectId = com.yedc.android.support.CollectHelpers.setupDemoProject()
        anotherProjectId = com.yedc.android.support.CollectHelpers.createProject(Project.New("Another project", "A", "#cccccc"))

        val component = DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>() as Application)
        projectResetter = component.projectResetter()
        storagePathProvider = component.storagePathProvider()
        settingsProvider = component.settingsProvider()
        formsRepositoryProvider = component.formsRepositoryProvider()
        instancesRepositoryProvider = component.instancesRepositoryProvider()
        savepointsRepositoryProvider = component.savepointsRepositoryProvider()
    }

    @Test
    fun `Reset settings clears unprotected settings for current project`() {
        setupTestGeneralSettings(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getUnprotectedSettings(currentProjectId).getString(ProjectKeys.KEY_USERNAME),
            `is`(
                Defaults.unprotected[ProjectKeys.KEY_USERNAME]
            )
        )
        assertThat(
            getUnprotectedSettings(currentProjectId).getString(ProjectKeys.KEY_PASSWORD),
            `is`(
                Defaults.unprotected[ProjectKeys.KEY_PASSWORD]
            )
        )
    }

    @Test
    fun `Reset settings does not clear unprotected settings for another projects`() {
        setupTestGeneralSettings(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getUnprotectedSettings(anotherProjectId).getString(ProjectKeys.KEY_USERNAME),
            `is`(
                "usernameTest"
            )
        )
        assertThat(
            getUnprotectedSettings(anotherProjectId).getString(ProjectKeys.KEY_PASSWORD),
            `is`(
                "passwordTest"
            )
        )
    }

    @Test
    fun `Reset settings clears protected settings for current project`() {
        setupTestAdminSettings(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getProtectedSettings(currentProjectId).getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT),
            `is`(
                Defaults.protected[ProtectedProjectKeys.KEY_VIEW_SENT]
            )
        )
    }

    @Test
    fun `Reset settings does not clear protected settings for another projects`() {
        setupTestAdminSettings(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertThat(
            getProtectedSettings(anotherProjectId).getBoolean(ProtectedProjectKeys.KEY_VIEW_SENT),
            `is`(
                false
            )
        )
    }

    @Test
    fun `Reset settings clears settings folder for current project`() {
        setupTestSettingsFolder(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS, currentProjectId))
    }

    @Test
    fun `Reset settings does not clear settings folder for another projects`() {
        setupTestSettingsFolder(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS, anotherProjectId), "settings.png").exists())
    }

    @Test
    fun `Reset settings reloads property manager`() {
        resetAppState(listOf(ProjectResetter.ResetAction.RESET_PREFERENCES))

        verify(propertyManager).reload()
    }

    @Test
    fun `Reset forms clears forms for current project`() {
        saveTestFormFiles(currentProjectId)
        setupTestFormsDatabase(currentProjectId)
        createTestItemsetsDatabaseFile(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_FORMS))

        assertEquals(0, formsRepositoryProvider.create(currentProjectId).all.size)
        assertFolderEmpty(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, currentProjectId))
        assertFalse(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.METADATA, currentProjectId) + "/itemsets.db").exists())
    }

    @Test
    fun `Reset forms does not clear forms for another projects`() {
        saveTestFormFiles(anotherProjectId)
        setupTestFormsDatabase(anotherProjectId)
        createTestItemsetsDatabaseFile(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_FORMS))

        assertEquals(1, formsRepositoryProvider.create(anotherProjectId).all.size)
        assertTestFormFiles(anotherProjectId)
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.METADATA, anotherProjectId) + "/itemsets.db").exists())
    }

    @Test
    fun `Reset instances does not clear instances and savepoints if the instances database is locked`() {
        saveTestInstanceFiles(currentProjectId)
        setupTestInstancesDatabase(currentProjectId)
        setupTestSavepointsDatabase(currentProjectId)

        (changeLockProvider.create(currentProjectId).instancesLock as BooleanChangeLock).lock("blah")
        val failedResetActions = projectResetter.reset(listOf(ProjectResetter.ResetAction.RESET_INSTANCES))
        assertEquals(1, failedResetActions.size)

        assertEquals(1, instancesRepositoryProvider.create(currentProjectId).all.size)
        assertTestInstanceFiles(currentProjectId)
        assertEquals(1, savepointsRepositoryProvider.create(currentProjectId).getAll().size)
    }

    @Test
    fun `Reset instances clears instances and savepoints for current project`() {
        saveTestInstanceFiles(currentProjectId)
        setupTestInstancesDatabase(currentProjectId)
        setupTestSavepointsDatabase(anotherProjectId)
        val instancesRepository = instancesRepositoryProvider.create(currentProjectId)
        val instance = instancesRepository.all[0]

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_INSTANCES))

        assertEquals(0, instancesRepository.all.size)
        assertEquals(false, File(instance.instanceFilePath).parentFile.exists())
        assertEquals(1, savepointsRepositoryProvider.create(anotherProjectId).getAll().size)
    }

    @Test
    fun `Reset instances does not clear instances and savepoints for another projects`() {
        saveTestInstanceFiles(anotherProjectId)
        setupTestInstancesDatabase(anotherProjectId)
        setupTestSavepointsDatabase(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_INSTANCES))

        assertEquals(1, instancesRepositoryProvider.create(anotherProjectId).all.size)
        assertTestInstanceFiles(anotherProjectId)
        assertEquals(1, savepointsRepositoryProvider.create(anotherProjectId).getAll().size)
    }

    @Test
    fun `Reset layers clears layers for current project`() {
        saveTestLayerFiles(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_LAYERS))

        assertFolderEmpty(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, currentProjectId))
    }

    @Test
    fun `Reset layers does not clear layers for another projects`() {
        saveTestLayerFiles(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_LAYERS))

        assertTestLayerFiles(anotherProjectId)
    }

    @Test
    fun `Reset cache clears cache and savepoints db for current project`() {
        setupTestSavepointsDatabase(currentProjectId)
        saveTestCacheFiles(currentProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_CACHE))

        assertEquals(0, savepointsRepositoryProvider.create(currentProjectId).getAll().size)
        assertFolderEmpty(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, currentProjectId))
    }

    @Test
    fun `Reset cache does not clear cache and savepoints db for another projects`() {
        setupTestSavepointsDatabase(anotherProjectId)
        saveTestCacheFiles(anotherProjectId)

        resetAppState(listOf(ProjectResetter.ResetAction.RESET_CACHE))

        assertEquals(1, savepointsRepositoryProvider.create(anotherProjectId).getAll().size)
        assertTestCacheFiles(anotherProjectId)
    }

    private fun resetAppState(resetActions: List<Int>) {
        val failedResetActions = projectResetter.reset(resetActions)
        assertEquals(0, failedResetActions.size)
    }

    private fun setupTestGeneralSettings(uuid: String) {
        getUnprotectedSettings(uuid).save(ProjectKeys.KEY_USERNAME, "usernameTest")
        getUnprotectedSettings(uuid).save(ProjectKeys.KEY_PASSWORD, "passwordTest")
    }

    private fun setupTestAdminSettings(uuid: String) {
        getProtectedSettings(uuid).save(ProtectedProjectKeys.KEY_VIEW_SENT, false)
    }

    private fun setupTestSettingsFolder(uuid: String) {
        assertTrue(
            File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS, uuid)).exists() || File(
                storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS, uuid)
            ).mkdir()
        )

        File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.SETTINGS, uuid), "settings.png").createNewFile()
    }

    private fun setupTestFormsDatabase(uuid: String) {
        FormsRepositoryProvider(ApplicationProvider.getApplicationContext()).create(uuid).save(
            com.yedc.forms.Form.Builder()
                .formId("jrFormId")
                .displayName("displayName")
                .formFilePath(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile1.xml")
                .build()
        )
        assertEquals(1, formsRepositoryProvider.create(uuid).all.size)
    }

    private fun setupTestInstancesDatabase(uuid: String) {
        InstancesRepositoryProvider(ApplicationProvider.getApplicationContext()).create(uuid).save(
            com.yedc.forms.instances.Instance.Builder()
                .instanceFilePath("testDir1/testFile1")
                .submissionUri("submissionUri")
                .displayName("formName")
                .formId("jrformid")
                .formVersion("jrversion")
                .build()
        )
        assertEquals(1, instancesRepositoryProvider.create(uuid).all.size)
    }

    private fun setupTestSavepointsDatabase(uuid: String) {
        SavepointsRepositoryProvider(ApplicationProvider.getApplicationContext(), storagePathProvider).create(uuid).save(
            Savepoint(1, 1, "blah", "blah")
        )
        assertEquals(1, savepointsRepositoryProvider.create(uuid).getAll().size)
    }

    private fun createTestItemsetsDatabaseFile(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.METADATA, uuid) + "/itemsets.db").createNewFile())
    }

    private fun saveTestFormFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile1.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile2.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile3.xml").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testDir1/testFile1-media").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testDir2/testFile2-media").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testDir3/testFile3-media/testFile.csv").mkdirs())
    }

    private fun assertTestFormFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile1.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile2.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testFile3.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testDir1/testFile1-media").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testDir2/testFile2-media").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.FORMS, uuid) + "/testDir3/testFile3-media/testFile.csv").exists())
    }

    private fun saveTestInstanceFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, uuid) + "/testDir1/testFile1.xml").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, uuid) + "/testDir2/testFile2.xml").mkdirs())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, uuid) + "/testDir3").mkdirs())
    }

    private fun assertTestInstanceFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, uuid) + "/testDir1/testFile1.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, uuid) + "/testDir2/testFile2.xml").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.INSTANCES, uuid) + "/testDir3").exists())
    }

    private fun saveTestLayerFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile1").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile2").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile3").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile4").createNewFile())
    }

    private fun assertTestLayerFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile1").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile2").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile3").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.LAYERS, uuid) + "/testFile4").exists())
    }

    private fun saveTestCacheFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, uuid) + "/testFile1").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, uuid) + "/testFile2").createNewFile())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, uuid) + "/testFile3").createNewFile())
    }

    private fun assertTestCacheFiles(uuid: String) {
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, uuid) + "/testFile1").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, uuid) + "/testFile2").exists())
        assertTrue(File(storagePathProvider.getOdkDirPath(_root_ide_package_.com.yedc.android.storage.StorageSubdirectory.CACHE, uuid) + "/testFile3").exists())
    }

    private fun assertFolderEmpty(folder: String) {
        assertTrue(File(folder).isDirectory)
        assertTrue(File(folder).list().isEmpty())
    }

    fun getUnprotectedSettings(uuid: String): Settings {
        return settingsProvider.getUnprotectedSettings(uuid)
    }

    fun getProtectedSettings(uuid: String): Settings {
        return settingsProvider.getProtectedSettings(uuid)
    }
}
