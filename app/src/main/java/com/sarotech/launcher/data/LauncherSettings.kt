package com.sarotech.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.launcherDataStore by preferencesDataStore(name = "launcher_settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class LauncherSettings(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val hapticsKey = booleanPreferencesKey("haptics_enabled")

    val themeMode: Flow<ThemeMode> = context.launcherDataStore.data.map { preferences ->
        runCatching { ThemeMode.valueOf(preferences[themeKey] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    val hapticsEnabled: Flow<Boolean> =
        context.launcherDataStore.data.map { it[hapticsKey] ?: true }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.launcherDataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.launcherDataStore.edit { it[hapticsKey] = enabled }
    }
}
