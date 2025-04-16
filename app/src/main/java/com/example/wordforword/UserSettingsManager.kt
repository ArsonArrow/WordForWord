package com.example.wordforword

import android.content.Context
import android.content.SharedPreferences

object UserSettingsManager {

    private const val PREF_NAME = "user_settings"
    private const val KEY_ERROR_THRESHOLD = "error_threshold"
    private const val KEY_PRACTICE_MODE = "practice_mode"

    fun saveErrorThreshold(context: Context, threshold: Float) {
        val prefs = getPrefs(context)
        prefs.edit().putFloat(KEY_ERROR_THRESHOLD, threshold).apply()
    }

    fun getErrorThreshold(context: Context): Float {
        return getPrefs(context).getFloat(KEY_ERROR_THRESHOLD, 0.30f) // Default is 30%
    }

    fun savePracticeMode(context: Context, mode: PracticeMode) {
        val prefs = getPrefs(context)
        prefs.edit().putString(KEY_PRACTICE_MODE, mode.name).apply()
    }

    fun getPracticeMode(context: Context): PracticeMode {
        val modeName = getPrefs(context).getString(KEY_PRACTICE_MODE, PracticeMode.FIRST_LETTER.name)
        return PracticeMode.valueOf(modeName!!)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
}
