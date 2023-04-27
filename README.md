# Airship Events Example

A sample project used to test the integration between Airship SDK and Bluedot Point SDK.

## Getting started

This project depends on `PointSDK-Android` and `urbanairship-fcm`. Both dependencies are managed by Gradle.

### Implement `PointSDK-Android`

1. Add `PointSDK-Android` module as a dependency to your application.

```groovy
dependencies {
  ...
  // Latest Android PointSDK is 15.5.3 
  implementation 'com.gitlab.bluedotio.android:point_sdk_android:15.5.3'
}

android {
  ...
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}
```

2. Start `PointSDK` in the application's `onCreate`. See `initPointSDK()` in `MainApplication` class. 

3. Update value of `PROJECT_ID` inside `MainApplication` class to your projectId from Bluedot Canvas web portal.
4. To receive callbacks, create a Receiver by extending `GeoTriggeringEventReceiver` and also add it in `AndroidManifest.xml`. 

```xml
 <receiver
    android:name="yourpackagename.AppGeoTriggerReceiver"
    android:enabled="true"
    android:exported="false">
  <intent-filter>
    <action android:name="io.bluedot.point.GEOTRIGGER" />
  </intent-filter>
</receiver>
```

4. Override callbacks `onZoneInfoUpdate`, `onZoneEntryEvent`, `onZoneExitEvent` and implement your own logic to send events to Airship or trigger custom flows.

```kotlin
class AppGeoTriggerReceiver : GeoTriggeringEventReceiver() {
  private val EVENT_PLACE_ENTERED = "bluedot_place_entered"
  private val EVENT_PLACE_EXITED = "bluedot_place_exited"

  /**
   * This method is invoked whenever the set of zones is updated. There are a number of situations
   * when zone updates can happen, such as initializing the SDK, periodic update, significant location
   * change or zone sync event from Canvas.
   * @param zones List of zones associated with the projectId
   */
  override fun onZoneInfoUpdate(zones: List<ZoneInfo?>, context: Context) {
    ...
  }

  /**
   * This method is invoked when the SDK registers an entry event into a geofeature.
   * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
   * or a corresponding exit event occurs, the entry event may occur again.
   * @param entryEvent Provides details of the entry event.
   */
  override fun onZoneEntryEvent(entryEvent: ZoneEntryEvent, context: Context) {
    ...
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
    ...
  }
}
```

### Implement `Airship Android SDK`

1. Follow official [Airship documentation](https://docs.airship.com/platform/mobile/setup/sdk/android/) to integrate Airship Android SDK into your app
Please note that Airship has to be initialized before sending any Bluedot check-in/check-out events.
2. Update Airship configurations in AirshipAutopilot class as per your Airship setup: development/production app key/secret, US/EU site, notification settings...

```kotlin
builder.setDevelopmentAppKey("YOUR DEV APP KEY")
builder.setDevelopmentAppSecret("YOUR DEV APP SECRET")

builder.setProductionAppKey("YOUR PROD APP KEY")
builder.setProductionAppSecret("YOUR PROD APP SECRET")
  ...
```

3. Track `Airship` events in your ZoneEntry/ZoneExits. See examples in `AppGeoTriggerReceiver` class.

```kotlin
override fun onZoneEntryEvent(entryEvent: ZoneEntryEvent, context: Context) {
  ...
  sendCustomEvent(
    EVENT_PLACE_ENTERED,
    entryEvent.zoneInfo,
    entryEvent.zoneInfo.customData
  )
}

override fun onZoneExitEvent(exitEvent: ZoneExitEvent, context: Context) {
  ...
  sendCustomEvent(
    EVENT_PLACE_EXITED,
    exitEvent.zoneInfo,
    exitEvent.dwellTime,
    exitEvent.zoneInfo.customData
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

```

## Next steps
Full documentation can be found at https://docs.bluedot.io/airship-integration/urban-airship-android-integration/ and https://docs.airship.com/platform/android/getting-started/ respectively.
