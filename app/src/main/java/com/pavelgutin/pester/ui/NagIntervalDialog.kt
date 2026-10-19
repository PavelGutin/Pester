package com.pavelgutin.pester.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pavelgutin.pester.data.NagPrefs

@Composable
fun NagIntervalDialog(
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    var seconds by remember { mutableFloatStateOf(initialSeconds.toFloat()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nag interval") },
        text = {
            Column {
                Text(
                    text = "Every ${seconds.toInt()} seconds",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = seconds,
                    onValueChange = { seconds = it },
                    valueRange = NagPrefs.MIN_INTERVAL_SECONDS.toFloat()..NagPrefs.MAX_INTERVAL_SECONDS.toFloat(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Text(
                    text = "Shorter is more insistent but drains battery and your watch's " +
                        "Bluetooth connection faster. Longer is gentler. Applies to every " +
                        "alarm and reminder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(seconds.toInt())
                onDismiss()
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
