package com.jed.optima.errors

import java.io.Serializable

data class ErrorItem(
    val title: String,
    val secondaryText: String,
    val supportingText: String
) : Serializable
