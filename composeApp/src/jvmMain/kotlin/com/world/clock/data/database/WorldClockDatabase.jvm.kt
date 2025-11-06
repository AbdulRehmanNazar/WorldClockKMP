package com.world.clock.data.database

import androidx.room.RoomDatabaseConstructor
import com.world.clock.data.getDatabaseBuilder

@Suppress(names = ["NO_ACTUAL_FOR_EXPECT"])
actual object WorldClockDatabaseConstructor :
    RoomDatabaseConstructor<WorldClockDatabase> {
    actual override fun initialize(): WorldClockDatabase {
        return getDatabaseBuilder()
    }
}