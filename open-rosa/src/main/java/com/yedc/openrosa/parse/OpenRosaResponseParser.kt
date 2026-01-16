package com.yedc.openrosa.parse

import org.kxml2.kdom.Document
import com.yedc.forms.FormListItem
import com.yedc.forms.MediaFile
import com.yedc.openrosa.forms.EntityIntegrity

interface OpenRosaResponseParser {
    fun parseFormList(document: Document): List<FormListItem>?
    fun parseManifest(document: Document): List<MediaFile>?
    fun parseIntegrityResponse(doc: Document): List<EntityIntegrity>?
}
