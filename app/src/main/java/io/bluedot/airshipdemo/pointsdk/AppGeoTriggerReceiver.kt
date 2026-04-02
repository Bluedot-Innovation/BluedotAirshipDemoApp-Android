package io.bluedot.airshipdemo.pointsdk

import android.content.Context
import android.widget.Toast
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.event.GeoTriggerEvent
import au.com.bluedot.point.net.engine.event.NotificationZoneInfo
import com.urbanairship.Airship
import com.urbanairship.analytics.CustomEvent
import io.bluedot.airshipdemo.utilities.createNotification
import io.bluedot.airshipdemo.utilities.fireNotification
import kotlin.collections.iterator

class AppGeoTriggerReceiver : GeoTriggeringEventReceiver() {

    /**
     * This method is invoked whenever the set of zones is updated. There are a number of situations
     * when zone updates can happen, such as initialising the SDK, periodic update, significant location
     * change or zone sync event from Canvas.
     * @param zones List of zones associated with the projectId
     */
    override fun onZoneInfoUpdate(context: Context) {
        Toast.makeText(context, "Rules Updated", Toast.LENGTH_LONG).show()
    }

    /**
     * This method is invoked when the SDK registers an entry event into a geofeature.
     * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
     * or a corresponding exit event occurs, the entry event may occur again.
     * @param entryEvent Provides details of the entry event.
     */
    override fun onZoneEntryEvent(entryEvent: GeoTriggerEvent, context: Context) {
        val entryDetails = "Entered zone ${entryEvent.zoneInfo.name} via fence ${entryEvent.entryEvent()?.fenceId}"
        val customDataString = entryEvent.zoneInfo.customData.toString()

        Toast.makeText(context, entryDetails + customDataString, Toast.LENGTH_LONG).show()

        sendCustomEvent(
            entryDetails,
            entryEvent.zoneInfo,
            -1,
            entryEvent.zoneInfo.customData,
            context
        )
    }

    /**
     * This method is invoked when the SDK registers a dwell event in a geofeature.
     * There can be only one dwell event per zone. However, after the minimum retrigger time lapses,
     * or a corresponding exit event occurs, the dwell event may occur again.
     * @param dwellEvent Provides details of the dwell event.
     */
    override fun onZoneDwellEvent(dwellEvent: GeoTriggerEvent, context: Context) {
        val dwellDetails = "Dwelled in zone ${dwellEvent.zoneInfo.name}"
        Toast.makeText(context, dwellDetails, Toast.LENGTH_LONG).show()

        sendCustomEvent(
            dwellDetails,
            dwellEvent.zoneInfo,
            dwellEvent.dwellEvent()?.dwellThreshold ?: -1,
            dwellEvent.zoneInfo.customData,
            context
        )
    }

    /**
     * This method is invoked when the SDK registers an exit event. An exit event can be triggered if
     * the geofeature is configured to trigger on exit. The option to enable exit events can be found
     * under project and zone configuration on Canvas. An exit event is a pending event and might occur
     * hours later after an entry event. Currently there is timeout for an exit of 24 hours. If an
     * exit wasn't triggered by that time, an automatic exit event will be registered.
     * @param exitEvent Provides details of the exit event.
     */
    override fun onZoneExitEvent(exitEvent: GeoTriggerEvent, context: Context) {
        val exitDetails = "Exited zone ${exitEvent.zoneInfo.name}"
        Toast.makeText(context, exitDetails, Toast.LENGTH_LONG).show()

        sendCustomEvent(
            exitDetails,
            exitEvent.zoneInfo,
            -1,
            exitEvent.zoneInfo.customData,
            context
        )
    }

    private fun sendCustomEvent(
        eventName: String,
        zoneInfo: NotificationZoneInfo,
        dwellTime: Int,
        customDataMap: Map<String, String>?,
        context: Context
    ) {
        // optional - track custom events via Airship
        val builder = CustomEvent.Builder(eventName)
        builder.setInteraction("location", zoneInfo.id.toString())
        builder.addProperty("bluedot_zone_name", zoneInfo.name)
        if (customDataMap != null && customDataMap.isNotEmpty()) {
            for ((key, value) in customDataMap) {
                builder.addProperty(key, value)
            }
        }
        if (dwellTime != -1) {
            builder.addProperty("dwell_time", dwellTime)
        }
        val event = builder.build()
        println("-- event data : " + event.toJsonValue())

        if (Airship.isFlying) {
            event.track()
        }

        fireNotification(
            notification = createNotification(
                title = eventName,
                content = zoneInfo.id.toString(),
                onGoing = false,
                context = context
            ),
            context = context
        )
    }
}