package com.jed.optima.android.instancemanagement

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.jed.optima.formstest.FormUtils.createXFormFile
import com.jed.optima.formstest.InstanceUtils.buildInstance

class InstanceDeleterTest {
    private val formsRepository = com.jed.optima.formstest.InMemFormsRepository()
    private val instancesRepository = com.jed.optima.formstest.InMemInstancesRepository()
    private val instanceDeleter = InstanceDeleter(instancesRepository, formsRepository)

    @Test
    fun `Soft-delete instance if it is submitted`() {
        val instance = instancesRepository.save(
            buildInstance("1", "version", createTempDir().absolutePath)
                .status(com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED)
                .build()
        )

        instanceDeleter.delete(instance.dbId)

        assertNotNull(instancesRepository[instance.dbId]!!.deletedDate)
    }

    @Test
    fun `Delete corresponding form when it is soft-deleted and there are no other instances`() {
        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("version")
                .deleted(true)
                .formFilePath(createXFormFile("1", "version").absolutePath)
                .build()
        )

        val instanceToDelete = instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("version")
                .instanceFilePath(createTempDir().absolutePath)
                .build()
        )

        instanceDeleter.delete(instanceToDelete.dbId)

        assertTrue(formsRepository.all.isEmpty())
    }

    @Test
    fun `Do not delete corresponding form when it is soft-deleted and there and there are other instances`() {
        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("version")
                .deleted(true)
                .formFilePath(createXFormFile("1", "version").absolutePath)
                .build()
        )

        instancesRepository.save(
            buildInstance(
                "1",
                "version",
                createTempDir().absolutePath
            ).build()
        )

        instancesRepository.save(
            buildInstance(
                "1",
                "version",
                createTempDir().absolutePath
            ).build()
        )

        val id = instancesRepository.all[0].dbId
        instanceDeleter.delete(id)
        assertThat(formsRepository.all.size, equalTo(1))
    }

    @Test
    fun `Delete corresponding form when it is soft-deleted and there are no other instances with the same version`() {
        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("1")
                .deleted(true)
                .formFilePath(createXFormFile("1", "1").absolutePath)
                .build()
        )

        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("2")
                .formFilePath(createXFormFile("1", "2").absolutePath)
                .deleted(true)
                .build()
        )

        val instanceToDelete = instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("1")
                .instanceFilePath(createTempDir().absolutePath)
                .build()
        )

        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("2")
                .instanceFilePath(createTempDir().absolutePath)
                .build()
        )

        instanceDeleter.delete(instanceToDelete.dbId)

        assertThat(formsRepository.all.size, equalTo(1))
        assertThat(formsRepository.all[0].version, equalTo("2"))
    }

    @Test
    fun `Delete corresponding form when it is soft-deleted and there are another deleted instances`() {
        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("version")
                .deleted(true)
                .formFilePath(createXFormFile("1", "version").absolutePath)
                .build()
        )

        instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .deletedDate(0L)
                .formVersion("version")
                .build()
        )

        val instanceToDelete = instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("version")
                .instanceFilePath(createTempDir().absolutePath)
                .build()
        )

        instanceDeleter.delete(instanceToDelete.dbId)
        assertThat(formsRepository.all.size, equalTo(0))
    }

    @Test
    fun `Do not delete corresponding form when it is not soft-deleted`() {
        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("version")
                .deleted(false)
                .formFilePath(createXFormFile("1", "version").absolutePath)
                .build()
        )

        val instanceToDelete = instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("version")
                .instanceFilePath(createTempDir().absolutePath)
                .build()
        )

        instanceDeleter.delete(instanceToDelete.dbId)
        assertThat(formsRepository.all.size, equalTo(1))
    }

    @Test
    fun `Do not delete corresponding form when its version is not soft-deleted`() {
        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("1")
                .deleted(true)
                .formFilePath(createXFormFile("1", "1").absolutePath)
                .build()
        )

        formsRepository.save(
            com.jed.optima.forms.Form.Builder()
                .formId("1")
                .version("2")
                .deleted(false)
                .formFilePath(createXFormFile("1", "2").absolutePath)
                .build()
        )

        val instanceToDelete = instancesRepository.save(
            com.jed.optima.forms.instances.Instance.Builder()
                .formId("1")
                .formVersion("2")
                .instanceFilePath(createTempDir().absolutePath)
                .build()
        )

        instanceDeleter.delete(instanceToDelete.dbId)
        assertThat(formsRepository.all.size, equalTo(2))
    }
}
