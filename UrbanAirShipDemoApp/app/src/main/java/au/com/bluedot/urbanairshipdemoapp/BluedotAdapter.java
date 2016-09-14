package au.com.bluedot.urbanairshipdemoapp;

import android.content.Context;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import com.urbanairship.UAirship;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.bluedot.application.model.Proximity;
import au.com.bluedot.application.model.geo.Fence;
import au.com.bluedot.model.geo.LineString;
import au.com.bluedot.point.ApplicationNotificationListener;
import au.com.bluedot.point.ServiceStatusListener;
import au.com.bluedot.point.net.engine.BDError;
import au.com.bluedot.point.net.engine.BeaconInfo;
import au.com.bluedot.point.net.engine.ServiceManager;
import au.com.bluedot.point.net.engine.ZoneInfo;


/*
 * @author Bluedot Innovation
 * Copyright (c) 2016 Bluedot Innovation. All rights reserved.
 * BluedotAdapter that interfaces Bluedot SDK functionality with Urban Airship Services
 */
public class BluedotAdapter {

    private static BluedotAdapter instance;
    private ServiceManager serviceManager;
    private Context mContext;
    private Handler handler;
    private final long TAG_EXPIRY_ms = 7000;

    /**
     * ServiceStatusListener listens to Bluedot service's status changes
     */
    private ServiceStatusListener serviceStatusListener = new ServiceStatusListener() {
        /**
         * It is called when BlueDotPointService started successful, application logic code using the Bluedot service could start from here.
         * This method is off the UI thread.
         */
        @Override
        public void onBlueDotPointServiceStartedSuccess() {
            serviceManager.subscribeForApplicationNotification(applicationNotificationListener);
        }

        /**
         * This method notifies the client application that BlueDotPointService is stopped. Application could release the resources related to Bluedot service from here.
         * It is called off the UI thread.
         */
        @Override
        public void onBlueDotPointServiceStop() {
            serviceManager.unsubscribeForApplicationNotification(applicationNotificationListener);
        }

        /**
         * The method delivers the error from BlueDotPointService by a generic BDError. There are several types of error such as
         * - BDAuthenticationError (fatal)
         * - BDNetworkError (fatal / non fatal)
         * - LocationServiceNotEnabledError (fatal / non fatal)
         * - RuleDownloadError (non fatal)
         * - BLENotAvailableError (non fatal)
         * - BluetoothNotEnabledError (non fatal)
         * The BDError.isFatal() indicates if error is fatal and service is not operable.
         * Followed by onBlueDotPointServiceStop() indicating service is stopped.
         * The BDError.getReason() is useful to analyse error cause.
         * @param bdError
         */
        @Override
        public void onBlueDotPointServiceError(BDError bdError) {
            System.out.println(bdError);
        }

        /**
         * The method deliveries the ZoneInfo list when the rules are updated. Application is able to get the latest ZoneInfo when the rules are updated.
         * @param zoneInfoList
         */
        @Override
        public void onRuleUpdate(List<ZoneInfo> zoneInfoList) {

        }
    };

