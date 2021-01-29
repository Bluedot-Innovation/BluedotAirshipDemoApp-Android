# Airship Events Example

A sample project used to test the integration between Airship SDK and Bluedot Point SDK.

## Getting started

This project depends on `PointSDK-Android` and `urbanairship-fcm`. Both dependencies are managed by Gradle.

### Implement `PointSDK-Android`

1. Add `PointSDK-Android` module as a dependency to your application.

```groovy
dependencies {
    ...

    implementation 'com.gitlab.bluedotio.android:point_sdk_android:15.3.0'
}

android {
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}
```

2. Start `PointSDK` in the application's `onCreate`

```java
@Override
public void onCreate() {
    super.onCreate();

    ...

    // Initialize Point SDK and start Geo Trigger
       boolean locationPermissionGranted =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (locationPermissionGranted) {
            serviceManager = ServiceManager.getInstance(this);

            if (!serviceManager.isBluedotServiceInitialized()) {

                InitializationResultListener resultListener = bdError -> {
                    String text = "Initialization Result ";
                    if(bdError != null)
                        text = text + bdError.getReason();
                    else {
                        text = text + "Success ";
                        startGeoTrigger();
                    }
                    Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();
                };
                serviceManager.initialize(PROJECT_ID, resultListener);
            }
        } else {
            requestPermissions();
        }
    }
}
```

3. To receive callbacks, create a Receiver by extending `GeoTriggeringEventReceiver` and also add it in `AndroidManifest.xml`

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

```java
public class AppGeoTriggerReceiver extends GeoTriggeringEventReceiver {
    private final String EVENT_PLACE_ENTERED = "bluedot_place_entered";
    private final String EVENT_PLACE_EXITED = "bluedot_place_exited";

    /**
     * This method is invoked whenever the set of zones is updated. There are a number of situations
     * when zone updates can happen, such as initialising the SDK, periodic update, significant location
     * change or zone sync event from Canvas.
     * @param zones List of zones associated with the projectId
     */
    @Override public void onZoneInfoUpdate(@NotNull List<ZoneInfo> zones, @NotNull Context context) {
        ...
    }

    /**
     * This method is invoked when the SDK registers an entry event into a geofeature.
     * There can be only one entry event per zone. However, after the minimum retrigger time lapses,
     * or a corresponding exit event occurs, the entry event may occur again.
     * @param entryEvent Provides details of the entry event.
     */
    @Override
    public void onZoneEntryEvent(@NotNull ZoneEntryEvent entryEvent, @NotNull Context context) {
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
    @Override
    public void onZoneExitEvent(@NotNull ZoneExitEvent exitEvent, @NotNull Context context) {
        ...
    }
    
}
```

### Implement `urbanairship-fcm`

Airship has to be initialised before sending any check-in/checkou-out events.

1. Add the `urbanairship-fcm` module as a dependency in your application.

```groovy
dependencies {
    ...

    implementation 'com.urbanairship.android:urbanairship-fcm:<version>'
    implementation 'com.google.firebase:firebase-messaging:<version>'
}
```

2. Add `airshipconfig.properties` file to your application's `assets` directory

```
developmentAppKey = your development UA app key 
developmentAppSecret = your development UA app secret


productionAppKey = your production UA app key
productionAppSecret = your production UA app secret

# Toggles between the development and production app credentials
# Before submitting your application to an app store set to true
inProduction = false

# LogLevel is "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR" or "ASSERT"
developmentLogLevel = DEBUG
productionLogLevel = ERROR

fcmSenderId = your FCM sender ID
```

3. Manually `takeOff` in the application's `onCreate`

```java
@Override
public void onCreate() {
    super.onCreate();

    ...

    // take off UrbanAirship SDK
    UAirship.takeOff(this, this);
}
```

or add `Autopilot` configuration to `AndroidManifest.xml`

```xml
<meta-data android:name="com.urbanairship.autopilot"
          android:value="com.urbanairship.Autopilot"/>
```

4. Track `Airship` events in your ZoneEntry/ZoneExits

```java
@Override
public void onZoneEntryEvent(@NotNull ZoneEntryEvent entryEvent, @NotNull Context context) {
    sendCustomEvent("bluedot_place_entered", entryEvent.getZoneInfo(), entryEvent.getZoneInfo().getCustomData());
    ...
}


 @Override
public void onZoneExitEvent(@NotNull ZoneExitEvent exitEvent, @NotNull Context context) {
    sendCustomEvent("bluedot_place_exited", exitEvent.getZoneInfo(), exitEvent.getDwellTime(), exitEvent.getZoneInfo().getCustomData());
    ...
}

 private void sendCustomEvent(String eventName, ZoneInfo zoneInfo, int dwellTime, Map<String, String> customDataMap) {
        //        name: "bluedot_place_exited"
        //        interaction_type: "location"
        //        interaction_id: zone_id
        //        properties: {
        //            bluedot_zone_name: <zone_name>
        //                    dwell_time: <dwell_time>
        //  <all custom data>
        //        }
        CustomEvent.Builder builder = new CustomEvent.Builder(eventName);
        builder.setInteraction("location", zoneInfo.getZoneId());
        builder.addProperty("bluedot_zone_name", zoneInfo.getZoneName());
        if (customDataMap != null && !customDataMap.isEmpty()) {
            for (Map.Entry<String, String> data : customDataMap.entrySet()) {
                builder.addProperty(data.getKey(), data.getValue());
            }

        }


        if (dwellTime != -1) {
            builder.addProperty("dwell_time", dwellTime);
        }
        CustomEvent event = builder.build();

        System.out.println("-- event data : " + event.toJsonValue());
        event.track();
    }


```

## Next steps
Full documentation can be found at https://docs.bluedot.io/airship-integration/urban-airship-android-integration/ and https://docs.airship.com/platform/android/getting-started/ respectively.
