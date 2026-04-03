package io.bluedot.airshipdemo

import android.app.Application
import android.util.Log
import android.widget.Toast
import au.com.bluedot.point.net.engine.GeoTriggeringService
import au.com.bluedot.point.net.engine.GeoTriggeringStatusListener
import au.com.bluedot.point.net.engine.InitializationResultListener
import au.com.bluedot.point.net.engine.ServiceManager
import com.urbanairship.Airship
import io.bluedot.airshipdemo.airship.AirshipAutopilot
import io.bluedot.airshipdemo.utilities.RezolvePreferences
import io.bluedot.airshipdemo.utilities.createNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainApplication : Application() {
    private lateinit var serviceManager: ServiceManager

    private val preferences by lazy { RezolvePreferences(this) }

    private val _isAirshipInitialized = MutableStateFlow(false)
    val isAirshipInitialized: StateFlow<Boolean> = _isAirshipInitialized.asStateFlow()

    private val _isPointSdkInitialized = MutableStateFlow(false)
    val isPointSdkInitialized: StateFlow<Boolean> = _isPointSdkInitialized.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        _isPointSdkInitialized.value = ServiceManager.getInstance(this).isBluedotServiceInitialized
    }

    fun getSavedProjectId(): String = preferences.projectId
    fun getSavedAirshipAppKey(): String = preferences.airshipAppKey
    fun getSavedAirshipAppSecret(): String = preferences.airshipAppSecret
    fun getSavedAirshipSite(): String = preferences.airshipSite
    fun getSavedBaseUrl(): String = preferences.baseUrl

    fun initAirship(airshipAppKey: String, airshipAppSecret: String, airshipSite: String) {
        preferences.airshipAppKey = airshipAppKey
        preferences.airshipAppSecret = airshipAppSecret
        preferences.airshipSite = airshipSite

        if (!Airship.isFlyingOrTakingOff) {
            Airship.takeOff(
                application = this,
                options = AirshipAutopilot.makeAirshipConfigOptions(applicationContext)
            ) {
                Log.d(TAG, "Airship takeoff!")
                AirshipAutopilot.airshipReady()
                _isAirshipInitialized.value = true
            }
        } else {
            Log.d(TAG, "Airship already flying!")
            _isAirshipInitialized.value = true
        }
    }

    fun safeInitPointSDK(projectId: String, baseUrl: String) {
        preferences.projectId = projectId
        preferences.baseUrl = baseUrl

        serviceManager = ServiceManager.getInstance(this)

        if (serviceManager.isBluedotServiceInitialized) {
            serviceManager.reset { bdError ->
                if (bdError != null) {
                    Log.d(TAG, "Reset failed: ${bdError.reason}")
                    Toast.makeText(applicationContext, "Reset failed: ${bdError.reason}", Toast.LENGTH_LONG).show()
                } else {
                    _isPointSdkInitialized.value = false
                    Toast.makeText(applicationContext, "Bluedot SDK reset successfully", Toast.LENGTH_LONG).show()
                    initPointSDK(projectId, baseUrl)
                }
            }
        } else {
            initPointSDK(projectId, baseUrl)
        }
    }

    private fun initPointSDK(projectId: String, baseUrl: String) {
        val resultListener = InitializationResultListener { bdError ->
            var text = "Initialization Result "
            if (bdError != null) text += bdError.reason else {
                text += "Success "
                _isPointSdkInitialized.value = true
                startGeoTrigger()
            }
            Log.d(TAG, "PointSDK Initialization Result: $text")
            Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
        }
        serviceManager.initialize(projectId, baseUrl, resultListener)
    }

    fun stopGeoTrigger() {
        GeoTriggeringService.stop(applicationContext, geoTriggeringStatusListener)
    }

    val geoTriggeringStatusListener = GeoTriggeringStatusListener { error ->
        Log.d(TAG, "onGeoTriggeringResult: $error")
        if (error != null) {
            Toast.makeText(applicationContext, "Error in stopping GeoTrigger ${error.reason}", Toast.LENGTH_LONG).show()
        } else {
            _isPointSdkInitialized.value = false
        }
    }

    private fun startGeoTrigger() {
        val notification = createNotification(
            title = applicationContext.getString(R.string.foreground_notification_title),
            content = applicationContext.getString(R.string.foreground_notification_text),
            onGoing = true,
            context = applicationContext
        )
        GeoTriggeringService.builder()
            .notification(notification)
            .start(this) { geoTriggerError ->
                if (geoTriggerError != null) {
                    Toast.makeText(applicationContext, "Error in starting GeoTrigger ${geoTriggerError.reason}", Toast.LENGTH_LONG).show()
                    return@start
                }
                Toast.makeText(applicationContext, "GeoTrigger started successfully", Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        private const val TAG = "MainApplication"
    }
}
