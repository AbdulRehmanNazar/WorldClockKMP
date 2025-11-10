package com.world.clock.datastore

import android.annotation.SuppressLint
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.world.clock.utils.dataStoreFileName
import okio.Path.Companion.toPath


lateinit var appContext: Context

fun appContext(context: Context){
    appContext = context
}
@SuppressLint("SuspiciousIndentation")
actual fun createDataStore(): DataStore<Preferences> {
    val context = appContext
        return PreferenceDataStoreFactory.createWithPath {
            context.filesDir.resolve(dataStoreFileName).absolutePath.toPath()
        }
}