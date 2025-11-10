package com.world.clock.data.database

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import java.io.File


fun desktopDatabaseBuilder(): RoomDatabase.Builder<WorldClockDatabase> {

    val dbFile = File(System.getProperty("java.io.tmpdir"), WorldClockDatabase.DB_NAME)
    return Room.databaseBuilder(
        name = dbFile.absolutePath
    )
}