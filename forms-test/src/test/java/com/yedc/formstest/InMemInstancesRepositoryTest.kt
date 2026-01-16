package com.yedc.formstest

import java.io.File
import java.util.function.Supplier

class InMemInstancesRepositoryTest : InstancesRepositoryTest() {
    override val instancesDir: File = createTempDir()

    override fun buildSubject(): _root_ide_package_.com.yedc.forms.instances.InstancesRepository {
        return _root_ide_package_.com.yedc.formstest.InMemInstancesRepository { System.currentTimeMillis() }
    }

    override fun buildSubject(clock: Supplier<Long>): _root_ide_package_.com.yedc.forms.instances.InstancesRepository {
        return _root_ide_package_.com.yedc.formstest.InMemInstancesRepository(clock)
    }
}
