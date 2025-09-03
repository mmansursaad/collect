package com.jed.optima.projects

import com.jed.optima.shared.strings.UUIDGenerator
import java.util.function.Supplier

class InMemProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator())
    }

    override fun buildSubject(clock: Supplier<Long>): ProjectsRepository {
        return InMemProjectsRepository(UUIDGenerator(), clock)
    }
}
