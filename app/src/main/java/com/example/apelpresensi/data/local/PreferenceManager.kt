package com.example.apelpresensi.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.apelpresensi.util.Constants

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuthToken(token: String) {
        prefs.edit().putString(Constants.KEY_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(Constants.KEY_TOKEN, null)
    }

    fun saveUserData(username: String, role: String) {
        prefs.edit().apply {
            putString(Constants.KEY_USERNAME, username)
            putString(Constants.KEY_ROLE, role)
        }.apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}