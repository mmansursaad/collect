package com.jed.optima.android.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import com.jed.optima.android.notifications.Notifier
import com.jed.optima.android.projects.ProjectDependencyModule
import com.jed.optima.android.utilities.ChangeLocks
import com.jed.optima.androidshared.data.AppState
import com.jed.optima.forms.instances.Instance.STATUS_COMPLETE
import com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE
import com.jed.optima.forms.instances.Instance.STATUS_INVALID
import com.jed.optima.forms.instances.Instance.STATUS_NEW_EDIT
import com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED
import com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED
import com.jed.optima.forms.instances.Instance.STATUS_VALID
import com.jed.optima.formstest.FormFixtures
import com.jed.optima.formstest.InstanceFixtures
import com.jed.optima.projects.ProjectDependencyFactory
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.shared.locks.BooleanChangeLock
import com.jed.optima.shared.settings.InMemSettings
import java.io.File

@RunWith(AndroidJUnit4::class)
class InstancesDataServiceTest {
    private val projectsDependencyModuleFactory = CachingProjectDependencyModuleFactory { projectId ->
        ProjectDependencyModule(
            projectId,
            {
                InMemSettings().also {
                    it.save(ProjectKeys.KEY_SERVER_URL, "http://example.com")
                }
            },
            { com.jed.optima.formstest.InMemFormsRepository() },
            { com.jed.optima.formstest.InMemInstancesRepository() },
            mock(),
            { ChangeLocks(BooleanChangeLock(), BooleanChangeLock()) },
            mock(),
            mock(),
            mock(),
            mock()
        )
    }

    private val projectId = "projectId"
    private val projectDependencyModule = projectsDependencyModuleFactory.create(projectId)
    private val httpInterface = mock<com.jed.optima.openrosa.http.OpenRosaHttpInterface>()
    private val notifier = mock<Notifier>()

    private val instancesDataService =
        InstancesDataService(
            AppState(),
            mock(),
            projectsDependencyModuleFactory,
            notifier,
            mock(),
            httpInterface,
            mock()
        )

    @Test
    fun `instances should not be deleted if the instances database is locked`() {
        (projectDependencyModule.instancesLock as BooleanChangeLock).lock("blah")
        val result = instancesDataService.deleteInstances(projectId, longArrayOf(1))
        assertThat(result, equalTo(false))
    }

    @Test
    fun `instances should be deleted if the instances database is not locked`() {
        val result = instancesDataService.deleteInstances(projectId, longArrayOf(1))
        assertThat(result, equalTo(true))
    }

    @Test
    fun `sendInstances() returns true when there are no instances to send`() {
        val result = instancesDataService.sendInstances(projectId)
        assertThat(result, equalTo(true))
    }

    @Test
    fun `sendInstances() does not notify when there are no instances to send`() {
        instancesDataService.sendInstances(projectId)
        verifyNoInteractions(notifier)
    }

    @Test
    fun `sendInstances() returns false when an instance fails to send`() {
        val formsRepository = projectDependencyModule.formsRepository
        val form = formsRepository.save(FormFixtures.form())

        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.save(InstanceFixtures.instance(form = form, status = STATUS_COMPLETE))

        whenever(httpInterface.executeGetRequest(any(), any(), any()))
            .doReturn(
                com.jed.optima.openrosa.http.HttpGetResult(
                    null,
                    emptyMap(),
                    "",
                    500
                )
            )

        val result = instancesDataService.sendInstances(projectId)
        assertThat(result, equalTo(false))
    }

    @Test
    fun `#reset does not reset instances that can't be deleted before sending`() {
        val formsRepository = projectDependencyModule.formsRepository
        val form = formsRepository.save(FormFixtures.form())

        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_INCOMPLETE
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_COMPLETE
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_INVALID
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_VALID
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_NEW_EDIT
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_SUBMITTED
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                canDeleteBeforeSend = false,
                status = STATUS_SUBMISSION_FAILED
            )
        )

        instancesDataService.reset(projectDependencyModule.projectId)
        val remainingInstances = instancesRepository.all
        assertThat(remainingInstances.size, equalTo(2))
        assertThat(remainingInstances.any { it.status == STATUS_COMPLETE }, equalTo(true))
        assertThat(remainingInstances.any { it.status == STATUS_SUBMISSION_FAILED }, equalTo(true))
        assertThat(File(remainingInstances[0].instanceFilePath).parentFile?.exists(), equalTo(true))
        assertThat(File(remainingInstances[1].instanceFilePath).parentFile?.exists(), equalTo(true))
    }

    @Test
    fun `#reset can delete forms with edits`() {
        val formsRepository = projectDependencyModule.formsRepository
        val form = formsRepository.save(FormFixtures.form())

        val instancesRepository = projectDependencyModule.instancesRepository
        val originalInstance = instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                status = STATUS_COMPLETE,
                lastStatusChangeDate = 1
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                status = STATUS_COMPLETE,
                lastStatusChangeDate = 2,
                editOf = originalInstance.dbId,
                editNumber = 1
            )
        )
        instancesRepository.save(
            InstanceFixtures.instance(
                form = form,
                status = STATUS_VALID,
                lastStatusChangeDate = 3,
                editOf = originalInstance.dbId,
                editNumber = 2
            )
        )

        instancesDataService.reset(projectDependencyModule.projectId)
        val remainingInstances = instancesRepository.all
        assertThat(remainingInstances.size, equalTo(0))
    }

    @Test
    fun `#update updates instances and counts`() {
        val instancesRepository = projectDependencyModule.instancesRepository
        instancesRepository.save(InstanceFixtures.instance(status = STATUS_COMPLETE))
        instancesRepository.save(InstanceFixtures.instance(status = STATUS_SUBMITTED))
        instancesRepository.save(InstanceFixtures.instance(status = STATUS_INCOMPLETE))

        instancesDataService.update(projectId)
        assertThat(
            instancesDataService.getInstances(projectId).value,
            equalTo(instancesRepository.all)
        )
        assertThat(instancesDataService.getSentCount(projectId).value, equalTo(1))
        assertThat(instancesDataService.getEditableCount(projectId).value, equalTo(1))
        assertThat(instancesDataService.getSendableCount(projectId).value, equalTo(1))
        assertThat(instancesDataService.getInstances("otherProjectId").value, equalTo(emptyList()))
        assertThat(instancesDataService.getSentCount("otherProjectId").value, equalTo(0))
        assertThat(instancesDataService.getEditableCount("otherProjectId").value, equalTo(0))
        assertThat(instancesDataService.getSendableCount("otherProjectId").value, equalTo(0))
    }
}

class CachingProjectDependencyModuleFactory(private val moduleFactory: (String) -> ProjectDependencyModule) :
    ProjectDependencyFactory<ProjectDependencyModule> {

    private val modules = mutableMapOf<String, ProjectDependencyModule>()

    override fun create(projectId: String): ProjectDependencyModule {
        return modules.getOrPut(projectId) {
            moduleFactory(projectId)
        }
    }
}
