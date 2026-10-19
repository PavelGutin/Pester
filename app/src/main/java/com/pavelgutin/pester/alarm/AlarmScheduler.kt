package com.pavelgutin.pester.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.pavelgutin.pester.data.NagEntity
import com.pavelgutin.pester.data.NagType
import com.pavelgutin.pester.ui.MainActivity
import java.time.DayOfWeek
import java.time.ZonedDateTime
import java.time.ZoneId

const val EXTRA_NAG_ID = "nag_id"
const val EXTRA_NAG_LABEL = "nag_label"
const val EXTRA_NAG_TYPE = "nag_type"

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(nag: NagEntity) {
        if (!nag.enabled) {
            cancel(nag)
            return
        }
        val triggerAt = nextTriggerMillis(nag) ?: return

        val intent = Intent(context, NagTriggerReceiver::class.java).apply {
            putExtra(EXTRA_NAG_ID, nag.id)
            putExtra(EXTRA_NAG_LABEL, nag.label)
            putExtra(EXTRA_NAG_TYPE, nag.type.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, nag.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val showIntent = PendingIntent.getActivity(
            context, nag.id.toInt(), Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showIntent),
            pendingIntent
        )
    }

    fun cancel(nag: NagEntity) {
        val intent = Intent(context, NagTriggerReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, nag.id.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun canScheduleExact(): Boolean = alarmManager.canScheduleExactAlarms()

    /** Next epoch-millis this Nag should fire at, or null if it has nothing left to do (a past one-shot). */
    private fun nextTriggerMillis(nag: NagEntity): Long? {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)

        return when (nag.type) {
            NagType.REMINDER -> nag.oneShotEpochMillis?.takeIf { it > now.toInstant().toEpochMilli() }

            NagType.ALARM -> {
                val days = nag.daysOfWeek.ifEmpty { DayOfWeek.entries.toSet() }
                var candidate = now.withHour(nag.hour).withMinute(nag.minute).withSecond(0).withNano(0)
                if (!candidate.isAfter(now)) candidate = candidate.plusDays(1)
                while (candidate.dayOfWeek !in days) candidate = candidate.plusDays(1)
                candidate.toInstant().toEpochMilli()
            }
        }
    }
}
