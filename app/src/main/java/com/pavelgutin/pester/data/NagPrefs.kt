package com.pavelgutin.pester.data

import android.content.Context

/**
 * How often the nag notification re-fires once triggered. Not governed by any
 * AlarmManager interval floor — the repeat loop runs as a coroutine delay inside
 * the foreground service, not as repeating AlarmManager alarms, so Android's
 * historical ~60s minimum for setRepeating() doesn't apply here. Android's
 * notification-enqueue rate limiter (AOSP default ~10/sec) doesn't apply either
 * at these intervals. The floor below is a sanity bound, not an OS requirement.
 */
object NagPrefs {
    const val DEFAULT_INTERVAL_SECONDS = 30
    const val MIN_INTERVAL_SECONDS = 5
    const val MAX_INTERVAL_SECONDS = 300

    private const val PREFS_NAME = "pester_prefs"
    private const val KEY_INTERVAL_SECONDS = "nag_interval_seconds"

    fun getIntervalSeconds(context: Context): Int =
        prefs(context).getInt(KEY_INTERVAL_SECONDS, DEFAULT_INTERVAL_SECONDS)

    fun setIntervalSeconds(context: Context, seconds: Int) {
        prefs(context).edit()
            .putInt(KEY_INTERVAL_SECONDS, seconds.coerceIn(MIN_INTERVAL_SECONDS, MAX_INTERVAL_SECONDS))
            .apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
