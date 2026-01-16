package com.yedc.android.formentry.questions

import org.javarosa.core.model.SelectChoice
import org.javarosa.form.api.FormEntryPrompt
import org.javarosa.measure.Measure
import org.javarosa.xpath.parser.XPathSyntaxException
import com.yedc.android.javarosawrapper.FormController
import java.io.FileNotFoundException

object SelectChoiceUtils {

    @JvmStatic
    @Throws(FileNotFoundException::class, XPathSyntaxException::class, _root_ide_package_.com.yedc.android.exception.ExternalDataException::class)
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
        return _root_ide_package_.com.yedc.android.dynamicpreload.ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint) != null
    }

    @Throws(FileNotFoundException::class, XPathSyntaxException::class)
    private fun readFastExternalItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        return _root_ide_package_.com.yedc.android.fastexternalitemset.ItemsetDao(
            _root_ide_package_.com.yedc.android.fastexternalitemset.ItemsetDbAdapter()
        )
            .getItems(prompt,
                _root_ide_package_.com.yedc.android.fastexternalitemset.XPathParseTool(), formController)
    }

    @Throws(FileNotFoundException::class, _root_ide_package_.com.yedc.android.exception.ExternalDataException::class)
    private fun readSearchPulldataItems(prompt: FormEntryPrompt, formController: FormController): List<SelectChoice> {
        // SurveyCTO-added support for dynamic select content (from .csv files)
        val xpathFuncExpr =
            _root_ide_package_.com.yedc.android.dynamicpreload.ExternalDataUtil.getSearchXPathExpression(prompt.appearanceHint)
        return _root_ide_package_.com.yedc.android.dynamicpreload.ExternalDataUtil.populateExternalChoices(prompt, xpathFuncExpr, formController)
    }
}
