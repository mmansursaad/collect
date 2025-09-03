package com.jed.optima.formstest

import java.io.File
import java.util.function.Supplier

class InMemInstancesRepositoryTest : InstancesRepositoryTest() {
    override val instancesDir: File = createTempDir()

    override fun buildSubject(): _root_ide_package_.com.jed.optima.forms.instances.InstancesRepository {
        return _root_ide_package_.com.jed.optima.formstest.InMemInstancesRepository { System.currentTimeMillis() }
    }

    override fun buildSubject(clock: Supplier<Long>): _root_ide_package_.com.jed.optima.forms.instances.InstancesRepository {
        return _root_ide_package_.com.jed.optima.formstest.InMemInstancesRepository(clock)
    }
}
