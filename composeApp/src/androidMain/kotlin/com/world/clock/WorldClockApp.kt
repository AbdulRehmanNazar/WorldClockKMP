package com.world.clock

import android.app.Application
import com.world.clock.datastore.appContext
import com.world.clock.di.androidDatabaseModule
import com.world.clock.di.sharedModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class WorldClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext(this)
        startKoin {
            androidContext(this@WorldClockApp)
            modules(androidDatabaseModule, sharedModule)
        }
    }

}