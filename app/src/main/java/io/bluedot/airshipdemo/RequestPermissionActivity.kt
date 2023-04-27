package io.bluedot.airshipdemo

import android.Manifest.permission
import android.annotation.TargetApi
import android.app.AlertDialog
import android.app.AlertDialog.Builder
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.bluedot.airshipdemo.R.string

/*
 * @author Bluedot Innovation
 * Copyright (c) 2023 Bluedot Innovation. All rights reserved.
 * RequestPermissionActivity handles permission requests needed for running Bluedot Point SDK on Marshmallow devices.
 */
class RequestPermissionActivity : AppCompatActivity() {
  private val PERMISSION_REQUEST_CODE = 1
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val permissions = arrayOfNulls<String>(1)
    permissions[0] = permission.ACCESS_FINE_LOCATION

    //Request permission required for location
    ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
  }

  @TargetApi(VERSION_CODES.M) override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    when (requestCode) {
      PERMISSION_REQUEST_CODE -> {
        var permissionGranted = true
        for (i in grantResults) {
          permissionGranted = permissionGranted && i == PackageManager.PERMISSION_GRANTED
        }
        if (permissionGranted) {
          (application as MainApplication).initPointSDK()
        } else {
          if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
            val alertDialog = Builder(this).create()
            alertDialog.setTitle("Information")
            alertDialog.setMessage(resources.getString(string.permission_needed))
            alertDialog.setButton(
              AlertDialog.BUTTON_NEUTRAL, "OK"
            ) { dialog, which -> dialog.dismiss() }
            alertDialog.show()
          } else {
            val alertDialog = Builder(this).create()
            alertDialog.setTitle("Information")
            alertDialog.setMessage(resources.getString(string.location_permissions_mandatory))
            alertDialog.setButton(
              AlertDialog.BUTTON_NEUTRAL, "OK"
            ) { dialog, which ->
              val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
              val uri = Uri.fromParts(
                "package",
                applicationContext.packageName,
                null
              )
              intent.data = uri
              startActivityForResult(intent, PERMISSION_REQUEST_CODE)
            }
            alertDialog.show()
          }
        }
        finish()
      }
    }
  }
}