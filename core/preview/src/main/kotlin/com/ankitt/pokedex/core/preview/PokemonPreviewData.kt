package com.ankitt.pokedex.core.preview

import com.ankitt.pokedex.core.model.Pokemon
import com.ankitt.pokedex.core.model.PokemonStat
import com.ankitt.pokedex.core.model.TypeEffectiveness

/** Sample data for Compose `@Preview`s only - never used by production code paths. */
object PokemonPreviewData {

    val bulbasaur = Pokemon(
        id = 1,
        name = "bulbasaur",
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png",
        types = listOf("grass", "poison"),
        stats = listOf(
            PokemonStat("hp", 45),
            PokemonStat("attack", 49),
            PokemonStat("defense", 49),
            PokemonStat("special-attack", 65),
            PokemonStat("special-defense", 65),
            PokemonStat("speed", 45),
        ),
        typeEffectiveness = TypeEffectiveness(
            weakTo = listOf("fire", "ice", "flying", "psychic"),
            resistantTo = listOf("water", "electric", "grass", "fighting", "fairy"),
        ),
    )

    val charmander = Pokemon(
        id = 4,
        name = "charmander",
        imageUrl = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png",
        types = listOf("fire"),
        stats = listOf(
            PokemonStat("hp", 39),
            PokemonStat("attack", 52),
            PokemonStat("defense", 43),
            PokemonStat("special-attack", 60),
            PokemonStat("special-defense", 50),
            PokemonStat("speed", 65),
        ),
        typeEffectiveness = TypeEffectiveness(
            weakTo = listOf("water", "ground", "rock"),
            resistantTo = listOf("fire", "grass", "ice", "bug", "steel", "fairy"),
        ),
    )

    val pokemonList = listOf(bulbasaur, charmander)
}
