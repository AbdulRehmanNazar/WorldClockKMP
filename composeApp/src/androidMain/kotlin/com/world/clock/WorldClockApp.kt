package com.world.clock

import android.app.Application
import com.world.clock.data.initAndroidContext

class WorldClockApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initAndroidContext(this)
    }

}