package com.jed.optima.android.formentry.questions

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.measure.Measure
import org.javarosa.xpath.parser.XPathSyntaxException
import com.jed.optima.android.javarosawrapper.FormController
import java.io.FileNotFoundException

object SelectChoiceUtils {

    @JvmStatic
    @Throws(FileNotFoundException::class, XPathSyntaxException::class, com.jed.optima.android.exception.ExternalDataException::class)
    fun loadSelectChoices(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        Measure.log("LoadSelectChoices")

        return when {
            isFastExternalItemsetUsed(prompt) -> readFastExternalItems(prompt, formController)
            isSearchPulldataItemsetUsed(prompt) -> readSearchPulldataItems(prompt, formController)
            else -> prompt.selectChoices ?: emptyList()
        }
    }

    private fun isFastExternalItemsetUsed(prompt: FormEntryPrompt): Boolean {
        val questionDef = prompt.question
        return questionDef?.getAdditionalAttribute(null, "query") != null
    }

    private fun isSearchPulldataItemsetUsed(prompt: FormEntryPrompt): Boolean {
        return com.jed.optima.android.dynamicpreload.ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint) != null
    }

    @Throws(FileNotFoundException::class, XPathSyntaxException::class)
    private fun readFastExternalItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        return com.jed.optima.android.fastexternalitemset.ItemsetDao(com.jed.optima.android.fastexternalitemset.ItemsetDbAdapter())
            .getItems(prompt,
                com.jed.optima.android.fastexternalitemset.XPathParseTool(), formController)
    }

    @Throws(FileNotFoundException::class, com.jed.optima.android.exception.ExternalDataException::class)
    private fun readSearchPulldataItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        val xpathFuncExpr =
            com.jed.optima.android.dynamicpreload.ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint)
        return com.jed.optima.android.dynamicpreload.ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr, formController)
    }
}
