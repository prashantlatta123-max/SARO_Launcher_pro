package com.sarotech.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Receiver contract for package changes. The activity also refreshes on resume,
 * which is the reliable fallback if the process was not alive for the broadcast.
 */
class PackageChangeReceiver(
    private val onPackagesChanged: () -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_CHANGED,
            Intent.ACTION_PACKAGE_REPLACED -> onPackagesChanged()
        }
    }
}
