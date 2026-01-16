/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yedc.android.formmanagement

import com.yedc.android.application.FeatureFlags
import com.yedc.forms.FormSourceException
import com.yedc.forms.ManifestFile
import com.yedc.forms.MediaFile
import com.yedc.openrosa.forms.OpenRosaClient
import com.yedc.shared.strings.Md5.getMd5Hash
import timber.log.Timber

/**
 * Open to allow mocking (used in existing Java tests)
 */
open class ServerFormsDetailsFetcher(
    private val formsRepository: com.yedc.forms.FormsRepository,
    private val formSource: com.yedc.forms.FormSource
) {
    open fun updateUrl(url: String) {
        (formSource as OpenRosaClient).updateUrl(url)
    }

    open fun updateCredentials(webCredentialsUtils: _root_ide_package_.com.yedc.android.utilities.WebCredentialsUtils) {
        (formSource as OpenRosaClient).updateWebCredentialsUtils(webCredentialsUtils)
    }

    @Throws(FormSourceException::class)
    open fun fetchFormDetails(): List<ServerFormDetails> {
        val formList = formSource.fetchFormList()
        return formList.map { listItem ->
            val manifestFile = listItem.manifestURL?.let {
                getManifestFile(formSource, it)
            }

            val forms = formsRepository.getAllNotDeletedByFormId(listItem.formID)
            val thisFormAlreadyDownloaded = forms.isNotEmpty()

            val formHash = listItem.hash
            val existingForm = if (formHash != null) {
                getFormByHash(formHash)
            } else {
                null
            }

            val isNewerFormVersionAvailable = listItem.hash.let {
                if (it == null) {
                    false
                } else if (thisFormAlreadyDownloaded) {
                    existingForm == null || existingForm.isDeleted
                } else {
                    false
                }
            }

            val areNewerMediaFilesAvailable = if (existingForm != null && manifestFile != null) {
                areNewerMediaFilesAvailable(existingForm, manifestFile.mediaFiles)
            } else {
                false
            }

            ServerFormDetails(
                listItem.name,
                listItem.downloadURL,
                listItem.formID,
                listItem.version,
                listItem.hash,
                !thisFormAlreadyDownloaded,
                isNewerFormVersionAvailable || areNewerMediaFilesAvailable,
                manifestFile,
                FeatureFlags.FASTER_FORM_UPDATES && (!isNewerFormVersionAvailable && areNewerMediaFilesAvailable)
            )
        }
    }

    private fun getManifestFile(formSource: com.yedc.forms.FormSource, manifestUrl: String): ManifestFile? {
        return try {
            formSource.fetchManifest(manifestUrl)
        } catch (formSourceException: FormSourceException) {
            Timber.w(formSourceException)
            null
        }
    }

    private fun areNewerMediaFilesAvailable(
        existingForm: com.yedc.forms.Form,
        newMediaFiles: List<MediaFile>
    ): Boolean {
        if (newMediaFiles.isEmpty()) {
            return false
        }

        val localMediaHashes = _root_ide_package_.com.yedc.android.utilities.FormUtils.getMediaFiles(existingForm)
            .map { it.getMd5Hash() }
            .toSet()

        return newMediaFiles.any {
            !it.filename.endsWith(".zip") && it.hash !in localMediaHashes
        }
    }

    private fun getFormByHash(hash: String): com.yedc.forms.Form? {
        return formsRepository.getOneByMd5Hash(hash)
    }
}
