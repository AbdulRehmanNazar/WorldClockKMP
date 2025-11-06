package com.world.clock.data.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import com.world.clock.data.dao.FavouriteTimeZoneDao
import com.world.clock.data.entity.FavouriteTimeZone


@Database(
    entities = [FavouriteTimeZone::class],
    version = 1
)
@ConstructedBy(WorldClockDatabaseConstructor::class)

abstract class WorldClockDatabase: RoomDatabase() {
    abstract fun favouriteTimeZoneDao(): FavouriteTimeZoneDao
}
// Add this constructor class
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object WorldClockDatabaseConstructor : RoomDatabaseConstructor<WorldClockDatabase> {
    override fun initialize(): WorldClockDatabase
}
