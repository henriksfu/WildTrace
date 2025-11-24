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

object Util {

    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return

        val permissionsNeeded = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        var notFineLocation = ContextCompat.checkSelfPermission(activity!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        var notCoarseLocation =  ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED

        // only 1 of fine and coarse location is needed
        val needsRequest = permissionsNeeded.take(permissionsNeeded.size - 2).any {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        } || (notFineLocation && notCoarseLocation)


        if (needsRequest) {
            notFineLocation =   ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)
            notCoarseLocation = ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_COARSE_LOCATION)

            // only care if both fine and coarse permissions denied
            val denied = permissionsNeeded.take(permissionsNeeded.size - 2).any { permission ->
                ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            } || (notFineLocation && notCoarseLocation)

            if (denied) {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", activity.packageName, null)
                )

                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                Toast.makeText(activity, "Please grant all permissions in the settings menu", Toast.LENGTH_LONG).show()

                try {
                    activity.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            else {
                ActivityCompat.requestPermissions(activity, permissionsNeeded, 0)
            }
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
