package com.world.clock.di

import androidx.room.RoomDatabase
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.database.desktopDatabaseBuilder
import org.koin.dsl.module


val desktopDatabaseModule = module {
    single<RoomDatabase.Builder<WorldClockDatabase>> {
        desktopDatabaseBuilder()
    }
}
