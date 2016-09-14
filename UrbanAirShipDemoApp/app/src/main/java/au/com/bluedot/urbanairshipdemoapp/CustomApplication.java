package au.com.bluedot.urbanairshipdemoapp;

import android.app.Application;

import com.urbanairship.UAirship;

import au.com.bluedot.point.net.engine.ServiceManager;

/**
 * Created by Adil Bhatti on 17/05/16.
 */
public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Start urban air ship
        UAirship.takeOff(this, new UAirship.OnReadyCallback() {
            @Override
            public void onAirshipReady(UAirship uAirship) {

                uAirship.getPushManager().setUserNotificationsEnabled(true);

            }
        });


    }

}
