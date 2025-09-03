package com.jed.optima.android.database.savepoints

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import androidx.core.database.getLongOrNull
import com.jed.optima.android.database.DatabaseConstants.SAVEPOINTS_DATABASE_NAME
import com.jed.optima.android.database.DatabaseConstants.SAVEPOINTS_DATABASE_VERSION
import com.jed.optima.android.database.DatabaseConstants.SAVEPOINTS_TABLE_NAME
import com.jed.optima.android.database.savepoints.DatabaseSavepointsColumns.FORM_DB_ID
import com.jed.optima.android.database.savepoints.DatabaseSavepointsColumns.INSTANCE_DB_ID
import com.jed.optima.androidshared.utils.PathUtils.getAbsoluteFilePath
import com.jed.optima.db.sqlite.CursorExt.foldAndClose
import com.jed.optima.db.sqlite.DatabaseConnection
import com.jed.optima.db.sqlite.SQLiteDatabaseExt.delete
import com.jed.optima.db.sqlite.SQLiteDatabaseExt.query
import com.jed.optima.forms.savepoints.Savepoint
import com.jed.optima.forms.savepoints.SavepointsRepository
import com.jed.optima.shared.PathUtils
import java.io.File

class DatabaseSavepointsRepository(
    context: Context,
    dbPath: String,
    private val cachePath: String,
    private val instancesPath: String
) : SavepointsRepository {
    private val databaseConnection: DatabaseConnection = DatabaseConnection(
        context,
        dbPath,
        SAVEPOINTS_DATABASE_NAME,
        SavepointsDatabaseMigrator(SAVEPOINTS_DATABASE_VERSION),
        SAVEPOINTS_DATABASE_VERSION,
        true
    )

    override fun get(formDbId: Long, instanceDbId: Long?): Savepoint? {
        val cursor = if (instanceDbId == null) {
            databaseConnection.readableDatabase.query(
                SAVEPOINTS_TABLE_NAME,
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID IS NULL",
                arrayOf(formDbId.toString())
            )
        } else {
            databaseConnection.readableDatabase.query(
                SAVEPOINTS_TABLE_NAME,
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID=?",
                arrayOf(formDbId.toString(), instanceDbId.toString())
            )
        }
        val savepoints = getSavepointsFromCursor(cursor)

        return if (savepoints.isNotEmpty()) savepoints[0] else null
    }

    override fun getAll(): List<Savepoint> {
        return getSavepointsFromCursor(
            databaseConnection.readableDatabase.query(SAVEPOINTS_TABLE_NAME)
        )
    }

    override fun save(savepoint: Savepoint) {
        if (get(savepoint.formDbId, savepoint.instanceDbId) != null) {
            return
        }

        val values = getValuesFromSavepoint(savepoint, cachePath, instancesPath)

        databaseConnection
            .writableDatabase
            .insertOrThrow(SAVEPOINTS_TABLE_NAME, null, values)
    }

    override fun delete(formDbId: Long, instanceDbId: Long?) {
        val savepoint = get(formDbId, instanceDbId) ?: return

        val (selection, selectionArgs) = if (savepoint.instanceDbId == null) {
            Pair(
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID IS NULL",
                arrayOf(savepoint.formDbId.toString())
            )
        } else {
            Pair(
                "$FORM_DB_ID=? AND $INSTANCE_DB_ID=?",
                arrayOf(savepoint.formDbId.toString(), savepoint.instanceDbId.toString())
            )
        }

        databaseConnection
            .writableDatabase
            .delete(SAVEPOINTS_TABLE_NAME, selection, selectionArgs)

        File(savepoint.savepointFilePath).delete()
    }

    override fun deleteAll() {
        getAll().forEach {
            File(it.savepointFilePath).delete()
        }

        databaseConnection
            .writableDatabase
            .delete(SAVEPOINTS_TABLE_NAME)
    }

    private fun getSavepointsFromCursor(cursor: Cursor?): List<Savepoint> {
        return cursor?.foldAndClose(emptyList()) { list, cursor ->
            list + getSavepointFromCurrentCursorPosition(cursor, cachePath, instancesPath)
        } ?: emptyList()
    }

    private fun getSavepointFromCurrentCursorPosition(
        cursor: Cursor,
        cachePath: String,
        instancesPath: String
    ): Savepoint {
        val formDbIdColumnIndex = cursor.getColumnIndex(FORM_DB_ID)
        val instanceDbIdColumnIndex = cursor.getColumnIndex(INSTANCE_DB_ID)
        val savepointFilePathColumnIndex = cursor.getColumnIndex(DatabaseSavepointsColumns.SAVEPOINT_FILE_PATH)
        val instanceDirPathColumnIndex = cursor.getColumnIndex(DatabaseSavepointsColumns.INSTANCE_FILE_PATH)

        return Savepoint(
            cursor.getLong(formDbIdColumnIndex),
            cursor.getLongOrNull(instanceDbIdColumnIndex),
            getAbsoluteFilePath(
                cachePath,
                cursor.getString(savepointFilePathColumnIndex)
            ),
            getAbsoluteFilePath(
                instancesPath,
                cursor.getString(instanceDirPathColumnIndex)
            )
        )
    }

    private fun getValuesFromSavepoint(savepoint: Savepoint, cachePath: String, instancesPath: String): ContentValues {
        return ContentValues().apply {
            put(FORM_DB_ID, savepoint.formDbId)
            put(INSTANCE_DB_ID, savepoint.instanceDbId)
            put(DatabaseSavepointsColumns.SAVEPOINT_FILE_PATH, PathUtils.getRelativeFilePath(cachePath, savepoint.savepointFilePath))
            put(DatabaseSavepointsColumns.INSTANCE_FILE_PATH, PathUtils.getRelativeFilePath(instancesPath, savepoint.instanceFilePath))
        }
    }
}
