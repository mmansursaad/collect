package com.jed.optima.android.database

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.runner.RunWith
import com.jed.optima.android.database.savepoints.DatabaseSavepointsRepository
import com.jed.optima.forms.savepoints.SavepointsRepository
import com.jed.optima.formstest.SavepointsRepositoryTest
import com.jed.optima.shared.TempFiles

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
