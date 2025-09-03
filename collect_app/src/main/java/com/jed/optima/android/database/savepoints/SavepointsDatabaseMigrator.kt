package com.jed.optima.android.database.savepoints

import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns._ID
import com.jed.optima.android.database.DatabaseConstants.SAVEPOINTS_TABLE_NAME
import com.jed.optima.android.database.savepoints.DatabaseSavepointsColumns.FORM_DB_ID
import com.jed.optima.android.database.savepoints.DatabaseSavepointsColumns.INSTANCE_DB_ID
import com.jed.optima.android.database.savepoints.DatabaseSavepointsColumns.INSTANCE_FILE_PATH
import com.jed.optima.android.database.savepoints.DatabaseSavepointsColumns.SAVEPOINT_FILE_PATH
import com.jed.optima.db.sqlite.MigrationListDatabaseMigrator

class SavepointsDatabaseMigrator(databaseVersion: Int) : MigrationListDatabaseMigrator(databaseVersion) {
    override fun createDbForVersion(db: SQLiteDatabase, version: Int) {
        if (version == 1) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS $SAVEPOINTS_TABLE_NAME (" +
                    "$_ID integer PRIMARY KEY, " +
                    "$FORM_DB_ID integer NOT NULL, " +
                    "$INSTANCE_DB_ID integer, " +
                    "$SAVEPOINT_FILE_PATH text NOT NULL, " +
                    "$INSTANCE_FILE_PATH text NOT NULL);"
            )
        }
    }
}
