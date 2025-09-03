package com.jed.optima.shared

import com.jed.optima.shared.Query.And
import com.jed.optima.shared.Query.NumericEq
import com.jed.optima.shared.Query.NumericNotEq
import com.jed.optima.shared.Query.Or
import com.jed.optima.shared.Query.StringEq
import com.jed.optima.shared.Query.StringNotEq

sealed class Query {
    data class StringEq(val column: String, val value: String) : Query()
    data class StringNotEq(val column: String, val value: String) : Query()
    data class NumericEq(val column: String, val value: Double) : Query()
    data class NumericNotEq(val column: String, val value: Double) : Query()
    data class And(val queryA: Query, val queryB: Query) : Query()
    data class Or(val queryA: Query, val queryB: Query) : Query()
}

fun Query.mapColumns(columnMapper: (String) -> String): Query {
    return when (this) {
        is StringEq -> StringEq(columnMapper(column), value)
        is StringNotEq -> StringNotEq(columnMapper(column), value)
        is NumericEq -> NumericEq(columnMapper(column), value)
        is NumericNotEq -> NumericNotEq(columnMapper(column), value)
        is And -> And(
            queryA.mapColumns(columnMapper),
            queryB.mapColumns(columnMapper)
        )
        is Or -> Or(
            queryA.mapColumns(columnMapper),
            queryB.mapColumns(columnMapper)
        )
    }
}
