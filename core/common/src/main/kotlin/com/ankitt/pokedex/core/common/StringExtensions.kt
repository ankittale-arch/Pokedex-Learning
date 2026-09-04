package com.ankitt.pokedex.core.common

/** e.g. "great-tusk" -> "Great Tusk", to display API slug names in the UI. */
fun String.capitalizeWords(): String =
    split("-").joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
