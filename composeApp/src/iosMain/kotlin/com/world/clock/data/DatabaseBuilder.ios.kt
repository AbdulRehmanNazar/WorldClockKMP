package com.world.clock.data

import androidx.room.Room
import com.world.clock.data.database.WorldClockDatabase
import platform.Foundation.NSHomeDirectory

actual fun getDatabaseBuilder(): WorldClockDatabase {
    val dbFilePath = NSHomeDirectory() + "/world_clock.db"
    return Room.databaseBuilder<WorldClockDatabase>(
        name = dbFilePath
    ).build()}