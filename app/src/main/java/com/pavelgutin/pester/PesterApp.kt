package com.pavelgutin.pester

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class PesterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createChannels()
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java)

        // Deliberately silent: no sound, no vibration pattern. The phone stays quiet;
        // the paired watch vibrates on its own terms when this notification is mirrored to it.
        val nagChannel = NotificationChannel(
            NAG_CHANNEL_ID, getString(R.string.nag_channel_name), NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = getString(R.string.nag_channel_description)
            setSound(null, null)
            vibrationPattern = null
            enableVibration(false)
        }

        val fgsChannel = NotificationChannel(
            FGS_CHANNEL_ID, getString(R.string.fgs_channel_name), NotificationManager.IMPORTANCE_MIN
        ).apply {
            setSound(null, null)
            vibrationPattern = null
            enableVibration(false)
        }

        manager.createNotificationChannel(nagChannel)
        manager.createNotificationChannel(fgsChannel)
    }

    companion object {
        const val NAG_CHANNEL_ID = "pester_nag"
        const val FGS_CHANNEL_ID = "pester_fgs"
    }
}
