package com.sarotech.launcher

/**
 * JNI bridge. Native methods are kept by app/proguard-rules.pro.
 * The fallback permits UI development if a local build omitted native libs.
 * CI release builds must package the Rust shared library for every configured ABI.
 */
object NativeBridge {
    private val libraryLoaded: Boolean = try {
        System.loadLibrary("sarotech_android_bridge")
        true
    } catch (_: UnsatisfiedLinkError) {
        false
    } catch (_: SecurityException) {
        false
    }

    external fun nativeVersion(): String
    external fun nativeNormalize(input: String): String
    external fun nativeSort(input: Array<String>): Array<String>

    fun version(): String =
        if (libraryLoaded) runCatching { nativeVersion() }.getOrDefault("Rust core unavailable")
        else "Rust core unavailable"

    fun normalize(input: String): String =
        if (libraryLoaded) runCatching { nativeNormalize(input) }.getOrDefault(input.trim().lowercase())
        else input.trim().lowercase()

    fun sort(input: Array<String>): Array<String> =
        if (libraryLoaded) runCatching { nativeSort(input) }.getOrDefault(input.sortedBy { it.lowercase() })
        else input.sortedBy { it.lowercase() }
}
