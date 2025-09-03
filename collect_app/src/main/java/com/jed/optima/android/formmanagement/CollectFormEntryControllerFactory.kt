package com.jed.optima.android.formmanagement

import android.os.Handler
import android.os.Looper
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.FilterStrategy
import org.javarosa.core.model.instance.DataInstance
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.form.api.FormEntryController
import org.javarosa.form.api.FormEntryModel
import org.javarosa.xpath.expr.XPathExpression
import com.jed.optima.android.formmanagement.finalization.EditedFormFinalizationProcessor
import com.jed.optima.android.preferences.SettingsExt.getExperimentalOptIn
import com.jed.optima.android.tasks.FormLoaderTask.FormEntryControllerFactory
import com.jed.optima.androidshared.ui.ToastUtils
import com.jed.optima.entities.javarosa.filter.LocalEntitiesFilterStrategy
import com.jed.optima.entities.javarosa.filter.PullDataFunctionHandler
import com.jed.optima.entities.storage.EntitiesRepository
import com.jed.optima.settings.keys.ProjectKeys
import com.jed.optima.shared.settings.Settings
import java.io.File
import java.util.function.Supplier
import kotlin.system.measureTimeMillis

class CollectFormEntryControllerFactory(
    private val entitiesRepository: EntitiesRepository,
    private val settings: Settings
) :
    FormEntryControllerFactory {
    override fun create(formDef: FormDef, formMediaDir: File, instance: com.jed.optima.forms.instances.Instance?): FormEntryController {
        val externalDataManager = com.jed.optima.android.dynamicpreload.ExternalDataManagerImpl(
            formMediaDir
        ).also {
            com.jed.optima.android.application.Collect.getInstance().externalDataManager = it
        }

        return FormEntryController(FormEntryModel(formDef)).also {
            val externalDataHandlerPull =
                com.jed.optima.android.dynamicpreload.handler.ExternalDataHandlerPull(
                    externalDataManager
                )
            it.addFunctionHandler(
                PullDataFunctionHandler(
                    entitiesRepository,
                    externalDataHandlerPull
                )
            )
            it.addPostProcessor(com.jed.optima.entities.javarosa.finalization.EntityFormFinalizationProcessor())
            it.addPostProcessor(EditedFormFinalizationProcessor(instance))

            if (settings.getExperimentalOptIn(ProjectKeys.KEY_DEBUG_FILTERS)) {
                it.addFilterStrategy(LoggingFilterStrategy())
            }

            it.addFilterStrategy(LocalEntitiesFilterStrategy(entitiesRepository))
        }
    }
}

private class LoggingFilterStrategy : FilterStrategy {
    override fun filter(
        sourceInstance: DataInstance<*>,
        nodeSet: TreeReference,
        predicate: XPathExpression,
        children: MutableList<TreeReference>,
        evaluationContext: EvaluationContext,
        next: Supplier<MutableList<TreeReference>>
    ): MutableList<TreeReference> {
        val result: MutableList<TreeReference>
        val filterTime = measureTimeMillis { result = next.get() }
        Handler(Looper.getMainLooper()).post {
            ToastUtils.showShortToast("Filter took ${filterTime / 1000.0}s")
        }

        return result
    }
}
