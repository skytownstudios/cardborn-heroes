package com.skytownstudios.cardbornheroes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore("app_preferences")

class AppPreferencesRepository(private val context: Context) {
    private val darkModeKey = booleanPreferencesKey("dark_mode")

    suspend fun loadDarkMode(): Boolean =
        context.appPreferencesDataStore.data
            .map { it[darkModeKey] ?: true }
            .first()

    suspend fun setDarkMode(enabled: Boolean) {
        context.appPreferencesDataStore.edit { it[darkModeKey] = enabled }
    }
}
