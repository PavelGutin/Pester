package com.pavelgutin.pester.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.pavelgutin.pester.data.NagType
import com.pavelgutin.pester.nag.EXTRA_SERVICE_NAG_ID
import com.pavelgutin.pester.nag.EXTRA_SERVICE_NAG_LABEL
import com.pavelgutin.pester.nag.NagForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NagTriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val nagId = intent.getLongExtra(EXTRA_NAG_ID, -1L)
        val label = intent.getStringExtra(EXTRA_NAG_LABEL) ?: "Pester"
        val typeName = intent.getStringExtra(EXTRA_NAG_TYPE)
        if (nagId < 0 || typeName == null) return

        val serviceIntent = Intent(context, NagForegroundService::class.java).apply {
            putExtra(EXTRA_SERVICE_NAG_ID, nagId)
            putExtra(EXTRA_SERVICE_NAG_LABEL, label)
        }
        ContextCompat.startForegroundService(context, serviceIntent)

        // Recurring alarms need their next occurrence scheduled; one-shot reminders are done after firing.
        if (typeName == NagType.ALARM.name) {
            val scheduler = AlarmScheduler(context)
            val repo = com.pavelgutin.pester.data.PesterDatabase.get(context).nagDao()
            CoroutineScope(Dispatchers.IO).launch {
                repo.getById(nagId)?.let { scheduler.schedule(it) }
            }
        }
    }
}
