package com.world.clock.datastore

import android.annotation.SuppressLint
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.world.clock.data.appContext
import com.world.clock.utils.dataStoreFileName
import okio.Path.Companion.toPath

@SuppressLint("SuspiciousIndentation")
actual fun createDataStore(): DataStore<Preferences> {
    val context = appContext
        return PreferenceDataStoreFactory.createWithPath {
            context.filesDir.resolve(dataStoreFileName).absolutePath.toPath()
        }
}