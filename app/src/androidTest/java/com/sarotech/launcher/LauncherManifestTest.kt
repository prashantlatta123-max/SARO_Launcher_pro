package com.sarotech.launcher

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LauncherManifestTest {
    @Test
    fun appHasExpectedPackageName() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.sarotech.launcher", context.packageName)
    }

    @Test
    fun launcherActivityCanBeResolved() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = android.content.Intent(android.content.Intent.ACTION_MAIN).apply {
            addCategory(android.content.Intent.CATEGORY_HOME)
            setPackage(context.packageName)
        }
        val resolved = context.packageManager.resolveActivity(intent, 0)
        assertNotNull(resolved)
    }
}
