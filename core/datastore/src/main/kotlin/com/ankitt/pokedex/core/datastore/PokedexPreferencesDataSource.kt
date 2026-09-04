package com.ankitt.pokedex.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val SORT_ALPHABETICALLY = booleanPreferencesKey("sort_alphabetically")

class PokedexPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(sortAlphabetically = prefs[SORT_ALPHABETICALLY] ?: false)
    }

    suspend fun setSortAlphabetically(sortAlphabetically: Boolean) {
        dataStore.edit { it[SORT_ALPHABETICALLY] = sortAlphabetically }
    }
}
