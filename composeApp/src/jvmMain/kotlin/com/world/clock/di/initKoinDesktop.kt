package com.world.clock.di

import org.koin.core.context.startKoin


fun initKoinDesktop() = startKoin {
    modules(
        desktopDatabaseModule,
        sharedModule
    )

}