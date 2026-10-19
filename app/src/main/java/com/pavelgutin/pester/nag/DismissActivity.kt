package com.pavelgutin.pester.nag

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pavelgutin.pester.ui.theme.FireflyAmber
import com.pavelgutin.pester.ui.theme.MosquitoPink
import com.pavelgutin.pester.ui.theme.PesterTheme
import kotlinx.coroutines.delay

class DismissActivity : ComponentActivity() {

    private var service: NagForegroundService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as NagForegroundService.LocalBinder).service()
            bound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
            service = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindService(NagForegroundService.bindingIntent(this), connection, Context.BIND_AUTO_CREATE)

        setContent {
            PesterTheme {
                DismissScreen(
                    label = { service?.currentLabel() ?: "Pester" },
                    onSwat = { service?.stopNagging() },
                    onDone = { finish() }
                )
            }
        }
    }

    override fun onDestroy() {
        if (bound) unbindService(connection)
        super.onDestroy()
    }
}

@Composable
private fun DismissScreen(label: () -> String, onSwat: () -> Unit, onDone: () -> Unit) {
    var squashed by remember { mutableStateOf(false) }
    var showBurst by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (squashed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
        label = "swatScale"
    )

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Bolt,
                contentDescription = null,
                tint = FireflyAmber,
                modifier = Modifier.size(56.dp)
            )
            Text(
                text = label(),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
            Text(
                text = "It's still buzzing. Make it stop.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            SwatButton(
                scale = scale,
                showBurst = showBurst,
                onClick = {
                    squashed = true
                    showBurst = true
                    onSwat()
                }
            )
        }
    }

    if (showBurst) {
        LaunchedBurstTimeout(onTimeout = onDone)
    }
}

@Composable
private fun SwatButton(scale: Float, showBurst: Boolean, onClick: () -> Unit) {
    Box(contentAlignment = Alignment.Center) {
        Button(
            onClick = onClick,
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = MosquitoPink,
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier
                .size(180.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale)
        ) {
            Text(
                text = "SWAT IT",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black
            )
        }

        AnimatedVisibility(
            visible = showBurst,
            enter = scaleIn(initialScale = 0.4f) + fadeIn(),
            exit = scaleOut(targetScale = 1.6f) + fadeOut()
        ) {
            Text(
                text = "BZZZT!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LaunchedBurstTimeout(onTimeout: () -> Unit) {
    androidx.compose.runtime.LaunchedEffect(Unit) {
        delay(600)
        onTimeout()
    }
}
