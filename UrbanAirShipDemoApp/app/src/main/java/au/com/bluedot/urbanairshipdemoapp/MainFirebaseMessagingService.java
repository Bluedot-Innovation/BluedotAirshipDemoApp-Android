package au.com.bluedot.urbanairshipdemoapp;

import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.urbanairship.push.fcm.AirshipFirebaseInstanceIdService;
import com.urbanairship.push.fcm.AirshipFirebaseMessagingService;

/**
 * Created by Adil Bhatti on 7/11/18.
 */
public class MainFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        System.out.println("-- onNewToken: " + s);
        AirshipFirebaseInstanceIdService.processTokenRefresh(this);

    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        AirshipFirebaseMessagingService.processMessageSync(this, remoteMessage);
        System.out.println("-- onMessageReceived: " + remoteMessage.toString());
    }

    @Override
    public void onSendError(String s, Exception e) {
        super.onSendError(s, e);
        System.out.println("-- onSendError: " + s + " e: " + e.getMessage());
    }
}
