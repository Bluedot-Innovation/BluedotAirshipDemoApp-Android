package io.bluedot.airshipdemo.airship

import android.content.Context
import android.util.Log
import com.urbanairship.Airship
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.Autopilot
import com.urbanairship.airshipConfigOptions
import com.urbanairship.messagecenter.MessageCenter
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.R
import io.bluedot.airshipdemo.utilities.RezolvePreferences

class AirshipAutopilot : Autopilot() {
    override fun createAirshipConfigOptions(context: Context): AirshipConfigOptions {
        return makeAirshipConfigOptions(context)
    }

    override fun onAirshipReady(context: Context) {
        airshipReady()
    }

    companion object {
        fun makeAirshipConfigOptions(context: Context): AirshipConfigOptions {
            val preferences = RezolvePreferences(context)
            val appKey = preferences.airshipAppKey.ifEmpty { BuildConfig.AIRSHIP_APP_KEY }
            val appSecret = preferences.airshipAppSecret.ifEmpty { BuildConfig.AIRSHIP_APP_SECRET }

            return airshipConfigOptions {
                setAppKey(appKey)
                setAppSecret(appSecret)
                setSite(AirshipConfigOptions.Site.SITE_EU)
                setInProduction(!BuildConfig.DEBUG)

                setNotificationAccentColor(context.getColor(R.color.colorAccent))
                setNotificationIcon(R.drawable.ic_stat_name)

                setDevelopmentLogLevel(AirshipConfigOptions.LogLevel.VERBOSE)
                setDevelopmentLogPrivacyLevel(AirshipConfigOptions.PrivacyLevel.PUBLIC)
            }
        }

        fun airshipReady() {
            val airshipListener = AirshipListener()
            with(Airship.push) {
                addPushListener(airshipListener)
                addPushTokenListener(airshipListener)
                notificationListener = airshipListener
            }

            Airship.channel.addChannelListener(airshipListener)
            Airship.push.userNotificationsEnabled = true
            MessageCenter.shared().setOnShowMessageCenterListener { messageId: String? ->
                true
            }
        }
    }
}
