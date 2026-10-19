package com.pavelgutin.pester.nag

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pavelgutin.pester.PesterApp
import com.pavelgutin.pester.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

const val EXTRA_SERVICE_NAG_ID = "service_nag_id"
const val EXTRA_SERVICE_NAG_LABEL = "service_nag_label"

/** How often the nag notification is re-posted while active. */
const val NAG_INTERVAL_MILLIS = 30_000L

private const val FGS_NOTIFICATION_ID = 1
private const val NAG_NOTIFICATION_ID = 2

/**
 * Keeps re-posting a silent notification every [NAG_INTERVAL_MILLIS] so the paired watch
 * keeps buzzing, while the phone itself never makes a sound or vibrates. The only way to
 * stop it is [DismissActivity] calling [stopNagging] on this bound service.
 */
class NagForegroundService : Service() {

    private val scopeJob = SupervisorJob()
    private val scope = CoroutineScope(scopeJob)
    private var loopJob: Job? = null

    private var activeNagId: Long = -1
    private var activeLabel: String = "Pester"

    inner class LocalBinder : Binder() {
        fun service(): NagForegroundService = this@NagForegroundService
    }

    private val binder = LocalBinder()
    override fun onBind(intent: Intent): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val nagId = intent?.getLongExtra(EXTRA_SERVICE_NAG_ID, -1L) ?: -1L
        val label = intent?.getStringExtra(EXTRA_SERVICE_NAG_LABEL) ?: "Pester"
        activeNagId = nagId
        activeLabel = label

        startForeground(FGS_NOTIFICATION_ID, buildForegroundNotification())
        startLoop()
        return START_REDELIVER_INTENT
    }

    fun currentLabel(): String = activeLabel
    fun currentNagId(): Long = activeNagId

    fun stopNagging() {
        loopJob?.cancel()
        NotificationManagerCompat.from(this).cancel(NAG_NOTIFICATION_ID)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLoop() {
        loopJob?.cancel()
        loopJob = scope.launch {
            val manager = NotificationManagerCompat.from(this@NagForegroundService)
            while (isActive) {
                manager.cancel(NAG_NOTIFICATION_ID)
                manager.notify(NAG_NOTIFICATION_ID, buildNagNotification())
                delay(NAG_INTERVAL_MILLIS)
            }
        }
    }

    private fun buildNagNotification(): Notification {
        val dismissIntent = Intent(this, DismissActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, PesterApp.NAG_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosquito)
            .setContentTitle(activeLabel)
            .setContentText("Still buzzing — open Pester to swat it.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setSound(null)
            .setVibrate(longArrayOf(0L))
            .build()
    }

    private fun buildForegroundNotification(): Notification =
        NotificationCompat.Builder(this, PesterApp.FGS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_mosquito)
            .setContentTitle(getString(R.string.fgs_channel_name))
            .setContentText(activeLabel)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSound(null)
            .setVibrate(longArrayOf(0L))
            .build()

    override fun onDestroy() {
        scopeJob.cancel()
        super.onDestroy()
    }

    companion object {
        fun bindingIntent(context: Context) = Intent(context, NagForegroundService::class.java)
    }
}
