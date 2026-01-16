package com.yedc.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import com.yedc.formstest.InstancesRepositoryTest
import java.io.File
import java.util.function.Supplier

@RunWith(AndroidJUnit4::class)
class DatabaseInstancesRepositoryTest : InstancesRepositoryTest() {
    private val dbDir = createTempDir()

    override val instancesDir: File = createTempDir()

    override fun buildSubject(): com.yedc.forms.instances.InstancesRepository {
        return _root_ide_package_.com.yedc.android.database.instances.DatabaseInstancesRepository(
            ApplicationProvider.getApplicationContext(),
            dbDir.absolutePath,
            instancesDir.absolutePath
        ) { System.currentTimeMillis() }
    }

    override fun buildSubject(clock: Supplier<Long>): com.yedc.forms.instances.InstancesRepository {
        return _root_ide_package_.com.yedc.android.database.instances.DatabaseInstancesRepository(
            ApplicationProvider.getApplicationContext(),
            dbDir.absolutePath,
            instancesDir.absolutePath,
            clock
        )
    }
}
