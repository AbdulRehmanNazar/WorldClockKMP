package com.world.clock.di

import com.world.clock.data.database.CreateDataBase
import com.world.clock.data.database.WorldClockDatabase
import org.koin.dsl.module


val sharedModule = module {
    single<WorldClockDatabase> {
        CreateDataBase(get()).getDataBase()
    }


}