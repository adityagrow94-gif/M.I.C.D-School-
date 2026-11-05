package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = MicdPrimaryDarkTheme,
  secondary = MicdSecondary,
  tertiary = MicdTertiary,
  background = MicdBackgroundDark,
  surface = MicdSurfaceDark,
  surfaceVariant = MicdSurfaceVariantDark,
  onPrimary = Color(0xFF002244),
  onBackground = Color(0xFFE1E7F0),
  onSurface = Color(0xFFE1E7F0)
)

private val LightColorScheme = lightColorScheme(
  primary = MicdPrimary,
  secondary = MicdSecondary,
  tertiary = MicdTertiary,
  background = MicdBackground,
  surface = MicdSurface,
  surfaceVariant = MicdSurfaceVariant,
  onPrimary = Color.White,
  onSecondary = Color.White,
  onTertiary = Color.White,
  onBackground = MicdTextPrimary,
  onSurface = MicdTextPrimary
)

@Composable
fun MicdSchoolTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Keep consistent school branding
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
