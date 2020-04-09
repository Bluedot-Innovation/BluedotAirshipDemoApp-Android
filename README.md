# Airship Events Example

A sample project used to test the integration between Airship SDK and Bluedot Point SDK.

## Getting started

This project depends on `PointSDK-Android` and `urbanairship-fcm`. Both dependencies are managed by Gradle.

### Implement `PointSDK-Android`

1. Add `PointSDK-Android` module as a dependency to your application.

```groovy
dependencies {
    ...

    implementation 'com.gitlab.bluedotio.android:point_sdk_android:15.0.0'
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

    // start Point SDK
    boolean locationPermissionGranted =
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    boolean backgroundPermissionGranted = (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                    || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
    
    if (locationPermissionGranted && backgroundPermissionGranted) {
        serviceManager = ServiceManager.getInstance(this);

        if(!serviceManager.isBlueDotPointServiceRunning()) {
            // Setting Notification for foreground service, required for Android Oreo and above.
            // Setting targetAllAPIs to TRUE will display foreground notification for Android versions lower than Oreo
            serviceManager.setForegroundServiceNotification(createNotification(), false);
            serviceManager.sendAuthenticationRequest("Your Bluedot API key", this, false);
        }
    }
    else
    {
        requestPermissions();
    }
}
```

3. Implement `Point SDK` callbacks

```java
@Override
public void onCheckIntoFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, LocationInfolocationInfo, Map<String, String> customDataMap, boolean b) {
    ...
}

@Override
public void onCheckedOutFromFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, int dwellTime,Map<String, String> customDataMap) {
    ...
}

@Override
public void onCheckIntoBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, LocationInfolocationInfo, Proximity proximity, Map<String, String> customDataMap, boolean b) {
    ...
}

@Override
public void onCheckedOutFromBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, int dwellTime,Map<String, String> customDataMap) {
    ...
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

4. Track `Airship` events in your checkins/checkouts

```java
@Override
public void onCheckIntoFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, LocationInfo locationInfo, Map<String, String> customDataMap, boolean b) {
    CustomEvent.Builder builder = new CustomEvent.Builder("bluedot_place_entered");
    builder.setInteraction("location", zoneInfo.getZoneId());
    builder.addProperty("bluedot_zone_name", zoneInfo.getZoneName());
    if(customDataMap != null && !customDataMap.isEmpty()) {
        for(Map.Entry<String, String> data : customDataMap.entrySet()) {
            builder.addProperty(data.getKey(), data.getValue());
        }
    }

    if(dwellTime != -1) {
        builder.addProperty("dwell_time", dwellTime);
    }

    CustomEvent event = builder.build();
    event.track();
}

@Override
public void onCheckedOutFromFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, int dwellTime, Map<String, String> customDataMap) { {
    CustomEvent.Builder builder = new CustomEvent.Builder("bluedot_place_exited");
    builder.setInteraction("location", zoneInfo.getZoneId());
    builder.addProperty("bluedot_zone_name", zoneInfo.getZoneName());
    if(customDataMap != null && !customDataMap.isEmpty()) {
        for(Map.Entry<String, String> data : customDataMap.entrySet()) {
            builder.addProperty(data.getKey(), data.getValue());
        }
    }

    if(dwellTime != -1) {
        builder.addProperty("dwell_time", dwellTime);
    }

    CustomEvent event = builder.build();
    event.track();
}
```

## Next steps
Full documentation can be found at https://docs.bluedot.io/android-sdk/ and https://docs.airship.com/platform/android/getting-started/ respectively.
