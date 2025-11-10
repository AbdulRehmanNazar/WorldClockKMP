package com.world.clock.di

import androidx.room.RoomDatabase
import com.world.clock.data.database.CreateDataBase
import com.world.clock.data.database.WorldClockDatabase
import com.world.clock.data.database.androidDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    single<RoomDatabase.Builder<WorldClockDatabase>> {
        androidDatabaseBuilder(androidContext())
    }
}
