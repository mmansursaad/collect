package com.yedc.android.instancemanagement

class InstanceDeleter(
    private val instancesRepository: com.yedc.forms.instances.InstancesRepository,
    private val formsRepository: com.yedc.forms.FormsRepository
) {
    fun delete(ids: Array<Long>) {
        ids.forEach {
            delete(it)
        }
    }

    fun delete(id: Long?) {
        instancesRepository[id]?.let { instance ->
            if (instance.status == com.yedc.forms.instances.Instance.STATUS_SUBMITTED) {
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
