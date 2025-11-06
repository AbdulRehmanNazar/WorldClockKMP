package com.world.clock.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.world.clock.utils.dataStoreFileName
import okio.Path.Companion.toPath
import java.io.File

actual fun createDataStore(): DataStore<Preferences> {
    val file = File(System.getProperty("user.home"), dataStoreFileName)
    return PreferenceDataStoreFactory.createWithPath { file.absolutePath.toPath() }
}