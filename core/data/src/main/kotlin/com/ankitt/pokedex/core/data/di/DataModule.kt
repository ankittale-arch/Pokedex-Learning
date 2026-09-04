package com.ankitt.pokedex.core.data.di

import com.ankitt.pokedex.core.common.Dispatcher
import com.ankitt.pokedex.core.common.PokedexDispatchers
import com.ankitt.pokedex.core.data.repository.PokemonRepository
import com.ankitt.pokedex.core.data.repository.PokemonRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun bindsPokemonRepository(impl: PokemonRepositoryImpl): PokemonRepository

    companion object {
        @Provides
        @Dispatcher(PokedexDispatchers.IO)
        fun providesIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
    }
}
