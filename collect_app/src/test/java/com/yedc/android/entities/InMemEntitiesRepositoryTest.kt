package com.yedc.android.entities

import com.yedc.entities.storage.EntitiesRepository
import com.yedc.entities.storage.InMemEntitiesRepository

class InMemEntitiesRepositoryTest : EntitiesRepositoryTest() {

    override fun buildSubject(clock: () -> Long): EntitiesRepository {
        return InMemEntitiesRepository(clock)
    }
}
