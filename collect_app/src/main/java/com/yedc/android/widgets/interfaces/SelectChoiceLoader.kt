package com.yedc.android.widgets.interfaces

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import java.io.FileNotFoundException

interface SelectChoiceLoader {

    @Throws(FileNotFoundException::class, XPathSyntaxException::class, _root_ide_package_.com.yedc.android.exception.ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt): List<SelectChoice>
}
