package com.pavelgutin.pester.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.DayOfWeek

enum class NagType { ALARM, REMINDER }

class DayOfWeekSetConverter {
    @TypeConverter
    fun fromSet(days: Set<DayOfWeek>): String = days.joinToString(",") { it.value.toString() }

    @TypeConverter
    fun toSet(raw: String): Set<DayOfWeek> =
        if (raw.isBlank()) emptySet()
        else raw.split(",").map { DayOfWeek.of(it.trim().toInt()) }.toSet()
}

@Entity(tableName = "nags")
@TypeConverters(DayOfWeekSetConverter::class)
data class NagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val type: NagType,
    val hour: Int,
    val minute: Int,
    // Only meaningful for ALARM: days this recurs on. Empty for REMINDER.
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    // Only meaningful for REMINDER: the exact one-shot fire time. Null for ALARM.
    val oneShotEpochMillis: Long? = null,
    val enabled: Boolean = true
)
