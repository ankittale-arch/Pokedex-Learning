package com.ankitt.pokedex.core.network.di

import com.ankitt.pokedex.core.network.BuildConfig
import com.ankitt.pokedex.core.network.PokeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

private const val POKE_API_BASE_URL = "https://pokeapi.co/api/v2/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun providesJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun providesOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    // BASIC still logs URLs/status/timing in release, which is enough to
                    // debug connectivity issues without dumping full request/response
                    // bodies (or headers) to logcat on every user's device.
                    level = if (BuildConfig.DEBUG) {
                        HttpLoggingInterceptor.Level.BODY
                    } else {
                        HttpLoggingInterceptor.Level.BASIC
                    }
                },
            )
            .build()

    @Provides
    @Singleton
    fun providesRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(POKE_API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun providesPokeApiService(retrofit: Retrofit): PokeApiService =
        retrofit.create(PokeApiService::class.java)
}
