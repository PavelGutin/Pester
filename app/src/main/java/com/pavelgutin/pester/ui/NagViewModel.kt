package com.pavelgutin.pester.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pavelgutin.pester.alarm.AlarmScheduler
import com.pavelgutin.pester.data.NagEntity
import com.pavelgutin.pester.data.NagType
import com.pavelgutin.pester.data.PesterDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NagViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PesterDatabase.get(application).nagDao()
    private val scheduler = AlarmScheduler(application)

    val alarms: StateFlow<List<NagEntity>> = dao.observeByType(NagType.ALARM)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val reminders: StateFlow<List<NagEntity>> = dao.observeByType(NagType.REMINDER)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(nag: NagEntity) = viewModelScope.launch {
        val id = dao.upsert(nag)
        val saved = nag.copy(id = if (nag.id == 0L) id else nag.id)
        scheduler.schedule(saved)
    }

    fun delete(nag: NagEntity) = viewModelScope.launch {
        scheduler.cancel(nag)
        dao.delete(nag)
    }

    fun setEnabled(nag: NagEntity, enabled: Boolean) = viewModelScope.launch {
        val updated = nag.copy(enabled = enabled)
        dao.update(updated)
        if (enabled) scheduler.schedule(updated) else scheduler.cancel(updated)
    }

    fun canScheduleExactAlarms(): Boolean = scheduler.canScheduleExact()
}
