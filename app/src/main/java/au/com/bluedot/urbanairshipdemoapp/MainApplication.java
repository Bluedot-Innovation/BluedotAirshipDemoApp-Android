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
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import au.com.bluedot.point.net.engine.GeoTriggeringService;
import au.com.bluedot.point.net.engine.InitializationResultListener;
import au.com.bluedot.point.net.engine.ServiceManager;
import com.urbanairship.UAirship;

import static android.app.Notification.PRIORITY_MAX;

/**
 * Created by Adil Bhatti on 17/05/16.
 */
public class MainApplication extends Application implements UAirship.OnReadyCallback{

    private ServiceManager serviceManager;
    private final String PROJECT_ID = "<PROJECT-ID>"; //ProjectID for the App  from Canvas
    private NotificationChannel notificationChannel = null;


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

    void reset() {
        serviceManager.reset(bdError -> {
            String text = "Reset Finished ";
            if(bdError != null)
                text = text + bdError.getReason();
            else {
                text = text + "Success ";
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            Toast.makeText(getApplicationContext(),text,Toast.LENGTH_LONG).show();
        });
    }

    void startGeoTrigger() {
        Notification notification = createNotification();

        GeoTriggeringService.builder()
                .notification(notification)
                .start(this, geoTriggerError -> {
                    if (geoTriggerError != null) {
                        Toast.makeText(getApplicationContext(),"Error in starting GeoTrigger"+geoTriggerError.getReason(),Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(getApplicationContext(),"GeoTrigger started successfully",Toast.LENGTH_LONG).show();

                });
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
