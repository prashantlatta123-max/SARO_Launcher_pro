package com.sarotech.launcher

import org.junit.Assert.assertEquals
import org.junit.Test

class NativeBridgeFallbackTest {
    @Test
    fun normalizeFallbackHasExpectedBehavior() {
        assertEquals("camera", NativeBridge.normalize("  CAMERA "))
    }

    @Test
    fun sortFallbackIsCaseInsensitive() {
        assertEquals(
            listOf("Alpha", "beta"),
            NativeBridge.sort(arrayOf("beta", "Alpha")).toList()
        )
    }
}
