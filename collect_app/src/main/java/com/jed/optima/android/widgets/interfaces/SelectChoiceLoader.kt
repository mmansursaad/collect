package com.jed.optima.android.widgets.interfaces

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import java.io.FileNotFoundException

interface SelectChoiceLoader {

    @Throws(FileNotFoundException::class, XPathSyntaxException::class, com.jed.optima.android.exception.ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt): List<SelectChoice>
}
