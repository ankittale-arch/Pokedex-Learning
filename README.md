# PokeDex

A multi-module, offline-first Android skeleton (Compose + Hilt + Room) built as a learning
reference. It's intentionally minimal - one real feature pair (`home` list -> `details`
screen) wired end-to-end - so the architecture is easy to read in full rather than
feature-complete.

## Module graph

```
app                     wires every feature:* module together via one NavHost
feature:home            pokemon list screen (ViewModel -> core:data)
feature:details         pokemon detail screen (ViewModel -> core:data)

core:data               repositories - the single source of truth for the UI
core:network            Retrofit service, DTOs, ApiResponse error wrapping
core:database           Room database, entities, DAOs, entity<->domain mappers
core:datastore          DataStore-backed user preferences
core:model              plain domain data classes (Pokemon)
core:common             dispatcher qualifiers, small extensions
core:navigation         shared, type-safe navigation routes
core:designsystem       Compose theme + generic shared components
core:preview            sample data + a light/dark @Preview annotation
core:test               shared JUnit test utilities (MainDispatcherRule)

baselineprofile         macrobenchmark module that generates app/src/main/baselineProfiles
build-logic/convention  Gradle convention plugins - the only place SDK/Compose/Hilt setup lives
```

`feature:*` modules never depend on each other - `feature:details` doesn't know
`feature:home` exists, and vice versa. They talk to each other's screens only through
`core:navigation`'s `PokedexRoute` sealed type, and `app` is the only module that assembles
them into a NavHost. `core:data` depends on `core:network`/`core:database`/`core:datastore`
but never the other way around, and never on a feature or on `app` - that one-way arrow is
what keeps the data layer testable and reusable across features.

Every module's `build.gradle.kts` applies a `pokedex.android.*` convention plugin from
`build-logic/convention` instead of repeating `compileSdk`/`minSdk`/Compose/Hilt setup by
hand - see `build-logic/convention/src/main/kotlin/KotlinAndroid.kt` and
`AndroidCompose.kt` for the one place those numbers live. `feature:*` modules in particular
only need `plugins { id("pokedex.android.feature") }` and a `namespace`; that single plugin
already pulls in Compose, Hilt, and the `core:designsystem`/`core:navigation`/`core:data`/
`core:preview`/`core:common` dependencies every feature needs.

## The offline-first pattern

Every fetch in `core:data`'s `PokemonRepositoryImpl` follows the same shape (see
`PokemonRepositoryImpl.kt`): check Room first: if the cached row is present, emit it
straight from the database and never touch the network. If it's missing (or, for a
Pokémon's types, incomplete), call the Retrofit service, and only on success write the
result into Room. Either way, the flow's final emission is always a *re-read of Room*, not
the raw network response - so Room is the one source of truth the UI observes, and the
network's only job is seeding/refreshing that cache. A network failure calls the
repository's `onError` callback instead of throwing, since `core:network`'s `safeApiCall`
wraps every retrofit call in a sealed `ApiResponse` before it ever reaches `core:data`.

Each repository interface also has a `Fake` implementation living next to it in `core:data`
(no DB/network) for use in tests and Compose previews.

## CI/CD

Two GitHub Actions workflows live in `.github/workflows/`:

- **CI** (`ci.yml`) - runs on every push to `master` and every pull request: Spotless
  format check, unit tests, and a debug APK build/upload for smoke testing.
- **Release** (`release.yml`) - tag-triggered: pushing a tag matching `v*.*.*` (e.g.
  `v1.2.0`) builds a signed release APK + AAB and publishes them as a GitHub Release with
  auto-generated release notes. It can also be run manually via `workflow_dispatch` to
  produce a release or debug build without cutting a GitHub Release, which is useful for
  verifying a signed build before shipping it.

Signing the release build requires four repo secrets (`RELEASE_KEYSTORE_BASE64`,
`RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`); without them the
build falls back to debug signing so the workflow still succeeds but the output must never
be shipped.

