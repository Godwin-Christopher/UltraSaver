package com.nothing.ultrasaver.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ultra_saver_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val KEY_IS_ULTRA_ACTIVE = booleanPreferencesKey("is_ultra_active")
        val KEY_WHITELISTED_APPS = stringSetPreferencesKey("whitelisted_apps")
        val KEY_AUTO_ENABLE_AT_10 = booleanPreferencesKey("auto_enable_at_10")
    }

    val isUltraActive: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_IS_ULTRA_ACTIVE] ?: false
    }

    val whitelistedApps: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[KEY_WHITELISTED_APPS] ?: emptySet()
    }

    suspend fun setUltraActive(active: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_ULTRA_ACTIVE] = active
        }
    }

    suspend fun updateWhitelist(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WHITELISTED_APPS] = packages
        }
    }
}
