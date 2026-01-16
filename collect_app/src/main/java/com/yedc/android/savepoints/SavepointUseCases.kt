package com.yedc.android.savepoints

import android.net.Uri
import com.yedc.android.utilities.ContentUriHelper
import com.yedc.forms.savepoints.Savepoint
import com.yedc.forms.savepoints.SavepointsRepository
import java.io.File

object SavepointUseCases {
    fun getSavepoint(
        uri: Uri,
        uriMimeType: String,
        formsRepository: com.yedc.forms.FormsRepository,
        instanceRepository: com.yedc.forms.instances.InstancesRepository,
        savepointsRepository: SavepointsRepository
    ): Savepoint? {
        return if (uriMimeType == _root_ide_package_.com.yedc.android.external.FormsContract.CONTENT_ITEM_TYPE) {
            val selectedForm = formsRepository.get(ContentUriHelper.getIdFromUri(uri))!!

            formsRepository.getAllByFormId(selectedForm.formId)
                .filter { it.date <= selectedForm.date }
                .sortedByDescending { it.date }
                .forEach { form ->
                    val savepoint = savepointsRepository.get(form.dbId, null)
                    if (savepoint != null && File(savepoint.savepointFilePath).exists()) {
                        return savepoint
                    }
                }
            null
        } else {
            val instance = instanceRepository.get(ContentUriHelper.getIdFromUri(uri))!!
            val form = formsRepository.getLatestByFormIdAndVersion(instance.formId, instance.formVersion)!!

            val savepoint = savepointsRepository.get(form.dbId, instance.dbId)
            if (savepoint != null &&
                File(savepoint.savepointFilePath).exists() &&
                File(savepoint.savepointFilePath).lastModified() > instance.lastStatusChangeDate
            ) {
                savepoint
            } else {
                null
            }
        }
    }
}
