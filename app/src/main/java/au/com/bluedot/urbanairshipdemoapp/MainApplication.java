package au.com.bluedot.urbanairshipdemoapp;

import android.Manifest;
import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

import com.urbanairship.UAirship;
import com.urbanairship.analytics.CustomEvent;

import java.util.List;
import java.util.Map;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.FenceInfo;
import au.com.bluedot.point.net.engine.LocationInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;

import static android.app.Notification.PRIORITY_MAX;

/**
 * Created by Adil Bhatti on 17/05/16.
 */
public class MainApplication extends Application implements UAirship.OnReadyCallback, ServiceStatusListener, ApplicationNotificationListener {

    private ServiceManager serviceManager;
    private final String API_KEY = ""; //API key for the Point Demo App 
    private final boolean RESTART_MODE = false;
    private NotificationChannel notificationChannel = null;
    private final String EVENT_PLACE_ENTERED = "bluedot_place_entered";
    private final String EVENT_PLACE_EXITED = "bluedot_place_exited";


    @Override
    public void onCreate() {
        super.onCreate();

        //take off UrbanAirship SDK
        initUrbanAirshipSdk();

        //start Point SDK
        initPointSDK();
    }

    public void initPointSDK() {
        boolean locationPermissionGranted =
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        boolean backgroundPermissionGranted = (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                || ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (locationPermissionGranted && backgroundPermissionGranted) {
            serviceManager = ServiceManager.getInstance(this);

            if (!serviceManager.isBlueDotPointServiceRunning()) {
                // Setting Notification for foreground service, required for Android Oreo and above.
                // Setting targetAllAPIs to TRUE will display foreground notification for Android versions lower than Oreo
                serviceManager.setForegroundServiceNotification(createNotification(), false);
                serviceManager.sendAuthenticationRequest(API_KEY, this, RESTART_MODE);


            }
        } else {
            requestPermissions();
        }
    }


    private void initUrbanAirshipSdk() {
        UAirship.takeOff(this, this);
    }

    @Override
    public void onAirshipReady(UAirship uAirship) {
        System.out.println("-- onAirshipReady");
        uAirship.getPushManager().setUserNotificationsEnabled(true);

        //setting up notification channel
        if (notificationChannel != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                uAirship.getPushManager()
                        .getNotificationFactory()
                        .setNotificationChannel(notificationChannel.getName().toString());
            }
        }

    }


    @Override
    public void onCheckIntoFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, LocationInfo locationInfo, Map<String, String> customDataMap, boolean b) {
        // Toast.makeText(getApplicationContext(),"CheckIn "+zoneInfo.getZoneName(),Toast.LENGTH_SHORT).show();
        sendCustomEvent(EVENT_PLACE_ENTERED, zoneInfo, customDataMap);


    }

    private void sendCustomEvent(String eventName, ZoneInfo zoneInfo, Map<String, String> customDataMap) {
        sendCustomEvent(eventName, zoneInfo, -1, customDataMap);
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

    @Override
    public void onCheckedOutFromFence(FenceInfo fenceInfo, ZoneInfo zoneInfo, int dwellTime, Map<String, String> customDataMap) {
        sendCustomEvent(EVENT_PLACE_EXITED, zoneInfo, dwellTime, customDataMap);
    }

    @Override
    public void onCheckIntoBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, LocationInfo locationInfo, Proximity proximity, Map<String, String> customDataMap, boolean b) {
        sendCustomEvent(EVENT_PLACE_ENTERED, zoneInfo, customDataMap);
    }

    @Override
    public void onCheckedOutFromBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, int dwellTime, Map<String, String> customDataMap) {
        sendCustomEvent(EVENT_PLACE_EXITED, zoneInfo, dwellTime, customDataMap);
    }

    @Override
    public void onBlueDotPointServiceStartedSuccess() {
        System.out.println("-- onBlueDotPointServiceStartedSuccess");
        serviceManager.subscribeForApplicationNotification(this);
    }

    @Override
    public void onBlueDotPointServiceStop() {
        serviceManager.unsubscribeForApplicationNotification(this);
    }

    @Override
    public void onBlueDotPointServiceError(BDError bdError) {

    }

    @Override
    public void onRuleUpdate(List<ZoneInfo> list) {

    }

    /**
     * Creates notification channel and notification, required for foreground service notification.
     *
     * @return notification
     */
    private Notification createNotification() {

        String channelId, channelName;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channelId = "Bluedot" + getString(R.string.app_name);
            channelName = "Bluedot Service" + getString(R.string.app_name);
            notificationChannel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.enableLights(false);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(false);
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(notificationChannel);

            Notification.Builder notification = new Notification.Builder(getApplicationContext(), channelId)
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new Notification.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        } else {

            NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext())
                    .setContentTitle(getString(R.string.foreground_notification_title))
                    .setContentText(getString(R.string.foreground_notification_text))
                    .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.foreground_notification_text)))
                    .setOngoing(true)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .setPriority(PRIORITY_MAX)
                    .setSmallIcon(R.mipmap.ic_launcher);

            return notification.build();
        }
    }

    private void requestPermissions() {

        Intent intent = new Intent(getApplicationContext(), RequestPermissionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
