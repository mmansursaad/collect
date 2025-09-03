package com.jed.optima.android.entities

import com.jed.optima.entities.storage.EntitiesRepository
import com.jed.optima.entities.storage.InMemEntitiesRepository

class InMemEntitiesRepositoryTest : EntitiesRepositoryTest() {

    override fun buildSubject(clock: () -> Long): EntitiesRepository {
        return InMemEntitiesRepository(clock)
    }
}
