package com.world.clock.di

import androidx.room.RoomDatabase
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.database.iosDatabaseBuilder
import org.koin.dsl.module


val iosDatabaseModule = module {
    single<RoomDatabase.Builder<WorldClockDatabase>> {
        iosDatabaseBuilder()
    }
}