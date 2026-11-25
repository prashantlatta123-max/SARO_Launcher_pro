package com.sarotech.launcher.data

data class AppEntry(
    val label: String,
    val packageName: String,
    val activityName: String,
    val isSystemApp: Boolean
) {
    val stableId: String get() = "$packageName/$activityName"
}
