package io.bluedot.airshipdemo.utilities

import android.Manifest.permission
import android.os.Build

val localPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    listOf(
        permission.ACCESS_FINE_LOCATION,
        permission.POST_NOTIFICATIONS
    )
} else {
    listOf(
        permission.ACCESS_FINE_LOCATION
    )
}
