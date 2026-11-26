
package com.sarotech.launcher

/**
 * JNI bridge for the independent SAROTECH Rust core.
 * Safe Kotlin fallbacks are used when native libraries are unavailable.
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

    fun version(): String {
        return if (libraryLoaded) {
            runCatching { nativeVersion() }
                .getOrDefault("Rust core unavailable")
        } else {
            "Rust core unavailable"
        }
    }

    fun normalize(input: String): String {
        return if (libraryLoaded) {
            runCatching { nativeNormalize(input) }
                .getOrDefault(input.trim().lowercase())
        } else {
            input.trim().lowercase()
        }
    }

    fun sort(input: Array<String>): Array<String> {
        return if (libraryLoaded) {
            runCatching { nativeSort(input) }
                .getOrElse {
                    input.sortedBy { value -> value.lowercase() }.toTypedArray()
                }
        } else {
            input.sortedBy { value -> value.lowercase() }.toTypedArray()
        }
    }
}
