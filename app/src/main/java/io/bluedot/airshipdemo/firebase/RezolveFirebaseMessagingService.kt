package io.bluedot.airshipdemo.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.urbanairship.push.fcm.AirshipFirebaseIntegration

class RezolveFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        AirshipFirebaseIntegration.processNewToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        AirshipFirebaseIntegration.processMessageSync(applicationContext, remoteMessage)
    }
}
