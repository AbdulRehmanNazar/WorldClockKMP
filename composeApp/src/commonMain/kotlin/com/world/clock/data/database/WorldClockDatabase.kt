package com.world.clock.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.world.clock.data.dao.FavouriteTimeZoneDao
import com.world.clock.data.entity.FavouriteTimeZone


@Database(
    entities = [FavouriteTimeZone::class],
    version = 1
)
abstract class WorldClockDatabase: RoomDatabase() {
    abstract fun favouriteTimeZoneDao(): FavouriteTimeZoneDao
}