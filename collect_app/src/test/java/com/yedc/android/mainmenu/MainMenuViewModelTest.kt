package com.yedc.android.mainmenu

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.MutableStateFlow
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import com.yedc.android.instancemanagement.InstancesDataService
import com.yedc.android.instancemanagement.autosend.AutoSendSettingsProvider
import com.yedc.android.projects.ProjectsDataService
import com.yedc.android.utilities.FormsRepositoryProvider
import com.yedc.android.utilities.InstancesRepositoryProvider
import com.yedc.androidtest.getOrAwaitValue
import com.yedc.formstest.FormUtils
import com.yedc.projects.Project
import com.yedc.settings.InMemSettingsProvider
import com.yedc.settings.keys.ProtectedProjectKeys
import com.yedc.shared.TempFiles
import com.yedc.testshared.FakeScheduler
import java.util.concurrent.TimeoutException

@RunWith(AndroidJUnit4::class)
class MainMenuViewModelTest {
    private val formsRepository = com.yedc.formstest.InMemFormsRepository()
    private val formsRepositoryProvider = mock<FormsRepositoryProvider>().apply {
        whenever(create()).thenReturn(formsRepository)
    }
    private val instancesRepository = com.yedc.formstest.InMemInstancesRepository()
    private val instancesRepositoryProvider = mock<InstancesRepositoryProvider>().apply {
        whenever(create()).thenReturn(instancesRepository)
    }
    private val autoSendSettingsProvider = mock<AutoSendSettingsProvider>()
    private val settingsProvider = InMemSettingsProvider()

    private val projectsDataService = mock<ProjectsDataService> {
        on { requireCurrentProject() } doReturn Project.DEMO_PROJECT
        on { getCurrentProject() } doReturn MutableStateFlow(Project.DEMO_PROJECT)
    }

    private val instancesDataService = mock<InstancesDataService> {
        on { getSendableCount(any()) } doReturn MutableStateFlow(0)
        on { getEditableCount(any()) } doReturn MutableStateFlow(0)
        on { getSentCount(any()) } doReturn MutableStateFlow(0)
    }

    private val scheduler = FakeScheduler()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun `version when beta release returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `version when dirty beta release returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-dirty")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `version when beta tag returns semantic version with prefix and beta version`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4")
        assertThat(viewModel.version, equalTo("v1.23.0 Beta 1"))
    }

    @Test
    fun `versionCommitDescription when release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7")
        assertThat(viewModel.versionCommitDescription, equalTo(null))
    }

    @Test
    fun `versionCommitDescription when dirty release returns dirty`() {
        val viewModel = createViewModelWithVersion("v1.1.7-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("dirty"))
    }

    @Test
    fun `versionCommitDescription when beta release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7-beta.7")
        assertThat(viewModel.versionCommitDescription, equalTo(null))
    }

    @Test
    fun `versionCommitDescription when dirty beta release returns null`() {
        val viewModel = createViewModelWithVersion("v1.1.7-beta.7-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("dirty"))
    }

    @Test
    fun `versionCommitDescription when beta tag returns commit count and SHA`() {
        val viewModel = createViewModelWithVersion("v1.23.0-beta.1-181-ge51d004d4")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4"))
    }

    @Test
    fun `versionCommitDescription when release tag returns commit count and SHA`() {
        val viewModel = createViewModelWithVersion("v1.23.0-181-ge51d004d4")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4"))
    }

    @Test
    fun `versionCommitDescription when dirty commit returns commit count and SHA and dirty tag`() {
        val viewModel = createViewModelWithVersion("v1.14.0-181-ge51d004d4-dirty")
        assertThat(viewModel.versionCommitDescription, equalTo("181-ge51d004d4-dirty"))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is saved as draft, editing drafts is enabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, true)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_INCOMPLETE)
                .build()
        )

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.edit_form))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is saved as draft, editing drafts is enabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, true)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_INCOMPLETE)
                .canEditWhenComplete(false)
                .build()
        )

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.edit_form))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is saved as draft, editing drafts is disabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_INCOMPLETE)
                .build()
        )

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.view_form))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is saved as draft, editing drafts is disabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_EDIT_SAVED, false)

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_INCOMPLETE)
                .canEditWhenComplete(false)
                .build()
        )

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_saved_as_draft))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.view_form))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is finalized, auto send is disabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_COMPLETE)
                .build()
        )
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(false)

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_saved))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.view_form))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is finalized, auto send is disabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_COMPLETE)
                .canEditWhenComplete(false)
                .build()
        )
        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(false)

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_saved))
        assertThat(formSavedSnackbarType.action, equalTo(null))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is finalized, auto send is enabled and encryption is disabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_COMPLETE)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_sending))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.view_form))
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance is finalized, auto send is enabled and encryption is enabled`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_COMPLETE)
                .canEditWhenComplete(false)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_sending))
        assertThat(formSavedSnackbarType.action, equalTo(null))
    }

    @Test(expected = TimeoutException::class)
    fun `setSavedForm should not set when the corresponding instance is already sent`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_SUBMITTED)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)
        viewModel.setSavedForm(uri)
        scheduler.flush()

        viewModel.savedForm.getOrAwaitValue()
    }

    @Test
    fun `setSavedForm should set proper message and action when the corresponding instance failed to sent`() {
        val viewModel = createViewModelWithVersion("")

        formsRepository.save(FormUtils.buildForm("1", "1", TempFiles.createTempDir().absolutePath).build())
        val instance = instancesRepository.save(
            com.yedc.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        whenever(autoSendSettingsProvider.isAutoSendEnabledInSettings()).thenReturn(true)

        val uri = _root_ide_package_.com.yedc.android.external.InstancesContract.getUri(Project.DEMO_PROJECT_ID, instance.dbId)

        viewModel.setSavedForm(uri)
        scheduler.flush()

        val formSavedSnackbarType = viewModel.savedForm.getOrAwaitValue().value!!
        assertThat(formSavedSnackbarType.message, equalTo(com.yedc.strings.R.string.form_sending))
        assertThat(formSavedSnackbarType.action, equalTo(com.yedc.strings.R.string.view_form))
    }

    private fun createViewModelWithVersion(version: String): MainMenuViewModel {
        return MainMenuViewModel(mock(),
            _root_ide_package_.com.yedc.android.version.VersionInformation { version }, settingsProvider, instancesDataService, scheduler, formsRepositoryProvider, instancesRepositoryProvider, autoSendSettingsProvider, projectsDataService)
    }
}
