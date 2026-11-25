# JNI entry points are looked up by their exact JVM names from Rust.
-keep class com.sarotech.launcher.NativeBridge { *; }
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Preserve Kotlin metadata used by reflection and diagnostics.
-keep class kotlin.Metadata { *; }

# Keep Room generated database implementations when they are added.
-keep class * extends androidx.room.RoomDatabase
