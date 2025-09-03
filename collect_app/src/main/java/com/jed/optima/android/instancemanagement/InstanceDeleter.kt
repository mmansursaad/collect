package com.jed.optima.android.instancemanagement

class InstanceDeleter(
    private val instancesRepository: com.jed.optima.forms.instances.InstancesRepository,
    private val formsRepository: com.jed.optima.forms.FormsRepository
) {
    fun delete(ids: Array<Long>) {
        ids.forEach {
            delete(it)
        }
    }

    fun delete(id: Long?) {
        instancesRepository[id]?.let { instance ->
            if (instance.status == com.jed.optima.forms.instances.Instance.STATUS_SUBMITTED) {
                instancesRepository.deleteWithLogging(id)
            } else {
                instancesRepository.delete(id)
            }
            val form =
                formsRepository.getLatestByFormIdAndVersion(instance.formId, instance.formVersion)
            if (form != null && form.isDeleted) {
                val otherInstances = instancesRepository.getAllNotDeletedByFormIdAndVersion(
                    form.formId,
                    form.version
                )
                if (otherInstances.isEmpty()) {
                    formsRepository.delete(form.dbId)
                }
            }
        }
    }
}
