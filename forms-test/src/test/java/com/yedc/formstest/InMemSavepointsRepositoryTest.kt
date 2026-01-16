package com.yedc.formstest

import com.yedc.forms.savepoints.SavepointsRepository

class InMemSavepointsRepositoryTest : SavepointsRepositoryTest() {
    override fun buildSubject(cacheDirPath: String, instancesDirPath: String): SavepointsRepository {
        return InMemSavepointsRepository()
    }
}
