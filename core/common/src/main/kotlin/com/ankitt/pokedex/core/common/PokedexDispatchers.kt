package com.ankitt.pokedex.core.common

import javax.inject.Qualifier

enum class PokedexDispatchers {
    Default,
    IO,
}

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class Dispatcher(val pokedexDispatcher: PokedexDispatchers)
