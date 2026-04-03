package io.bluedot.airshipdemo.utilities

import android.content.Context
import androidx.core.content.edit
import com.urbanairship.AirshipConfigOptions

class RezolvePreferences(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var projectId: String
        get() = prefs.getString(KEY_PROJECT_ID, "") ?: ""
        set(value) = prefs.edit { putString(KEY_PROJECT_ID, value) }

    var airshipAppKey: String
        get() = prefs.getString(KEY_AIRSHIP_APP_KEY, "") ?: ""
        set(value) = prefs.edit { putString(KEY_AIRSHIP_APP_KEY, value) }

    var airshipAppSecret: String
        get() = prefs.getString(KEY_AIRSHIP_APP_SECRET, "") ?: ""
        set(value) = prefs.edit { putString(KEY_AIRSHIP_APP_SECRET, value) }

    var airshipSite: String
        get() = prefs.getString(KEY_AIRSHIP_SITE, AirshipConfigOptions.Site.SITE_EU.name) ?: AirshipConfigOptions.Site.SITE_EU.name
        set(value) = prefs.edit { putString(KEY_AIRSHIP_SITE, value) }

    var baseUrl: String
        get() = prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL
        set(value) = prefs.edit { putString(KEY_BASE_URL, value) }

    companion object Companion {
        private const val PREFS_NAME = "bluedot_prefs"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_AIRSHIP_APP_KEY = "airship_app_key"
        private const val KEY_AIRSHIP_APP_SECRET = "airship_app_secret"
        private const val KEY_AIRSHIP_SITE = "airship_site"
        private const val KEY_BASE_URL = "base_url"
        const val DEFAULT_BASE_URL = "https://globalconfig.dev-bluedot.com/"
        const val ALTERNATIVE_BASE_URL = "https://globalconfig.bluedot.io/"
    }
}
