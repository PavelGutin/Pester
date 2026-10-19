package com.pavelgutin.pester.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pavelgutin.pester.data.PesterDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val scheduler = AlarmScheduler(context)
        val dao = PesterDatabase.get(context).nagDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.getAllEnabled().forEach { scheduler.schedule(it) }
        }
    }
}
