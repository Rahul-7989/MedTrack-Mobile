package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val MedTrackColorScheme =
  lightColorScheme(
    primary = DustyTeal,
    secondary = WarmAmber,
    tertiary = BurntApricot,
    background = WarmIvory,
    surface = WarmCream,
    onPrimary = WarmIvory,
    onSecondary = MedTrackTextPrimary,
    onTertiary = WarmIvory,
    onBackground = MedTrackTextPrimary,
    onSurface = MedTrackTextPrimary,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = MedTrackColorScheme,
    typography = Typography,
    content = content
  )
}
