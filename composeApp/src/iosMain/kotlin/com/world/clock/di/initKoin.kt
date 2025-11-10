package com.world.clock.di

import org.koin.core.context.startKoin


fun initKoin() {
    startKoin {
        modules(iosDatabaseModule, sharedModule)
    }
}