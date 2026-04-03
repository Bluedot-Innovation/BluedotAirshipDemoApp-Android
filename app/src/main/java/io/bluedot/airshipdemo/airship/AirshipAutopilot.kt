package io.bluedot.airshipdemo.airship

import android.content.Context
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.Autopilot
import com.urbanairship.UAirship
import com.urbanairship.messagecenter.MessageCenter
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.R
import io.bluedot.airshipdemo.utilities.RezolvePreferences

class AirshipAutopilot : Autopilot() {
    override fun createAirshipConfigOptions(context: Context): AirshipConfigOptions {
        return makeAirshipConfigOptions(context)
    }

    override fun onAirshipReady(airship: UAirship) {
        airshipReady()
    }

    companion object {
        fun makeAirshipConfigOptions(context: Context): AirshipConfigOptions {
            val preferences = RezolvePreferences(context)
            val appKey = preferences.airshipAppKey.ifEmpty { BuildConfig.AIRSHIP_APP_KEY }
            val appSecret = preferences.airshipAppSecret.ifEmpty { BuildConfig.AIRSHIP_APP_SECRET }
            val site = if (preferences.airshipSite == AirshipConfigOptions.SITE_US) AirshipConfigOptions.SITE_US else AirshipConfigOptions.SITE_EU

            return AirshipConfigOptions.Builder()
                .setAppKey(appKey)
                .setAppSecret(appSecret)
                .setSite(site)
                .setInProduction(!BuildConfig.DEBUG)

                .setNotificationAccentColor(context.getColor(R.color.colorAccent))
                .setNotificationIcon(R.drawable.ic_stat_name)
                .build()
        }

        fun airshipReady() {
            val airshipListener = AirshipListener()
            with(UAirship.shared().pushManager) {
                addPushListener(airshipListener)
                addPushTokenListener(airshipListener)
                notificationListener = airshipListener
            }

            UAirship.shared().channel.addChannelListener(airshipListener)
            UAirship.shared().pushManager.userNotificationsEnabled = true
            MessageCenter.shared().setOnShowMessageCenterListener { messageId: String? ->
                true
            }
        }
    }
}
