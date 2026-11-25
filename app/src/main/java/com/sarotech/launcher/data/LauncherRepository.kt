package com.sarotech.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LauncherRepository(private val context: Context) {
    suspend fun loadLaunchableApps(): List<AppEntry> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val results = if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, 0)
        }

        results.mapNotNull { info ->
            val activity = info.activityInfo ?: return@mapNotNull null
            val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            if (label.isBlank()) return@mapNotNull null
            AppEntry(
                label = label,
                packageName = activity.packageName,
                activityName = activity.name,
                isSystemApp = (activity.applicationInfo.flags and
                    android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0
            )
        }.distinctBy { it.stableId }
            .sortedBy { it.label.lowercase() }
    }

    fun launch(app: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            component = ComponentName(app.packageName, app.activityName)
            addCategory(Intent.CATEGORY_LAUNCHER)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }
}
