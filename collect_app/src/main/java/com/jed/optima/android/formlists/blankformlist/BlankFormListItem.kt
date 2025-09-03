package com.jed.optima.android.formlists.blankformlist

import android.net.Uri

data class BlankFormListItem(
    val databaseId: Long,
    val formId: String,
    val formName: String,
    val formVersion: String,
    val geometryPath: String,
    val dateOfCreation: Long,
    val dateOfLastUsage: Long,
    val dateOfLastDetectedAttachmentsUpdate: Long?,
    val contentUri: Uri
)

fun com.jed.optima.forms.Form.toBlankFormListItem(projectId: String, instancesRepository: com.jed.optima.forms.instances.InstancesRepository) = BlankFormListItem(
    databaseId = this.dbId,
    formId = this.formId,
    formName = this.displayName,
    formVersion = this.version ?: "",
    geometryPath = this.geometryXpath ?: "",
    dateOfCreation = this.date,
    dateOfLastUsage = instancesRepository
        .getAllByFormId(this.formId)
        .filter { it.formVersion == this.version }
        .maxByOrNull { it.lastStatusChangeDate }?.lastStatusChangeDate ?: 0L,
    dateOfLastDetectedAttachmentsUpdate = this.lastDetectedAttachmentsUpdateDate,
    contentUri = com.jed.optima.android.external.FormsContract.getUri(projectId, this.dbId)
)
