package com.yedc.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import com.yedc.android.database.savepoints.DatabaseSavepointsRepository
import com.yedc.forms.savepoints.SavepointsRepository
import com.yedc.formstest.SavepointsRepositoryTest
import com.yedc.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class DatabaseSavepointsRepositoryTest : SavepointsRepositoryTest() {
    override fun buildSubject(cacheDirPath: String, instancesDirPath: String): SavepointsRepository {
        return DatabaseSavepointsRepository(
            ApplicationProvider.getApplicationContext(),
            TempFiles.createTempDir().absolutePath,
            cacheDirPath,
            instancesDirPath
        )
    }
}
