package au.com.bluedot.urbanairshipdemoapp;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.urbanairship.push.fcm.AirshipFirebaseIntegration;
import org.jetbrains.annotations.NotNull;


public class MainFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NotNull String s) {
        super.onNewToken(s);
        System.out.println("-- onNewToken: " + s);
        AirshipFirebaseIntegration.processNewToken(this);

    }


    @Override
    public void onMessageReceived(@NotNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        AirshipFirebaseIntegration.processMessageSync(this, remoteMessage);
        System.out.println("-- onMessageReceived: " + remoteMessage.toString());
    }

    @Override
    public void onSendError(@NotNull String s, @NotNull Exception e) {
        super.onSendError(s, e);
        System.out.println("-- onSendError: " + s + " e: " + e.getMessage());
    }
}
