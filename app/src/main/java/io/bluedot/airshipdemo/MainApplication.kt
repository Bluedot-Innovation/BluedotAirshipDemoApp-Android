package io.bluedot.airshipdemo

import android.Manifest.permission
import android.app.Application
import android.app.Notification
import android.app.Notification.BigTextStyle
import android.app.Notification.Builder
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import au.com.bluedot.point.net.engine.GeoTriggeringService
import au.com.bluedot.point.net.engine.InitializationResultListener
import au.com.bluedot.point.net.engine.ServiceManager
import com.urbanairship.UAirship
import com.urbanairship.UAirship.OnReadyCallback
import com.urbanairship.push.notifications.NotificationChannelCompat
import io.bluedot.airshipdemo.R.mipmap
import io.bluedot.airshipdemo.R.string

class MainApplication : Application(), OnReadyCallback {
  private lateinit var serviceManager: ServiceManager
  private val PROJECT_ID = "<PROJECT-ID>" //ProjectID for the Bluedot Canvas portal
  private var channelCompat: NotificationChannelCompat? = null
  override fun onCreate() {
    super.onCreate()

    //take off UrbanAirship SDK
    initUrbanAirshipSdk()

    //start Point SDK
    initPointSDK()
  }

  fun initPointSDK() {
    val locationPermissionGranted = ActivityCompat.checkSelfPermission(
      applicationContext, permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (locationPermissionGranted) {
      serviceManager = ServiceManager.getInstance(this)
      if (!serviceManager.isBluedotServiceInitialized) {
        val resultListener = InitializationResultListener { bdError ->
          var text = "Initialization Result "
          if (bdError != null) text += bdError.reason else {
            text += "Success "
            startGeoTrigger()
          }
          Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
        }
        serviceManager.initialize(PROJECT_ID, resultListener)
      }
    } else {
      requestPermissions()
    }
  }

  private fun initUrbanAirshipSdk() {
    UAirship.takeOff(this, this)
  }

  override fun onAirshipReady(uAirship: UAirship) {
    println("-- onAirshipReady")
    uAirship.pushManager.userNotificationsEnabled = true

    //setting up notification channel
    if (channelCompat != null) {
      if (VERSION.SDK_INT >= VERSION_CODES.O) {
        uAirship.pushManager
          .notificationChannelRegistry
          .createNotificationChannel(channelCompat!!)
      }
    }
  }

  fun reset() {
    serviceManager.reset { bdError ->
      var text = "Reset Finished "
      if (bdError != null) text += bdError.reason else {
        text += "Success "
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
      }
      Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
    }
  }

  private fun startGeoTrigger() {
    val notification = createNotification()
    GeoTriggeringService.builder()
      .notification(notification)
      .start(this) { geoTriggerError ->
        if (geoTriggerError != null) {
          Toast.makeText(
            applicationContext,
            "Error in starting GeoTrigger" + geoTriggerError.reason,
            Toast.LENGTH_LONG
          ).show()
          return@start
        }
        Toast.makeText(
          applicationContext,
          "GeoTrigger started successfully",
          Toast.LENGTH_LONG
        ).show()
      }
  }

  /**
   * Creates notification channel and notification, required for foreground service notification.
   *
   * @return notification
   */
  private fun createNotification(): Notification {
    val channelId: String
    val channelName: String
    return if (VERSION.SDK_INT >= VERSION_CODES.O) {
      channelId = "Bluedot" + getString(string.app_name)
      channelName = "Bluedot Service" + getString(string.app_name)
      channelCompat = NotificationChannelCompat(
        channelId,
        channelName,
        NotificationManagerCompat.IMPORTANCE_DEFAULT
      )
      val notificationChannel = NotificationChannel(
        channelId, channelName,
        NotificationManager.IMPORTANCE_DEFAULT
      )
      notificationChannel.enableLights(false)
      notificationChannel.lightColor = Color.RED
      notificationChannel.enableVibration(false)
      val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
      notificationManager.createNotificationChannel(notificationChannel)
      val notification = Builder(
        applicationContext, channelId
      )
        .setContentTitle(getString(string.foreground_notification_title))
        .setContentText(getString(string.foreground_notification_text))
        .setStyle(BigTextStyle().bigText(getString(string.foreground_notification_text)))
        .setOngoing(true)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setSmallIcon(mipmap.ic_launcher)
      notification.build()
    } else {
      val notification = NotificationCompat.Builder(
        applicationContext
      )
        .setContentTitle(getString(string.foreground_notification_title))
        .setContentText(getString(string.foreground_notification_text))
        .setStyle(
          NotificationCompat.BigTextStyle().bigText(getString(string.foreground_notification_text))
        )
        .setOngoing(true)
        .setCategory(Notification.CATEGORY_SERVICE)
        .setPriority(NotificationManager.IMPORTANCE_HIGH)
        .setSmallIcon(mipmap.ic_launcher)
      notification.build()
    }
  }

  private fun requestPermissions() {
    val intent = Intent(applicationContext, RequestPermissionActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    startActivity(intent)
  }
}
