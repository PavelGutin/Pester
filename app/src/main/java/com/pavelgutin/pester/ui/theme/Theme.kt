package com.pavelgutin.pester.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PesterColorScheme = darkColorScheme(
    primary = ZapGreen,
    onPrimary = DuskBackground,
    secondary = MosquitoPink,
    onSecondary = TextPrimary,
    tertiary = FireflyAmber,
    onTertiary = DuskBackground,
    background = DuskBackground,
    onBackground = TextPrimary,
    surface = DuskSurface,
    onSurface = TextPrimary,
    surfaceVariant = DuskSurfaceRaised,
    onSurfaceVariant = TextSecondary
)

@Composable
fun PesterTheme(
    // Pester is a dusk/bug-zapper theme by design — it does not follow system light/dark.
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PesterColorScheme,
        typography = PesterTypography,
        content = content
    )
}
