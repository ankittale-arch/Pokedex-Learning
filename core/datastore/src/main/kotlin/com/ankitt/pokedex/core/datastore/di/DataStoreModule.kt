package com.ankitt.pokedex.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

private const val PREFERENCES_NAME = "pokedex_user_preferences"

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providesPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> =
        // DataStore needs its own long-lived scope, independent of any one caller's lifecycle,
        // since a write started from a ViewModel must still finish even if that ViewModel is
        // cleared mid-write. SupervisorJob so a failure in one read/edit can't cancel the
        // scope and take down every other in-flight DataStore operation with it.
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        ) {
            context.preferencesDataStoreFile(PREFERENCES_NAME)
        }
}
