package io.bluedot.airshipdemo.utilities

import android.content.Context
import androidx.core.content.edit

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

    companion object Companion {
        private const val PREFS_NAME = "bluedot_prefs"
        private const val KEY_PROJECT_ID = "project_id"
        private const val KEY_AIRSHIP_APP_KEY = "airship_app_key"
        private const val KEY_AIRSHIP_APP_SECRET = "airship_app_secret"
    }
}
