package com.world.clock.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.world.clock.data.entity.FavouriteTimeZone
import kotlinx.coroutines.flow.Flow


@Dao
interface FavouriteTimeZoneDao{
    @Query("SELECT * FROM favourite_zones")
    suspend fun getAllTimeZones(): List<FavouriteTimeZone>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(zone: FavouriteTimeZone)

    @Delete
    suspend fun delete(zone: FavouriteTimeZone)
    @Query("UPDATE favourite_zones SET name = :newName WHERE dbId = :id")
    suspend fun updateName(id: Long, newName: String)

}
