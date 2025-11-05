package com.world.clock.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favourite_zones")
data class FavouriteTimeZone(
    @PrimaryKey val id: String,
)