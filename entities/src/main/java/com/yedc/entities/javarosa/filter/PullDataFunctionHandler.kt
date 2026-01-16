package com.yedc.entities.javarosa.filter

import org.javarosa.core.model.condition.EvaluationContext
import org.javarosa.core.model.condition.IFunctionHandler
import org.javarosa.xpath.expr.XPathFuncExpr
import com.yedc.entities.javarosa.intance.LocalEntitiesInstanceAdapter
import com.yedc.entities.storage.EntitiesRepository
import com.yedc.entities.storage.QueryException
import com.yedc.shared.Query

class PullDataFunctionHandler(
    entitiesRepository: EntitiesRepository,
    private val fallback: IFunctionHandler? = null
) : IFunctionHandler {

    private val instanceAdapter = LocalEntitiesInstanceAdapter(entitiesRepository)

    override fun getName(): String {
        return NAME
    }

    override fun getPrototypes(): List<Array<Class<Any>>> {
        return emptyList()
    }

    override fun rawArgs(): Boolean {
        return true
    }

    override fun realTime(): Boolean {
        return false
    }

    override fun eval(args: Array<Any>, ec: EvaluationContext): Any {
        val instanceId = XPathFuncExpr.toString(args[0])

        return if (instanceAdapter.supportsInstance(instanceId)) {
            val child = XPathFuncExpr.toString(args[1])
            val filterChild = XPathFuncExpr.toString(args[2])
            val filterValue = XPathFuncExpr.toString(args[3])

            try {
                instanceAdapter.query(instanceId, Query.StringEq(filterChild, filterValue)).firstOrNull()
                    ?.getFirstChild(child)?.value?.value ?: ""
            } catch (e: QueryException) {
                ""
            }
        } else {
            fallback?.eval(args, ec) ?: ""
        }
    }

    companion object {
        private const val NAME = "pulldata"
    }
}
