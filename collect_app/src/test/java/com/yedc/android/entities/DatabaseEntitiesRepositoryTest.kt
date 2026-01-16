package com.yedc.android.entities

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import com.yedc.android.database.entities.DatabaseEntitiesRepository
import com.yedc.android.entities.support.EntitySameAsMatcher.Companion.sameEntityAs
import com.yedc.entities.storage.EntitiesRepository
import com.yedc.entities.storage.Entity
import com.yedc.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class DatabaseEntitiesRepositoryTest : EntitiesRepositoryTest() {
    override fun buildSubject(clock: () -> Long): EntitiesRepository {
        return DatabaseEntitiesRepository(
            ApplicationProvider.getApplicationContext(),
            TempFiles.createTempDir().absolutePath,
            clock
        )
    }

    @Test
    fun `#save supports properties with db column names saving new entities and updating existing ones`() {
        val repository = buildSubject()
        val entity = Entity.New(
            "1",
            "One",
            properties = listOf(Pair("_id", "value"), Pair("version", "value"))
        )

        repository.save("things", entity)
        val savedEntity = repository.query("things")[0]
        assertThat(savedEntity, sameEntityAs(entity))

        repository.save("things", savedEntity)
        assertThat(repository.query("things")[0], sameEntityAs(savedEntity))
    }
}
