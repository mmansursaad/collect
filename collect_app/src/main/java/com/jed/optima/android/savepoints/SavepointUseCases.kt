package com.jed.optima.android.savepoints

import android.net.Uri
import com.jed.optima.android.utilities.ContentUriHelper
import com.jed.optima.forms.savepoints.Savepoint
import com.jed.optima.forms.savepoints.SavepointsRepository
import java.io.File

object SavepointUseCases {
    fun getSavepoint(
        uri: Uri,
        uriMimeType: String,
        formsRepository: com.jed.optima.forms.FormsRepository,
        instanceRepository: com.jed.optima.forms.instances.InstancesRepository,
        savepointsRepository: SavepointsRepository
    ): Savepoint? {
        return if (uriMimeType == com.jed.optima.android.external.FormsContract.CONTENT_ITEM_TYPE) {
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
