package io.bluedot.airshipdemo.firebase

import au.com.bluedot.point.net.engine.ServiceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.rezolve.pushnotifications.isRezolvePushNotification
import com.rezolve.pushnotifications.toRezolvePushData
import com.urbanairship.push.fcm.AirshipFirebaseIntegration

class RezolveFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AirshipFirebaseIntegration.processNewToken(applicationContext, token)
        ServiceManager.getInstance(this).pushNotificationsManager.onNewFcmToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        AirshipFirebaseIntegration.processMessageSync(applicationContext, remoteMessage)
        if (remoteMessage.isRezolvePushNotification()) {
            ServiceManager.getInstance(this).pushNotificationsManager.onMessageReceived(remoteMessage.toRezolvePushData())
        }
    }
}
