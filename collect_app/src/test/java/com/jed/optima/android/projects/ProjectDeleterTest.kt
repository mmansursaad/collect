package com.jed.optima.android.projects

import androidx.test.espresso.matcher.ViewMatchers.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.instanceOf
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import com.jed.optima.android.preferences.Defaults
import com.jed.optima.android.storage.StoragePathProvider
import com.jed.optima.android.utilities.ChangeLockProvider
import com.jed.optima.android.utilities.InstancesRepositoryProvider
import com.jed.optima.androidshared.data.AppState
import com.jed.optima.projects.InMemProjectsRepository
import com.jed.optima.projects.Project
import com.jed.optima.settings.InMemSettingsProvider
import com.jed.optima.settings.keys.MetaKeys
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.settings.keys.ProtectedProjectKeys
import com.jed.optima.shared.TempFiles
import com.jed.optima.shared.locks.BooleanChangeLock
import java.io.File

class ProjectDeleterTest {
    private val project1 = Project.Saved("1", "1", "1", "#ffffff")
    private val projectsRepository = InMemProjectsRepository().apply {
        save(project1)
    }
    private val instancesRepository = com.jed.optima.formstest.InMemInstancesRepository()
    private val instancesRepositoryProvider = mock<InstancesRepositoryProvider>().apply {
        whenever(create(project1.uuid)).thenReturn(instancesRepository)
    }
    private val settingsProvider = InMemSettingsProvider()
    private val projectsDataService = ProjectsDataService(AppState(), settingsProvider, projectsRepository, mock(), mock())
    private val formUpdateScheduler = mock<com.jed.optima.android.backgroundwork.FormUpdateScheduler>()
    private val instanceSubmitScheduler = mock<com.jed.optima.android.backgroundwork.InstanceSubmitScheduler>()
    private val storagePathProvider = mock<StoragePathProvider>().apply {
        whenever(getProjectRootDirPath(project1.uuid)).thenReturn("")
    }
    private val changeLockProvider = mock<ChangeLockProvider> {
        on { getFormLock(any()) } doReturn BooleanChangeLock()
        on { getInstanceLock(any()) } doReturn BooleanChangeLock()
    }
    private val deleter = ProjectDeleter(
        projectsRepository,
        projectsDataService,
        formUpdateScheduler,
        instanceSubmitScheduler,
        instancesRepositoryProvider,
        storagePathProvider,
        changeLockProvider,
        settingsProvider
    )

