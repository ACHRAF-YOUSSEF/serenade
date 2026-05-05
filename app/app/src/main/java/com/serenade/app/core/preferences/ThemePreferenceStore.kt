package com.serenade.app.core.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.serenade.app.ui.theme.SerenadeThemeChoice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "serenade_theme")
private val ThemeKey = stringPreferencesKey("selected_theme")

class ThemePreferenceStore(private val context: Context) {
    val selectedTheme: Flow<SerenadeThemeChoice> = context.themeDataStore.data.map { prefs ->
        val key = prefs[ThemeKey]
        SerenadeThemeChoice.entries.firstOrNull { it.storageKey == key }
            ?: SerenadeThemeChoice.Midnight
    }

    suspend fun setTheme(choice: SerenadeThemeChoice) {
        context.themeDataStore.edit { prefs ->
            prefs[ThemeKey] = choice.storageKey
        }
    }
}
