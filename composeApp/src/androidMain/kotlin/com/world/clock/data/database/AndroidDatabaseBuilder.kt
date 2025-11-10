package com.world.clock.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<WorldClockDatabase> {

    val dbFile = context.getDatabasePath(WorldClockDatabase.DB_NAME)
    return Room.databaseBuilder(
        context = context.applicationContext,
        name = dbFile.absolutePath
    )
}