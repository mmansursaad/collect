package com.yedc.android.instancemanagement.autosend

object InstanceAutoSendFetcher {

    fun getInstancesToAutoSend(
        instancesRepository: com.yedc.forms.instances.InstancesRepository,
        formsRepository: com.yedc.forms.FormsRepository,
        forcedOnly: Boolean = false
    ): List<com.yedc.forms.instances.Instance> {
        val allFinalizedForms = instancesRepository.getAllByStatus(
            com.yedc.forms.instances.Instance.STATUS_COMPLETE,
            com.yedc.forms.instances.Instance.STATUS_SUBMISSION_FAILED
        )

        val filter: (com.yedc.forms.Form) -> Boolean = if (forcedOnly) {
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
