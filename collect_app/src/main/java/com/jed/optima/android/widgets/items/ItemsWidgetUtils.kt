package com.jed.optima.android.widgets.items

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.xpath.parser.XPathSyntaxException
import com.jed.optima.android.R
import com.jed.optima.android.widgets.interfaces.SelectChoiceLoader
import java.io.FileNotFoundException

object ItemsWidgetUtils {

    @JvmStatic
    fun loadItemsAndHandleErrors(
        widget: com.jed.optima.android.widgets.QuestionWidget,
        prompt: FormEntryPrompt,
        selectChoiceLoader: SelectChoiceLoader
    ): List<SelectChoice> {
        return try {
            selectChoiceLoader.loadSelectChoices(prompt)
        } catch (e: FileNotFoundException) {
            widget.showWarning(widget.context.getString(com.jed.optima.strings.R.string.file_missing, e.message))
            emptyList()
        } catch (e: XPathSyntaxException) {
            widget.showWarning(widget.context.getString(com.jed.optima.strings.R.string.parser_exception, e.message))
            emptyList()
        } catch (e: com.jed.optima.android.exception.ExternalDataException) {
            widget.showWarning(e.message)
            emptyList()
        }
    }
}
