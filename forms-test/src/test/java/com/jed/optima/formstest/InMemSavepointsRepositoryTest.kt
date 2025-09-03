package com.jed.optima.formstest

import com.jed.optima.forms.savepoints.SavepointsRepository

class InMemSavepointsRepositoryTest : SavepointsRepositoryTest() {
    override fun buildSubject(cacheDirPath: String, instancesDirPath: String): SavepointsRepository {
        return InMemSavepointsRepository()
    }
}
