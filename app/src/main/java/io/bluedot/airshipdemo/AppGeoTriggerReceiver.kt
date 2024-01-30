package io.bluedot.airshipdemo

import android.content.Context
import android.widget.Toast
import au.com.bluedot.point.net.engine.GeoTriggeringEventReceiver
import au.com.bluedot.point.net.engine.ZoneEntryEvent
import au.com.bluedot.point.net.engine.ZoneExitEvent
import au.com.bluedot.point.net.engine.ZoneInfo
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
  override fun onZoneInfoUpdate(zones: List<ZoneInfo>, context: Context) {
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
  override fun onZoneEntryEvent(entryEvent: ZoneEntryEvent, context: Context) {
    val entryDetails = "Entered zone " + entryEvent.zoneInfo.zoneName
      .toString() + " via fence " + entryEvent.fenceInfo.name
    var customDataString = ""
    if (entryEvent.zoneInfo.getCustomData() != null) {
      customDataString =
        entryEvent.zoneInfo.getCustomData().toString()
    }

    Toast.makeText(
      context, entryDetails + customDataString,
      Toast.LENGTH_LONG
    ).show()

    sendCustomEvent(
      EVENT_PLACE_ENTERED,
      entryEvent.zoneInfo,
      -1,
      entryEvent.zoneInfo.getCustomData()
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
  override fun onZoneExitEvent(exitEvent: ZoneExitEvent, context: Context) {
    val exitDetails = "Exited zone" + exitEvent.zoneInfo.zoneName
    val dwellT = "Dwell time: " + exitEvent.dwellTime.toString() + " minutes"
    Toast.makeText(
      context, exitDetails + dwellT,
      Toast.LENGTH_LONG
    ).show()
    sendCustomEvent(
      EVENT_PLACE_EXITED,
      exitEvent.zoneInfo,
      exitEvent.dwellTime,
      exitEvent.zoneInfo.getCustomData()
    )
  }

  private fun sendCustomEvent(
    eventName: String,
    zoneInfo: ZoneInfo,
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
    builder.setInteraction("location", zoneInfo.zoneId)
    zoneInfo.zoneName?.let { builder.addProperty("bluedot_zone_name", it) }
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