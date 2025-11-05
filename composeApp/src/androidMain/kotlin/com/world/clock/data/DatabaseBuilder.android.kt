package com.world.clock.data

import android.content.Context
import androidx.room.Room
import com.world.clock.data.database.WorldClockDatabase



lateinit var appContext: Context

fun initAndroidContext(context: Context) {
    appContext = context
}

actual fun getDatabaseBuilder(): WorldClockDatabase {
    return Room.databaseBuilder(
        appContext,
        WorldClockDatabase::class.java,
        "world_clock.db"
    ).build()
}