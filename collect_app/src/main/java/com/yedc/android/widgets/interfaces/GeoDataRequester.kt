package com.yedc.android.widgets.interfaces

import org.javarosa.form.api.FormEntryPrompt

interface GeoDataRequester {
    fun requestGeoPoint(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
    )

    fun requestGeoShape(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
    )

    fun requestGeoTrace(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: _root_ide_package_.com.yedc.android.widgets.utilities.WaitingForDataRegistry
    )
}
