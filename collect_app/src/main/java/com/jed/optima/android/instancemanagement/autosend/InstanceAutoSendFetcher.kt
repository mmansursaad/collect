package com.jed.optima.android.instancemanagement.autosend

object InstanceAutoSendFetcher {

    fun getInstancesToAutoSend(
        instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
        formsRepository: com.jed.optima.forms.FormsRepository,
        forcedOnly: Boolean = false
    ): List<com.jed.optima.forms.instances.Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(
            com.jed.optima.forms.instances.Instance.STATUS_COMPLETE,
            com.jed.optima.forms.instances.Instance.STATUS_SUBMISSION_FAILED
        )

        val filter: (com.jed.optima.forms.Form) -> Boolean = if (forcedOnly) {
            { form -> form.getAutoSendMode() == FormAutoSendMode.FORCED }
        } else {
            { form -> form.getAutoSendMode() == FormAutoSendMode.NEUTRAL }
        }

        return allFinalizedForms.filter {
            formsRepository.getLatestByFormIdAndVersion(it.formId, it.formVersion)
                ?.let { form -> filter(form) } ?: false
        }
    }
}
