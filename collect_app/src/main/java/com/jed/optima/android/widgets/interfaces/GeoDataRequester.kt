package com.jed.optima.android.widgets.interfaces

import org.javarosa.form.api.FormEntryPrompt

interface GeoDataRequester {
    fun requestGeoPoint(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: com.jed.optima.android.widgets.utilities.WaitingForDataRegistry
    )

    fun requestGeoShape(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: com.jed.optima.android.widgets.utilities.WaitingForDataRegistry
    )

    fun requestGeoTrace(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: com.jed.optima.android.widgets.utilities.WaitingForDataRegistry
    )
}
