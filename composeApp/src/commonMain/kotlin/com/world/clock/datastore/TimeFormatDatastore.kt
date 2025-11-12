package com.world.clock.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


object TimeFormatDatastore {
    private val dataStore: DataStore<Preferences> by lazy { createDataStore() }
    private val IS_24_HOUR = booleanPreferencesKey("is_24_hour")
    private val IS_DATE = booleanPreferencesKey("is_date")

    fun is24HourFlow(): Flow<Boolean> = dataStore.data.map {
        it[IS_24_HOUR] ?: false
    }
    fun isDateFlow(): Flow<Boolean> = dataStore.data.map {
        it[IS_DATE] ?: false
    }

    suspend fun set24Hour(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_24_HOUR] = enabled
        }
    }

    suspend fun setDate(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[IS_DATE] = enabled
        }
    }


}

