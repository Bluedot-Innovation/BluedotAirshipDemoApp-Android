package io.bluedot.airshipdemo

import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.urbanairship.channel.AirshipChannelListener
import io.bluedot.airshipdemo.ui.MainScreen
import io.bluedot.airshipdemo.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.lifecycleScope
import com.urbanairship.UAirship
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val _channelId = MutableStateFlow(if (UAirship.isFlying()) UAirship.shared().channel.id else "")
    val channelId: StateFlow<String?> = _channelId.asStateFlow()

    private val channelListener = AirshipChannelListener {
        Log.d(TAG, "App.ChannelListener: ${UAirship.shared().channel.id}")
        _channelId.value = UAirship.shared().channel.id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            // wait for Airship to be flying before checking channel status
            UAirship.shared { airship ->
                if (airship.channel.id != null) {
                    Log.d(TAG, "Airship channel already exists: ${airship.channel.id}")
                    _channelId.value = airship.channel.id
                } else {
                    Log.d(TAG, "Airship is flying but no channel yet, adding channel listener")
                    airship.channel.addChannelListener(channelListener)
                }
            }
        }

//        UAirship.shared(Looper.getMainLooper()) { airship ->
//            if (airship.channel.id != null) {
//                Log.d(TAG, "Airship channel already exists: ${airship.channel.id}")
//                _channelId.value = airship.channel.id
//            } else {
//                Log.d(TAG, "Airship is flying but no channel yet, adding channel listener")
//                airship.channel.addChannelListener(channelListener)
//            }
//        }

//        if (UAirship.shared().channel.id != null) {
//            Log.d(TAG, "Airship channel already exists: ${UAirship.shared().channel.id}")
//            _channelId.value = UAirship.shared().channel.id
//        } else {
//            Log.d(TAG, "Airship is flying but no channel yet, adding channel listener")
//            UAirship.shared().channel.addChannelListener(channelListener)
//        }
        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val mainApplication = applicationContext as MainApplication
                    val channel by channelId.collectAsState()
                    val isAirshipInitialized by mainApplication.isAirshipInitialized.collectAsState()
                    val isPointSdkInitialized by mainApplication.isPointSdkInitialized.collectAsState()
                    MainScreen(
                        channelId = channel,
                        isAirshipInitialized = isAirshipInitialized,
                        isPointSdkInitialized = isPointSdkInitialized,
                        onInitAirship = { airshipAppKey, airshipAppSecret, airshipSite ->
                            mainApplication.initAirship(airshipAppKey, airshipAppSecret, airshipSite)
                        },
                        onInitPointSdk = { projectId, baseUrl ->
                            mainApplication.safeInitPointSDK(projectId, baseUrl)
                        },
                        onReset = { mainApplication.stopGeoTrigger() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (UAirship.isFlying()) {
            UAirship.shared().channel.removeChannelListener(channelListener)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
