package com.pavelgutin.pester.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.pavelgutin.pester.R
import com.pavelgutin.pester.data.NagEntity
import com.pavelgutin.pester.data.NagType
import com.pavelgutin.pester.ui.theme.FireflyAmber
import com.pavelgutin.pester.ui.theme.MosquitoPink
import com.pavelgutin.pester.ui.theme.PesterTheme

class MainActivity : ComponentActivity() {

    private val viewModel: NagViewModel by viewModels()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            PesterTheme {
                PesterApp(
                    viewModel = viewModel,
                    needsExactAlarmPermission = { !viewModel.canScheduleExactAlarms() },
                    needsBatteryExemption = { !isIgnoringBatteryOptimizations() },
                    onRequestExactAlarm = { openExactAlarmSettings() },
                    onRequestBatteryExemption = { requestBatteryExemption() }
                )
            }
        }
    }

    private fun isIgnoringBatteryOptimizations(): Boolean {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:$packageName")))
        }
    }

    private fun requestBatteryExemption() {
        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName"))
        startActivity(intent)
    }
}

@Composable
private fun PesterApp(
    viewModel: NagViewModel,
    needsExactAlarmPermission: () -> Boolean,
    needsBatteryExemption: () -> Boolean,
    onRequestExactAlarm: () -> Unit,
    onRequestBatteryExemption: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var editing by remember { mutableStateOf<NagEntity?>(null) }
    var creatingType by remember { mutableStateOf<NagType?>(null) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    var intervalSeconds by remember { mutableIntStateOf(com.pavelgutin.pester.data.NagPrefs.getIntervalSeconds(context)) }

    val alarms by viewModel.alarms.collectAsState()
    val reminders by viewModel.reminders.collectAsState()

    val currentType = if (selectedTab == 0) NagType.ALARM else NagType.REMINDER
    val accent = if (currentType == NagType.ALARM) MosquitoPink else FireflyAmber
    val icon = painterResource(if (currentType == NagType.ALARM) R.drawable.ic_mosquito else R.drawable.ic_firefly)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { creatingType = currentType }, containerColor = accent) {
                Icon(painter = icon, contentDescription = "Add", tint = MaterialTheme.colorScheme.background)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Pester",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f).padding(start = 20.dp, top = 12.dp, bottom = 4.dp)
                )
                androidx.compose.material3.IconButton(onClick = { showIntervalDialog = true }) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Nag interval settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (needsExactAlarmPermission() || needsBatteryExemption()) {
                PermissionBanners(
                    showExactAlarm = needsExactAlarmPermission(),
                    showBattery = needsBatteryExemption(),
                    onRequestExactAlarm = onRequestExactAlarm,
                    onRequestBatteryExemption = onRequestBatteryExemption
                )
            }

            TabRow(selectedTabIndex = selectedTab, contentColor = accent) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResourceCompat(R.string.tab_alarms)) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResourceCompat(R.string.tab_reminders)) })
            }

            Box(modifier = Modifier.weight(1f)) {
                if (selectedTab == 0) {
                    NagListScreen(
                        type = NagType.ALARM,
                        nags = alarms,
                        accent = MosquitoPink,
                        icon = painterResource(R.drawable.ic_mosquito),
                        emptyText = stringResourceCompat(R.string.empty_alarms),
                        onToggle = viewModel::setEnabled,
                        onDelete = viewModel::delete,
                        onEdit = { editing = it }
                    )
                } else {
                    NagListScreen(
                        type = NagType.REMINDER,
                        nags = reminders,
                        accent = FireflyAmber,
                        icon = painterResource(R.drawable.ic_firefly),
                        emptyText = stringResourceCompat(R.string.empty_reminders),
                        onToggle = viewModel::setEnabled,
                        onDelete = viewModel::delete,
                        onEdit = { editing = it }
                    )
                }
            }
        }
    }

    if (creatingType != null) {
        EditNagDialog(
            type = creatingType!!,
            existing = null,
            onDismiss = { creatingType = null },
            onSave = { viewModel.save(it) }
        )
    }

    editing?.let { nag ->
        EditNagDialog(
            type = nag.type,
            existing = nag,
            onDismiss = { editing = null },
            onSave = { viewModel.save(it) }
        )
    }

    if (showIntervalDialog) {
        NagIntervalDialog(
            initialSeconds = intervalSeconds,
            onDismiss = { showIntervalDialog = false },
            onSave = { seconds ->
                com.pavelgutin.pester.data.NagPrefs.setIntervalSeconds(context, seconds)
                intervalSeconds = seconds
            }
        )
    }
}

@Composable
private fun PermissionBanners(
    showExactAlarm: Boolean,
    showBattery: Boolean,
    onRequestExactAlarm: () -> Unit,
    onRequestBatteryExemption: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (showExactAlarm) {
            Banner(
                text = "Pester needs permission to schedule exact alarms, or your nags may fire late.",
                actionText = "Fix it",
                onAction = onRequestExactAlarm
            )
        }
        if (showBattery) {
            Banner(
                text = "Exempt Pester from battery optimization so nags don't get killed in the background.",
                actionText = "Fix it",
                onAction = onRequestBatteryExemption
            )
        }
    }
}

@Composable
private fun Banner(text: String, actionText: String, onAction: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onAction, modifier = Modifier.padding(top = 8.dp)) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun stringResourceCompat(id: Int): String = androidx.compose.ui.res.stringResource(id)
