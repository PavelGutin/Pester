package com.pavelgutin.pester.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pavelgutin.pester.data.NagEntity
import com.pavelgutin.pester.data.NagType
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime

private val weekOrder = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNagDialog(
    type: NagType,
    existing: NagEntity?,
    onDismiss: () -> Unit,
    onSave: (NagEntity) -> Unit
) {
    var label by remember { mutableStateOf(existing?.label ?: if (type == NagType.ALARM) "Wake up" else "Don't forget") }
    var days by remember { mutableStateOf(existing?.daysOfWeek ?: emptySet()) }

    val now = ZonedDateTime.now(ZoneId.systemDefault())
    val timeState = rememberTimePickerState(
        initialHour = existing?.hour ?: now.hour,
        initialMinute = existing?.minute ?: now.minute,
        is24Hour = false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New ${type.display()}" else "Edit ${type.display()}") },
        text = {
            Column {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
                TimePicker(state = timeState)

                if (type == NagType.ALARM) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        weekOrder.forEach { day ->
                            DayToggle(
                                label = day.name.take(1),
                                selected = day in days,
                                onClick = { days = if (day in days) days - day else days + day }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val oneShot = if (type == NagType.REMINDER) {
                    now.withHour(timeState.hour).withMinute(timeState.minute).withSecond(0).withNano(0)
                        .let { if (it.isBefore(now)) it.plusDays(1) else it }
                        .toInstant().toEpochMilli()
                } else null

                onSave(
                    (existing ?: NagEntity(label = label, type = type, hour = 0, minute = 0)).copy(
                        label = label,
                        hour = timeState.hour,
                        minute = timeState.minute,
                        daysOfWeek = days,
                        oneShotEpochMillis = oneShot,
                        enabled = true
                    )
                )
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun NagType.display() = if (this == NagType.ALARM) "alarm" else "reminder"

@Composable
private fun DayToggle(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    androidx.compose.foundation.layout.Box(
        contentAlignment = androidx.compose.ui.Alignment.Center,
        modifier = Modifier
            .size(34.dp)
            .background(background, androidx.compose.foundation.shape.CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant, androidx.compose.foundation.shape.CircleShape)
            .clickable(onClick = onClick)
    ) {
        Text(text = label, color = contentColor, style = MaterialTheme.typography.labelLarge)
    }
}