    @Test
    fun `If there are incomplete instances the project should not be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are invalid instances the project should not be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_INVALID)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are valid instances the project should not be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_VALID)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are new edits the project should not be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are complete instances the project should not be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_COMPLETE)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are submission failed instances the project should not be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.UnsentInstances::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are saved instances but all sent the project should be deleted`() {
        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .status(com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED)
                .build()
        )

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfullyLastProject::class.java))
        assertThat(projectsRepository.projects.size, equalTo(0))
    }

    @Test
    fun `If there are no instances the project should be deleted`() {
        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfullyLastProject::class.java))
        assertThat(projectsRepository.projects.size, equalTo(0))
    }

    @Test
    fun `If there are running background jobs that use blank forms the project should not be deleted`() {
        val formChangeLock = BooleanChangeLock().apply {
            lock("blah")
        }
        whenever(changeLockProvider.getFormLock(any())).thenReturn(formChangeLock)

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.RunningBackgroundJobs::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `If there are running background jobs that use saved forms the project should not be deleted`() {
        val changeLock = BooleanChangeLock().apply {
            lock("blah")
        }
        whenever(changeLockProvider.getInstanceLock(any())).thenReturn(changeLock)

        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.RunningBackgroundJobs::class.java))
        assertThat(projectsRepository.projects.contains(project1), equalTo(true))
    }

    @Test
    fun `Deleting project cancels scheduled form updates and instance submits`() {
        deleter.deleteProject(project1.uuid)

        verify(formUpdateScheduler).cancelUpdates(project1.uuid)
        verify(instanceSubmitScheduler).cancelSubmit(project1.uuid)
    }

    @Test
    fun `Deleting project clears its settings`() {
        settingsProvider.getMetaSettings().save(MetaKeys.KEY_INSTALL_ID, "1234")
        settingsProvider.getUnprotectedSettings(project1.uuid).save(ProjectKeys.KEY_SERVER_URL, "https://my-server.com")
        settingsProvider.getProtectedSettings(project1.uuid).save(ProtectedProjectKeys.KEY_AUTOSEND, false)
        settingsProvider.getUnprotectedSettings("2").save(ProjectKeys.KEY_SERVER_URL, "https://my-server.com")
        settingsProvider.getProtectedSettings("2").save(ProtectedProjectKeys.KEY_AUTOSEND, false)

        deleter.deleteProject(project1.uuid)

        assertThat(settingsProvider.getMetaSettings().getString(MetaKeys.KEY_INSTALL_ID), equalTo("1234"))
        settingsProvider.getUnprotectedSettings(project1.uuid).getAll().forEach { (key, value) ->
            assertThat(value, equalTo(Defaults.protected[key]))
        }
        settingsProvider.getProtectedSettings(project1.uuid).getAll().forEach { (key, value) ->
            assertThat(value, equalTo(Defaults.protected[key]))
        }

        assertThat(settingsProvider.getUnprotectedSettings("2").getString(ProjectKeys.KEY_SERVER_URL), equalTo("https://my-server.com"))
        assertThat(settingsProvider.getProtectedSettings("2").getBoolean(ProtectedProjectKeys.KEY_AUTOSEND), equalTo(false))
    }

    @Test
    fun `If the deleted project was the last one return DeletedSuccessfully with null parameter`() {
        val result = deleter.deleteProject(project1.uuid)

        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfullyLastProject::class.java))
    }

    @Test
    fun `If the deleted project was the current one and not the last one set the current project and return the new current one`() {
        val project2 = Project.Saved("2", "2", "2", "#cccccc")
        projectsRepository.save(project2)
        projectsDataService.setCurrentProject(project1.uuid)

        val result = deleter.deleteProject(project1.uuid)

        assertThat(projectsDataService.requireCurrentProject().uuid, equalTo(project2.uuid))
        assertThat((result as DeleteProjectResult.DeletedSuccessfullyCurrentProject).newCurrentProject, equalTo(project2))
    }

    @Test
    fun `If the deleted project was not the current one and not the last one do not set the current project and return DeletedSuccessfully with null parameter`() {
        val project2 = Project.Saved("2", "2", "2", "#cccccc")
        projectsRepository.save(project2)
        projectsDataService.setCurrentProject(project2.uuid)

        val result = deleter.deleteProject(project1.uuid)

        assertThat(projectsDataService.requireCurrentProject().uuid, equalTo(project2.uuid))
        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfullyInactiveProject::class.java))
    }

    @Test
    fun `Project directory should be removed`() {
        val projectDir = TempFiles.createTempDir()
        File(projectDir, "dir").mkdir()

        assertThat(projectDir.exists(), equalTo(true))
        assertThat(projectDir.listFiles().size, equalTo(1))

        whenever(storagePathProvider.getProjectRootDirPath(project1.uuid)).thenReturn(projectDir.absolutePath)

        deleter.deleteProject(project1.uuid)

        assertThat(projectDir.exists(), equalTo(false))
    }

    @Test
    fun `If there is no project id passed to ProjectDeleter#deleteProject() then delete the current project`() {
        val project2 = Project.Saved("2", "2", "2", "#cccccc")
        projectsRepository.save(project2)
        projectsDataService.setCurrentProject(project2.uuid)
        whenever(instancesRepositoryProvider.create(project2.uuid)).thenReturn(instancesRepository)
        whenever(storagePathProvider.getProjectRootDirPath(project2.uuid)).thenReturn("")

        val result = deleter.deleteProject()

        assertThat(result, instanceOf(DeleteProjectResult.DeletedSuccessfullyCurrentProject::class.java))
    }
}
