package com.example.group21

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.InputStream

object Util {

    const val REQUEST_CODE_PERMISSIONS = 100 // Standard request code

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        // Storage permissions are now mostly handled automatically via FileProvider, but kept for older APIs
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    /**
     * Checks if all required permissions are granted and requests them.
     * If permissions were permanently denied, guides the user to Settings.
     */
    fun checkPermissions(activity: Activity?) {
        if (activity == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return

        // 1. Check which permissions are NOT granted.
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()


        if (permissionsToRequest.isNotEmpty()) {
            // 2. Check if permissions were previously denied (leading to Rationale/Settings need)
            val shouldShowRationale = permissionsToRequest.any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            }

            if (shouldShowRationale) {
                // If any permission was previously denied, show Toast and guide to Settings.
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null)
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS

                Toast.makeText(activity, "Please grant all required permissions in the settings menu", Toast.LENGTH_LONG).show()

                try {
                    activity.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Log.e("Util", "Settings Activity not found: ${e.message}")
                }
            } else {
                // Request permissions normally for the first time or if user hasn't permanently denied
                ActivityCompat.requestPermissions(activity, permissionsToRequest, REQUEST_CODE_PERMISSIONS)
            }
        }
        // If permissionsToRequest is empty, all permissions are granted.
    }

    /**
     * Retrieves a Bitmap from a given Uri without failing due to rotation metadata.
     * Note: This simple implementation is prone to OutOfMemoryErrors (OOM) for very large images.
     */
    fun getBitmap(context: Context, imgUri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        return try {
            inputStream = context.contentResolver.openInputStream(imgUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // The original code used a Matrix without modification, which is harmless but redundant.
            // Returning the bitmap directly is clearer unless specific rotation logic is needed.
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, Matrix(), true)

        } catch (e: Exception) {
            Log.e("Util", "Error loading bitmap from Uri: ${e.message}", e)
            null
        } finally {
            inputStream?.close()
        }
    }
}