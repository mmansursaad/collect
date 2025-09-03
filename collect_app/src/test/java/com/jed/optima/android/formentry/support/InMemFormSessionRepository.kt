package com.jed.optima.android.formentry.support

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.jed.optima.android.formentry.FormSession
import com.jed.optima.android.formentry.FormSessionRepository
import com.jed.optima.android.javarosawrapper.FormController
import com.jed.optima.shared.strings.UUIDGenerator

class InMemFormSessionRepository : FormSessionRepository {

    private val map = mutableMapOf<String, MutableLiveData<FormSession>>()

    override fun create(): String {
        return UUIDGenerator().generateUUID()
    }

    override fun get(id: String): LiveData<FormSession> {
        return getLiveData(id)
    }

    override fun set(id: String, formController: FormController, form: com.jed.optima.forms.Form, instance: com.jed.optima.forms.instances.Instance?) {
        getLiveData(id).value = FormSession(formController, form, instance)
    }

    override fun update(id: String, instance: com.jed.optima.forms.instances.Instance?) {
        val liveData = getLiveData(id)
        liveData.value?.let {
            liveData.value = it.copy(instance = instance)
        }
    }

    override fun clear(id: String) {
        getLiveData(id).value = null
        map.remove(id)
    }

    private fun getLiveData(id: String): MutableLiveData<FormSession> {
        return map.getOrPut(id) { MutableLiveData<FormSession>(null) }
    }
}
