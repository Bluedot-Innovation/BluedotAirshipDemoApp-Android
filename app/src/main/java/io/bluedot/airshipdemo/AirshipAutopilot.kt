package io.bluedot.airshipdemo

import android.content.Context
import androidx.core.content.ContextCompat
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.Autopilot
import com.urbanairship.UAirship
import com.urbanairship.messagecenter.MessageCenter
import com.urbanairship.push.notifications.NotificationProvider

class AirshipAutopilot : Autopilot() {

  override fun createAirshipConfigOptions(context: Context): AirshipConfigOptions? {
    val builder = AirshipConfigOptions.newBuilder()

    builder.setDevelopmentAppKey("YOUR DEV APP KEY")
    builder.setDevelopmentAppSecret("YOUR DEV APP SECRET")

    builder.setProductionAppKey("YOUR PROD APP KEY")
    builder.setProductionAppSecret("YOUR PROD APP SECRET")

    // toggle this to TRUE before submitting app to store
    builder.setInProduction(false)

    // Set site. Either SITE_US or SITE_EU
    builder.setSite(AirshipConfigOptions.SITE_US)

    // Allow lists. Use * to allow anything
    builder.setUrlAllowList(arrayOf("*"))

    // Any other configurations if needed
    // Set Common Notification config
    builder.setNotificationAccentColor(ContextCompat.getColor(context, R.color.colorPrimary))
      .setNotificationIcon(R.drawable.ic_stat_name)
      .setNotificationChannel(NotificationProvider.DEFAULT_NOTIFICATION_CHANNEL)

    return builder.build()
  }

  override fun onAirshipReady(airship: UAirship) {
    // Custom Message Center
    MessageCenter.shared().setOnShowMessageCenterListener { messageId: String? ->
      true
    }

    // etc...
  }
}