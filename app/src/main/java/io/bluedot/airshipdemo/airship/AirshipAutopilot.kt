package io.bluedot.airshipdemo.airship

import android.content.Context
import android.util.Log
import com.urbanairship.UAirship
import com.urbanairship.AirshipConfigOptions
import com.urbanairship.Autopilot
import com.urbanairship.messagecenter.MessageCenter
import io.bluedot.airshipdemo.BuildConfig
import io.bluedot.airshipdemo.MainApplication
import io.bluedot.airshipdemo.R
import io.bluedot.airshipdemo.utilities.RezolvePreferences

class AirshipAutopilot : Autopilot() {

    // Store context captured during config creation so onAirshipReady can reach MainApplication.
    private var appContext: Context? = null

    override fun createAirshipConfigOptions(context: Context): AirshipConfigOptions {
        appContext = context.applicationContext
        return makeAirshipConfigOptions(context)
    }

    override fun onAirshipReady(uairship: UAirship) {
        airshipReady()

        // Notify MainApplication now that Airship is ready. This is the earliest reliable
        // point to read channel.id and register a channel listener.
        val app = appContext?.applicationContext as? MainApplication
        app?.onAirshipStarted(uairship)
    }

    companion object {
        fun makeAirshipConfigOptions(context: Context): AirshipConfigOptions {
            val preferences = RezolvePreferences(context)
            val appKey = preferences.airshipAppKey.ifEmpty { BuildConfig.AIRSHIP_APP_KEY }
            val appSecret = preferences.airshipAppSecret.ifEmpty { BuildConfig.AIRSHIP_APP_SECRET }

            // Fix: compare correctly — if saved site is US, use US; otherwise default to EU.
            val site = if (preferences.airshipSite == AirshipConfigOptions.SITE_US) {
                AirshipConfigOptions.SITE_US
            } else {
                AirshipConfigOptions.SITE_EU
            }

            Log.d("AirshipAutopilot", "Config: key=${appKey.take(4)}***, site=$site")

            return AirshipConfigOptions.newBuilder()
                .setAppKey(appKey)
                .setAppSecret(appSecret)
                .setSite(site)
                .setInProduction(!BuildConfig.DEBUG)
                .setNotificationAccentColor(context.getColor(R.color.colorAccent))
                .setNotificationIcon(R.drawable.ic_stat_name)
                .setDevelopmentLogLevel(Log.VERBOSE)
                .setDevelopmentLogPrivacyLevel(AirshipConfigOptions.PrivacyLevel.PUBLIC)
                .build()
        }

        fun airshipReady() {
            Log.i("AirshipAutopilot", "Airship is ready!")
            val listener = AirshipListener()
            with(UAirship.shared().pushManager) {
                addPushListener(listener)
                addPushTokenListener(listener)
                notificationListener = listener
                userNotificationsEnabled = true
            }

            UAirship.shared().channel.addChannelListener(listener)
            Log.i("AirshipAutopilot", "Channel ID at ready: ${UAirship.shared().channel.id}")
            MessageCenter.shared().setOnShowMessageCenterListener { true }
        }
    }
}
