package io.bluedot.airshipdemo

import android.content.Context
import android.widget.Toast
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.event.GeoTriggerEvent
import au.com.bluedot.point.net.engine.ZoneInfo
import au.com.bluedot.point.net.engine.event.NotificationZoneInfo
import com.urbanairship.analytics.CustomEvent.Builder

class AppGeoTriggerReceiver : GeoTriggeringEventReceiver() {
  private val EVENT_PLACE_ENTERED = "bluedot_place_entered"
  private val EVENT_PLACE_EXITED = "bluedot_place_exited"

  /**
   * This method is invoked whenever the set of zones is updated. There are a number of situations
   * when zone updates can happen, such as initialising the SDK, periodic update, significant location
   * change or zone sync event from Canvas.
   * @param zones List of zones associated with the projectId
   */
  override fun onZoneInfoUpdate(context: Context) {
    Toast.makeText(
      context, "Rules Updated",
      Toast.LENGTH_LONG
    ).show()
  }

  /**
   * This method is invoked when the SDK registers an entry event into a geofeature.
   * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
   * or a corresponding exit event occurs, the entry event may occur again.
   * @param entryEvent Provides details of the entry event.
   */
  override fun onZoneEntryEvent(entryEvent: GeoTriggerEvent, context: Context) {
    val entryDetails = "Entered zone " + entryEvent.zoneInfo.name + " via fence " +
            entryEvent.entryEvent()?.fenceName
    var customDataString = ""
    if (entryEvent.zoneInfo.customData != null) {
      customDataString =
        entryEvent.zoneInfo.customData.toString()
    }

    Toast.makeText(
      context, entryDetails + customDataString,
      Toast.LENGTH_LONG
    ).show()

    sendCustomEvent(
      EVENT_PLACE_ENTERED,
      entryEvent.zoneInfo,
      -1,
      entryEvent.zoneInfo.customData
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
    val exitDetails = "Exited zone" + exitEvent.zoneInfo.name
    val dwellT = "Dwell time: " + exitEvent.exitEvent()?.dwellTime.toString() + " milliseconds"
    Toast.makeText(
      context, exitDetails + dwellT,
      Toast.LENGTH_LONG
    ).show()
    sendCustomEvent(
      EVENT_PLACE_EXITED,
      exitEvent.zoneInfo,
      exitEvent.exitEvent()?.dwellTime?.toInt() ?: 0,
      exitEvent.zoneInfo.customData
    )
  }

  private fun sendCustomEvent(
    eventName: String,
    zoneInfo: NotificationZoneInfo,
    dwellTime: Int,
    customDataMap: Map<String, String>?
  ) {
    //        name: "bluedot_place_exited"
    //        interaction_type: "location"
    //        interaction_id: zone_id
    //        properties: {
    //            bluedot_zone_name: <zone_name>
    //                    dwell_time: <dwell_time>
    //  <all custom data>
    //        }
    val builder = Builder(eventName)
    builder.setInteraction("location", zoneInfo.id.toString())
    zoneInfo.name.let { builder.addProperty("bluedot_zone_name", it) }
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
    event.track()
  }
}