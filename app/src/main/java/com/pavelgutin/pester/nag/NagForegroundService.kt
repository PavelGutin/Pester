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
import com.pavelgutin.pester.data.NagPrefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

const val EXTRA_SERVICE_NAG_ID = "service_nag_id"
const val EXTRA_SERVICE_NAG_LABEL = "service_nag_label"

private const val FGS_NOTIFICATION_ID = 1

/**
 * Base for the nag notification's ID, which increments every cycle (see [startLoop]).
 * Android's notification-enqueue rate limiter specifically throttles repeated *updates*
 * to an existing notification ID, not fresh ones — and some watch-side notification
 * relays (Garmin included) appear to de-dup re-posts of the same ID as "the same
 * notification coming back" rather than something new worth buzzing for again. Using a
 * new ID every cycle sidesteps both: each post is unambiguously a brand-new notification.
 */
private const val NAG_NOTIFICATION_ID_BASE = 1000

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
    private var lastPostedNotificationId: Int? = null

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
        lastPostedNotificationId?.let { NotificationManagerCompat.from(this).cancel(it) }
        lastPostedNotificationId = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startLoop() {
        loopJob?.cancel()
        val intervalMillis = NagPrefs.getIntervalSeconds(this) * 1_000L
        loopJob = scope.launch {
            val manager = NotificationManagerCompat.from(this@NagForegroundService)
            var nextId = NAG_NOTIFICATION_ID_BASE
            while (isActive) {
                val id = nextId++
                lastPostedNotificationId?.let { manager.cancel(it) }
                manager.notify(id, buildNagNotification())
                lastPostedNotificationId = id
                delay(intervalMillis)
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
