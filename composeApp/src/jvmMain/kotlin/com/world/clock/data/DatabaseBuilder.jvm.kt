package com.world.clock.data

import androidx.room.Room
import com.world.clock.data.database.WorldClockDatabase
import java.io.File

actual fun getDatabaseBuilder(): WorldClockDatabase {
    val dbFile = File(System.getProperty("user.home"), "world_clock.db")
    return Room.databaseBuilder<WorldClockDatabase>(
        name = dbFile.absolutePath
    ).build()
}