package io.bluedot.airshipdemo.pointsdk

import android.content.Context
import android.util.Log
import android.widget.Toast
import au.com.bluedot.point.api.push.model.RezolvePushData
import com.rezolve.pushnotifications.PushNotificationsEventReceiver

class MyPushNotificationsEventReceiver : PushNotificationsEventReceiver() {

    override fun onNotificationReceived(rezolvePushData: RezolvePushData, context: Context) {
        Log.d(TAG, "Received push notification: ${rezolvePushData.title}")
        Toast.makeText(context, "Received push: ${rezolvePushData.title}", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationClicked(rezolvePushData: RezolvePushData, context: Context) {
        Log.d(TAG, "Clicked push notification: ${rezolvePushData.title}")
        Toast.makeText(context, "Push clicked: ${rezolvePushData.title}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TAG = "MyPushNotificationsEventReceiver"
    }
}
