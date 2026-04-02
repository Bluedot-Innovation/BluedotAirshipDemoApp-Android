# Airship Events Example

A sample project used to test the integration between Airship SDK and Bluedot Point SDK.

## Getting started

This project depends on `point_sdk_android` and `urbanairship-fcm`. Both dependencies are managed by Gradle.

### Prerequisites — API keys

The project reads sensitive credentials from two files that are **not** committed to version control. You must provide them before building.

#### 1. `local.properties`

Add the following keys to the `local.properties` file in the root of the project (create it if it doesn't exist):

```properties
# Android SDK location (usually added automatically by Android Studio)
sdk.dir=/path/to/your/android/sdk

# Bluedot project ID from the Canvas web portal
bluedot.projectId=YOUR_BLUEDOT_PROJECT_ID

# Airship app key and secret from the Airship dashboard
airship.appKey=YOUR_AIRSHIP_APP_KEY
airship.appSecret=YOUR_AIRSHIP_APP_SECRET
```

These values are injected as `BuildConfig` constants at compile time (`BuildConfig.BLUEDOT_PROJECT_ID`, `BuildConfig.AIRSHIP_APP_KEY`, `BuildConfig.AIRSHIP_APP_SECRET`) and used as default values in the app's UI. They can also be overridden at runtime through the main screen.

#### 2. `app/google-services.json`

Place your Firebase `google-services.json` file in the `app/` directory. This file is required for FCM push notifications (used by the Airship FCM integration). You can download it from the [Firebase Console](https://console.firebase.google.com/) for your project.

---

### Implement `point_sdk_android`

1. Add `point_sdk_android` as a dependency. The version is controlled by the `pointSdkVersion` variable in `app/build.gradle`:

```groovy
def pointSdkVersion = "17.4.1"

dependencies {
    implementation "com.gitlab.bluedotio.android:point_sdk_android:$pointSdkVersion"
}

android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_21
        targetCompatibility JavaVersion.VERSION_21
    }
}
```

2. Initialise the Point SDK from `MainApplication`. The app calls `ServiceManager.getInstance(context).initialize(projectId, configUrl, resultListener)` and then starts Geo-triggering with a foreground notification via `GeoTriggeringService.builder()`.

3. Supply your **Bluedot Project ID** either in `local.properties` (compiled into `BuildConfig.BLUEDOT_PROJECT_ID`) or enter it at runtime in the app's main screen.

4. Receive geo-trigger callbacks by extending `GeoTriggeringEventReceiver` and registering it in `AndroidManifest.xml`:

```xml
<receiver
    android:name=".pointsdk.AppGeoTriggerReceiver"
    android:enabled="true"
    android:exported="false">
    <intent-filter>
        <action android:name="io.bluedot.point.GEOTRIGGER" />
    </intent-filter>
</receiver>
```

5. Override `onZoneInfoUpdate`, `onZoneEntryEvent`, `onZoneDwellEvent`, and `onZoneExitEvent`. The actual implementation in `AppGeoTriggerReceiver` uses the `GeoTriggerEvent` type (replacing the older `ZoneEntryEvent` / `ZoneExitEvent` types) and also handles the **dwell** event:

```kotlin
class AppGeoTriggerReceiver : GeoTriggeringEventReceiver() {

    override fun onZoneInfoUpdate(context: Context) {
        Toast.makeText(context, "Rules Updated", Toast.LENGTH_LONG).show()
    }

    override fun onZoneEntryEvent(entryEvent: GeoTriggerEvent, context: Context) {
        val entryDetails = "Entered zone ${entryEvent.zoneInfo.name} via fence ${entryEvent.entryEvent()?.fenceId}"
        sendCustomEvent(entryDetails, entryEvent.zoneInfo, -1, entryEvent.zoneInfo.customData, context)
    }

    override fun onZoneDwellEvent(dwellEvent: GeoTriggerEvent, context: Context) {
        val dwellDetails = "Dwelled in zone ${dwellEvent.zoneInfo.name}"
        sendCustomEvent(
            dwellDetails,
            dwellEvent.zoneInfo,
            dwellEvent.dwellEvent()?.dwellThreshold ?: -1,
            dwellEvent.zoneInfo.customData,
            context
        )
    }

    override fun onZoneExitEvent(exitEvent: GeoTriggerEvent, context: Context) {
        val exitDetails = "Exited zone ${exitEvent.zoneInfo.name}"
        sendCustomEvent(exitDetails, exitEvent.zoneInfo, -1, exitEvent.zoneInfo.customData, context)
    }
}
```

---

### Implement Airship Android SDK

1. Follow the official [Airship documentation](https://docs.airship.com/platform/mobile/setup/sdk/android/) to integrate the Airship Android SDK. The version is controlled by the `airshipVersion` variable in `app/build.gradle`:

```groovy
def airshipVersion = "20.6.1"

dependencies {
    implementation "com.urbanairship.android:urbanairship-fcm:$airshipVersion"
    implementation "com.urbanairship.android:urbanairship-adm:$airshipVersion"
    implementation "com.urbanairship.android:urbanairship-automation:$airshipVersion"
    implementation "com.urbanairship.android:urbanairship-message-center:$airshipVersion"
}
```

2. Airship configuration is handled in `AirshipAutopilot`. It reads credentials from `RezolvePreferences` (values previously entered at runtime) and falls back to `BuildConfig` constants sourced from `local.properties`. The site is configured to **EU** (`SITE_EU`); update this if you are on the US site:

```kotlin
companion object {
    fun makeAirshipConfigOptions(context: Context): AirshipConfigOptions {
        val preferences = RezolvePreferences(context)
        val appKey = preferences.airshipAppKey.ifEmpty { BuildConfig.AIRSHIP_APP_KEY }
        val appSecret = preferences.airshipAppSecret.ifEmpty { BuildConfig.AIRSHIP_APP_SECRET }

        return airshipConfigOptions {
            setAppKey(appKey)
            setAppSecret(appSecret)
            setSite(AirshipConfigOptions.Site.SITE_EU)   // change to SITE_US if needed
            setInProduction(!BuildConfig.DEBUG)
            // ...
        }
    }
}
```

3. Declare `AirshipAutopilot` in `AndroidManifest.xml` so Airship can auto-initialise:

```xml
<meta-data
    android:name="com.urbanairship.autopilot"
    android:value="io.bluedot.airshipdemo.AirshipAutopilot" />
```

4. Airship is also initialised programmatically from `MainApplication.initAirship()` when credentials are supplied via the UI (using `Airship.takeOff`). Airship **must be initialised before** any Bluedot check-in/check-out events are tracked.

5. Custom events are sent in `AppGeoTriggerReceiver.sendCustomEvent()`. The event is only tracked when Airship is already flying (`Airship.isFlying`):

```kotlin
private fun sendCustomEvent(
    eventName: String,
    zoneInfo: NotificationZoneInfo,
    dwellTime: Int,
    customDataMap: Map<String, String>?,
    context: Context
) {
    //  name: <event name, e.g. "Entered zone …">
    //  interaction_type: "location"
    //  interaction_id: zone_id
    //  properties: {
    //      bluedot_zone_name: <zone_name>
    //      dwell_time: <dwell_threshold>   (only for dwell events)
    //      <all custom data key/value pairs>
    //  }
    val builder = CustomEvent.Builder(eventName)
    builder.setInteraction("location", zoneInfo.id.toString())
    builder.addProperty("bluedot_zone_name", zoneInfo.name)
    if (!customDataMap.isNullOrEmpty()) {
        for ((key, value) in customDataMap) {
            builder.addProperty(key, value)
        }
    }
    if (dwellTime != -1) {
        builder.addProperty("dwell_time", dwellTime)
    }
    val event = builder.build()
    if (Airship.isFlying) {
        event.track()
    }
}
```

---

## Next steps

Full documentation:
- Bluedot: https://docs.bluedot.io/Point%20SDK/Android/Overview
- Airship Android: https://docs.airship.com/platform/android/getting-started/