    /**
     * This callback interface is used to subscribe to receive ApplicationNotification
     */
    private ApplicationNotificationListener applicationNotificationListener = new ApplicationNotificationListener() {

        /**
         * This callback happens when user is subscribed to Application Notification
         * and check into any fence under that Zone
         * @param fence      - Fence triggered
         * @param zoneInfo   - Zone information Fence belongs to
         * @param location   - geographical coordinate where trigger happened
         * @param isCheckOut - CheckOut will be tracked and delivered once device left the Fence
         */
        @Override
        public void onCheckIntoFence(final Fence fence, final ZoneInfo zoneInfo, Location location, boolean isCheckOut) {
            UAirship.shared().getPushManager().editTags()
                    .addTag("zone_" + zoneInfo.getZoneName())
                    .addTag("fence_" + fence.getName())
                    .apply();

            if(fence.getGeometry() instanceof LineString || isCheckOut==false) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UAirship.shared().getPushManager().editTags()
                                .removeTag("zone_" + zoneInfo.getZoneName())
                                .removeTag("fence_" + fence.getName())
                                .apply();
                    }
                },TAG_EXPIRY_ms);

            }

        }

        /**
         * This callback happens when user is subscribed to Application Notification
         * and checked out from fence under that Zone
         * @param fence     - Fence user is checked out from
         * @param zoneInfo  - Zone information Fence belongs to
         * @param dwellTime - time spent inside the Fence; in minutes
         */
        @Override
        public void onCheckedOutFromFence(Fence fence, ZoneInfo zoneInfo, int dwellTime) {
            UAirship.shared().getPushManager().editTags()
                    .removeTag("zone_" + zoneInfo.getZoneName())
                    .removeTag("fence_" + fence.getName())
                    .apply();
        }

        /**
         * This callback happens when user is subscribed to Application Notification
         * and check into any beacon under that Zone
         * @param beaconInfo - Beacon triggered
         * @param zoneInfo   - Zone information Beacon belongs to
         * @param location   - geographical coordinate where trigger happened
         * @param proximity  - the proximity at which the trigger occurred
         * @param isCheckOut - CheckOut will be tracked and delivered once device left the Beacon advertisement range
         */
        @Override
        public void onCheckIntoBeacon(final BeaconInfo beaconInfo, final ZoneInfo zoneInfo, Location location, Proximity proximity, boolean isCheckOut) {
            UAirship.shared().getPushManager().editTags()
                    .addTag("zone_" + zoneInfo.getZoneName())
                    .addTag("beacon_" + beaconInfo.getName())
                    .apply();

            if(isCheckOut==false) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UAirship.shared().getPushManager().editTags()
                                .removeTag("zone_" + zoneInfo.getZoneName())
                                .removeTag("beacon_" + beaconInfo.getName())
                                .apply();
                    }
                },TAG_EXPIRY_ms);
            }
        }

        /**
         * This callback happens when user is subscribed to Application Notification
         * and checked out from beacon under that Zone
         * @param beaconInfo - Beacon is checked out from
         * @param zoneInfo   - Zone information Beacon belongs to
         * @param dwellTime  - time spent inside the Beacon area; in minutes
         */
        @Override
        public void onCheckedOutFromBeacon(BeaconInfo beaconInfo, ZoneInfo zoneInfo, int dwellTime) {
            UAirship.shared().getPushManager().editTags()
                    .removeTag("zone_" + zoneInfo.getZoneName())
                    .removeTag("beacon_" + beaconInfo.getName())
                    .apply();
        }

    };

    /**
     * Constructor
     * @param context - Context from the Activity called
     */
    private BluedotAdapter(Context context) {
        mContext = context;
        handler = new Handler(Looper.getMainLooper());
    }

    /**
     * Get instance of the Adapter
     * @param context - Context from the Activity called
     * @return - instance of Adapter
     */
    public static BluedotAdapter getInstance(Context context) {
        if(instance == null) {
            instance = new BluedotAdapter(context);
        }
        return instance;
    }

    /**
     * This method invokes SDK authentication with custom URL
     *
     * @param packageName The package name of your app created in the Bluedot Point Access
     * @param apiKey      The API key generated for your app in the Bluedot Point Access
     * @param userName    The user name you used to login to the Bluedot Point Access
     * @param url         The end point url
     * @param restartMode Service will be restarted if the app is killed by the Android OS
     */
    public void startSDK(String packageName, String apiKey, String userName, String url, boolean restartMode) {

        if (mContext != null) {
            serviceManager = ServiceManager.getInstance(mContext);
            if (!serviceManager.isBlueDotPointServiceRunning()) {

                serviceManager.sendAuthenticationRequest(packageName, apiKey, userName, serviceStatusListener, restartMode, url);

            }
        }
    }

    /**
     * This method invokes SDK authentication
     *
     * @param packageName The package name of your app created in the Bluedot Point Access
     * @param apiKey      The API key generated for your app in the Bluedot Point Access
     * @param userName    The user name you used to login to the Bluedot Point Access
     * @param restartMode Service will be restarted if the app is killed by the Android OS
     */
    public void startSDK(String packageName, String apiKey, String userName, boolean restartMode) {

        if (mContext != null) {
            serviceManager = ServiceManager.getInstance(mContext);
            if (!serviceManager.isBlueDotPointServiceRunning()) {

                serviceManager.sendAuthenticationRequest(packageName, apiKey, userName, serviceStatusListener, restartMode);

            }
        }
    }


    /**
     * This method stops the SDK service
     */
    public void stopSDK() {
        if (serviceManager != null && serviceManager.isBlueDotPointServiceRunning()) {
            serviceManager.stopPointService();
        }


    }

    /**
     * This method checks whether the service is running
     *
     * @return - status of service
     */
    public boolean isServiceRunning() {
        try {
            if (serviceManager.isBlueDotPointServiceRunning()) {
                return true;
            }
            return false;

        } catch (Exception e) {
            return false;
        }

    }
}
