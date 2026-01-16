package com.yedc.android.widgets.interfaces

import android.content.Context
import com.yedc.androidshared.livedata.NonNullLiveData

interface Printer {
    fun parseAndPrint(
        htmlDocument: String,
        questionMediaManager: _root_ide_package_.com.yedc.android.utilities.QuestionMediaManager,
        context: Context
    )

    fun isLoading(): NonNullLiveData<Boolean>
}
