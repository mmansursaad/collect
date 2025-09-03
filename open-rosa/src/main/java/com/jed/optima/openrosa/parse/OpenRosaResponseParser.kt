package com.jed.optima.openrosa.parse

import org.kxml2.kdom.Document
import com.jed.optima.forms.FormListItem
import com.jed.optima.forms.MediaFile
import com.jed.optima.openrosa.forms.EntityIntegrity

interface OpenRosaResponseParser {
    fun parseFormList(document: Document): List<FormListItem>?
    fun parseManifest(document: Document): List<MediaFile>?
    fun parseIntegrityResponse(doc: Document): List<EntityIntegrity>?
}
