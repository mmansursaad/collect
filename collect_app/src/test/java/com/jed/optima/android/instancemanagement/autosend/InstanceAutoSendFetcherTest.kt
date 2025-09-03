package com.jed.optima.android.instancemanagement.autosend

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import com.jed.optima.formstest.FormUtils.buildForm
import com.jed.optima.formstest.InstanceUtils.buildInstance

@RunWith(AndroidJUnit4::class)
class InstanceAutoSendFetcherTest {

    private val instancesRepository = com.jed.optima.formstest.InMemInstancesRepository()
    private val formsRepository = com.jed.optima.formstest.InMemFormsRepository()

    private val formWithEnabledAutoSend = buildForm("1", "1", createTempDir().absolutePath, autosend = "true").build()

    private val instanceOfFormWithEnabledAutoSendIncomplete = buildInstance("1", "1", "instance 1", com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendComplete = buildInstance("1", "1", "instance 2", com.jed.optima.forms.instances.Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendSubmissionFailed = buildInstance("1", "1", "instance 3", com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithEnabledAutoSendSubmitted = buildInstance("1", "1", "instance 4", com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithoutSpecifiedAutoSend = buildForm("2", "1", createTempDir().absolutePath).build()

    private val instanceOfFormWithoutSpecifiedAutoSendIncomplete = buildInstance("2", "1", "instance 1", com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendComplete = buildInstance("2", "1", "instance 2", com.jed.optima.forms.instances.Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed = buildInstance("2", "1", "instance 3", com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithoutSpecifiedAutoSendSubmitted = buildInstance("2", "1", "instance 4", com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithDisabledAutoSend = buildForm("3", "1", createTempDir().absolutePath, autosend = "false").build()

    private val instanceOfFormWithDisabledAutoSendIncomplete = buildInstance("3", "1", "instance 1", com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendComplete = buildInstance("3", "1", "instance 2", com.jed.optima.forms.instances.Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendSubmissionFailed = buildInstance("3", "1", "instance 3", com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithDisabledAutoSendSubmitted = buildInstance("3", "1", "instance 4", com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    private val formWithCustomAutoSend = buildForm("4", "1", createTempDir().absolutePath, autosend = "anything").build()

    private val instanceOfFormWithCustomAutoSendIncomplete = buildInstance("4", "1", "instance 1", com.jed.optima.forms.instances.Instance.STATUS_INCOMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendComplete = buildInstance("4", "1", "instance 2", com.jed.optima.forms.instances.Instance.STATUS_COMPLETE, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendSubmissionFailed = buildInstance("4", "1", "instance 3", com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED, null, createTempDir().absolutePath).build()
    private val instanceOfFormWithCustomAutoSendSubmitted = buildInstance("4", "1", "instance 4", com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED, null, createTempDir().absolutePath).build()

    @Test
    fun `return all finalized instances of forms that do not have auto send on a form level`() {
        formsRepository.save(formWithEnabledAutoSend)
        formsRepository.save(formWithoutSpecifiedAutoSend)
        formsRepository.save(formWithDisabledAutoSend)
        formsRepository.save(formWithCustomAutoSend)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendIncomplete)
            save(instanceOfFormWithEnabledAutoSendComplete)
            save(instanceOfFormWithEnabledAutoSendSubmissionFailed)
            save(instanceOfFormWithEnabledAutoSendSubmitted)

            save(instanceOfFormWithoutSpecifiedAutoSendIncomplete)
            save(instanceOfFormWithoutSpecifiedAutoSendComplete)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmitted)

            save(instanceOfFormWithDisabledAutoSendIncomplete)
            save(instanceOfFormWithDisabledAutoSendComplete)
            save(instanceOfFormWithDisabledAutoSendSubmissionFailed)
            save(instanceOfFormWithDisabledAutoSendSubmitted)

            save(instanceOfFormWithCustomAutoSendIncomplete)
            save(instanceOfFormWithCustomAutoSendComplete)
            save(instanceOfFormWithCustomAutoSendSubmissionFailed)
            save(instanceOfFormWithCustomAutoSendSubmitted)
        }

        val instancesToSend = InstanceAutoSendFetcher.getInstancesToAutoSend(
            instancesRepository,
            formsRepository
        )

        assertThat(
            instancesToSend.map { it.instanceFilePath },
            contains(
                instanceOfFormWithoutSpecifiedAutoSendComplete.instanceFilePath,
                instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed.instanceFilePath,
                instanceOfFormWithCustomAutoSendComplete.instanceFilePath,
                instanceOfFormWithCustomAutoSendSubmissionFailed.instanceFilePath
            )
        )
    }

    @Test
    fun `return all finalized forms with autosend when formAutoSend is true`() {
        formsRepository.save(formWithEnabledAutoSend)
        formsRepository.save(formWithoutSpecifiedAutoSend)
        formsRepository.save(formWithDisabledAutoSend)
        formsRepository.save(formWithCustomAutoSend)

        instancesRepository.apply {
            save(instanceOfFormWithEnabledAutoSendIncomplete)
            save(instanceOfFormWithEnabledAutoSendComplete)
            save(instanceOfFormWithEnabledAutoSendSubmissionFailed)
            save(instanceOfFormWithEnabledAutoSendSubmitted)

            save(instanceOfFormWithoutSpecifiedAutoSendIncomplete)
            save(instanceOfFormWithoutSpecifiedAutoSendComplete)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmissionFailed)
            save(instanceOfFormWithoutSpecifiedAutoSendSubmitted)

            save(instanceOfFormWithDisabledAutoSendIncomplete)
            save(instanceOfFormWithDisabledAutoSendComplete)
            save(instanceOfFormWithDisabledAutoSendSubmissionFailed)
            save(instanceOfFormWithDisabledAutoSendSubmitted)

            save(instanceOfFormWithCustomAutoSendIncomplete)
            save(instanceOfFormWithCustomAutoSendComplete)
            save(instanceOfFormWithCustomAutoSendSubmissionFailed)
            save(instanceOfFormWithCustomAutoSendSubmitted)
        }

        val instancesToSend = InstanceAutoSendFetcher.getInstancesToAutoSend(
            instancesRepository,
            formsRepository,
            forcedOnly = true
        )

        assertThat(
            instancesToSend.map { it.instanceFilePath },
            contains(
                instanceOfFormWithEnabledAutoSendComplete.instanceFilePath,
                instanceOfFormWithEnabledAutoSendSubmissionFailed.instanceFilePath,
            )
        )
    }
}
