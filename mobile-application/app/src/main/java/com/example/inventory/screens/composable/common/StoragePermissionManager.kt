package com.example.inventory.screens.composable.common

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

class PermissionManager {

    /**
     * Checks if the app has storage permission
     * For Android 10+: Always returns true (no permission needed)
     * For Android 9-: Checks WRITE_EXTERNAL_STORAGE permission
     */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ uses scoped storage - no permission needed for Downloads
            true
        } else {
            // Android 9 and below need explicit permission
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Checks if we should show rationale for storage permission
     * This should only be called for Android 9 and below
     */
    fun shouldShowPermissionRationale(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            false // No rationale needed for Android 10+
        }
    }

    /**
     * Checks if permission is permanently denied (user selected "Don't ask again")
     */
    fun isPermissionPermanentlyDenied(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            !hasStoragePermission(activity) &&
                    !shouldShowPermissionRationale(activity)
        } else {
            false // Not applicable for Android 10+
        }
    }
}