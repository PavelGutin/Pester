package com.pavelgutin.pester.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.pavelgutin.pester.R
import com.pavelgutin.pester.data.NagEntity
import com.pavelgutin.pester.data.NagType

@Composable
fun NagListScreen(
    type: NagType,
    nags: List<NagEntity>,
    accent: Color,
    icon: Painter,
    emptyText: String,
    onToggle: (NagEntity, Boolean) -> Unit,
    onDelete: (NagEntity) -> Unit,
    onEdit: (NagEntity) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (nags.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(painter = icon, contentDescription = null, tint = accent, modifier = Modifier.size(48.dp))
                Text(
                    text = emptyText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(nags, key = { it.id }) { nag ->
                    NagRow(nag, accent, icon, onToggle, onDelete, onEdit)
                }
            }
        }
    }
}

@Composable
private fun NagRow(
    nag: NagEntity,
    accent: Color,
    icon: Painter,
    onToggle: (NagEntity, Boolean) -> Unit,
    onDelete: (NagEntity) -> Unit,
    onEdit: (NagEntity) -> Unit
) {
    Card(
        onClick = { onEdit(nag) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(painter = icon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "%02d:%02d".format(nag.hour, nag.minute),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = nag.label + daysSuffix(nag),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(checked = nag.enabled, onCheckedChange = { onToggle(nag, it) })

            IconButton(onClick = { onDelete(nag) }) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun daysSuffix(nag: NagEntity): String =
    if (nag.type == NagType.ALARM && nag.daysOfWeek.isNotEmpty()) {
        " • " + nag.daysOfWeek.sortedBy { it.value }.joinToString(" ") { it.name.take(3) }
    } else ""
