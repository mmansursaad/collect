package com.jed.optima.android.widgets.interfaces

import android.content.Context
import com.jed.optima.androidshared.livedata.NonNullLiveData

interface Printer {
    fun parseAndPrint(
        htmlDocument: String,
        questionMediaManager: com.jed.optima.android.utilities.QuestionMediaManager,
        context: Context
    )

    fun isLoading(): NonNullLiveData<Boolean>
}
