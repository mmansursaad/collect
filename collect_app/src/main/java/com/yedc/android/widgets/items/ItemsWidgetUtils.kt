package com.yedc.android.widgets.items

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import com.yedc.android.R
import com.yedc.android.widgets.interfaces.SelectChoiceLoader
import java.io.FileNotFoundException

object ItemsWidgetUtils {

    @JvmStatic
    fun loadItemsAndHandleErrors(
        widget: _root_ide_package_.com.yedc.android.widgets.QuestionWidget,
        prompt: FormEntryPrompt,
        selectChoiceLoader: SelectChoiceLoader
    ): List<SelectChoice> {
        return try {
            selectChoiceLoader.loadSelectChoices(prompt)
        } catch (e: FileNotFoundException) {
            widget.showWarning(widget.context.getString(com.yedc.strings.R.string.file_missing, e.message))
            emptyList()
        } catch (e: XPathSyntaxException) {
            widget.showWarning(widget.context.getString(com.yedc.strings.R.string.parser_exception, e.message))
            emptyList()
        } catch (e: _root_ide_package_.com.yedc.android.exception.ExternalDataException) {
            widget.showWarning(e.message)
            emptyList()
        }
    }
}
