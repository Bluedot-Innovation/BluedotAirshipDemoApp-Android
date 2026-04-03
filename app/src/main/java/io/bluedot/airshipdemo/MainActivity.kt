package io.bluedot.airshipdemo

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.urbanairship.Airship
import com.urbanairship.AirshipStatus
import com.urbanairship.channel.AirshipChannelListener
import io.bluedot.airshipdemo.ui.MainScreen
import io.bluedot.airshipdemo.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val _channelId = MutableStateFlow(if (Airship.isFlying) Airship.channel.id else "")
    val channelId: StateFlow<String?> = _channelId.asStateFlow()

    private val channelListener = AirshipChannelListener {
        Log.d(TAG, "App.ChannelListener: ${Airship.channel.id}")
        _channelId.value = Airship.channel.id
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            // wait for Airship to be flying before checking channel status
            Airship.statusFlow.first { it == AirshipStatus.IS_FLYING }

            if (Airship.channel.id != null) {
                Log.d(TAG, "Airship channel already exists: ${Airship.channel.id}")
                _channelId.value = Airship.channel.id
            } else {
                Log.d(TAG, "Airship is flying but no channel yet, adding channel listener")
                Airship.channel.addChannelListener(channelListener)
            }
        }
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
        if (Airship.isFlying) {
            Airship.channel.removeChannelListener(channelListener)
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
