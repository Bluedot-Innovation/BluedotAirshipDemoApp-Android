package io.bluedot.airshipdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import io.bluedot.airshipdemo.ui.MainScreen
import io.bluedot.airshipdemo.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val mainApplication = applicationContext as MainApplication
                    val channel by mainApplication.channelId.collectAsState()
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

    companion object {
        const val TAG = "MainActivity"
    }
}
