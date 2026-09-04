# R8 rules for release builds. Room, Hilt, Retrofit and Coil ship their own consumer
# rules inside their AARs, so nothing below duplicates those. This file only adds what
# those libraries can't infer on their own, plus a few tweaks to make a decompiled
# release build harder to read/re-sign.

# --- kotlinx.serialization ---
# The Retrofit converter looks up generated `$$serializer` companions and `serializer()`
# factory functions reflectively; without these keeps R8 renames/removes them and every
# API response fails to parse at runtime instead of at compile time.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.ankitt.pokedex.**$$serializer { *; }
-keepclassmembers class com.ankitt.pokedex.** {
    *** Companion;
}
-keepclasseswithmembers class com.ankitt.pokedex.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Anti-tamper / anti-reverse-engineering hardening ---
# Flatten every obfuscated class into a single unnamed package instead of preserving
# the original package tree, and let R8 widen access modifiers where it helps inline
# and merge classes. Together these destroy most of the structural/navigational
# information a decompiler (jadx, etc.) relies on, on top of name obfuscation alone.
-repackageclasses ''
-allowaccessmodification

# Strip verbose/debug/info logcat output from release so response bodies, DB paths and
# internal state aren't sitting in the device log for anyone to `adb logcat`.
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Keep enough of the stack trace to symbolicate crashes with the mapping file, but
# replace the real source file name so it doesn't leak original file/class layout.
-keepattributes SourceFile, LineNumberTable
-renamesourcefileattribute SourceFile
