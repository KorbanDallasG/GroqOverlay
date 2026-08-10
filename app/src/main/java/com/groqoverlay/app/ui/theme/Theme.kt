package com.groqoverlay.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val BlueLightColors = lightColorScheme(
    primary = Color(0xFF1565C0),
    secondary = Color(0xFF42A5F5),
    tertiary = Color(0xFF26C6DA),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

val BlueDarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    secondary = Color(0xFF1565C0),
    tertiary = Color(0xFF4DD0E1),
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color.White,
    onTertiary = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

val PurpleLightColors = lightColorScheme(
    primary = Color(0xFF7B1FA2),
    secondary = Color(0xFFBA68C8),
    tertiary = Color(0xFFEC407A),
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF)
)

val PurpleDarkColors = darkColorScheme(
    primary = Color(0xFFCE93D8),
    secondary = Color(0xFFAB47BC),
    tertiary = Color(0xFFF06292),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF24243E)
)

val PinkLightColors = lightColorScheme(
    primary = Color(0xFFD81B60),
    secondary = Color(0xFFFF5722),
    tertiary = Color(0xFFFFAB40)
)

val PinkDarkColors = darkColorScheme(
    primary = Color(0xFFF48FB1),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFFFFD54F),
    background = Color(0xFF1F1A1E),
    surface = Color(0xFF2A2026)
)

val GreenLightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF66BB6A),
    tertiary = Color(0xFF26A69A)
)

val GreenDarkColors = darkColorScheme(
    primary = Color(0xFF81C784),
    secondary = Color(0xFF388E3C),
    tertiary = Color(0xFF4DB6AC),
    background = Color(0xFF121C14),
    surface = Color(0xFF1E2922)
)

val OrangeLightColors = lightColorScheme(
    primary = Color(0xFFE65100),
    secondary = Color(0xFFFF8A65),
    tertiary = Color(0xFFFFB74D)
)

val OrangeDarkColors = darkColorScheme(
    primary = Color(0xFFFFAB91),
    secondary = Color(0xFFFF7043),
    tertiary = Color(0xFFFFCC80),
    background = Color(0xFF1A1310),
    surface = Color(0xFF2A1F18)
)

val TealLightColors = lightColorScheme(
    primary = Color(0xFF00695C),
    secondary = Color(0xFF26A69A),
    tertiary = Color(0xFF00ACC1)
)

val TealDarkColors = darkColorScheme(
    primary = Color(0xFF4DB6AC),
    secondary = Color(0xFF00897B),
    tertiary = Color(0xFF29B6F6),
    background = Color(0xFF0F181A),
    surface = Color(0xFF192427)
)

fun getAmoledDark(): Color = Color(0xFF000000)
fun getAmoledSurface(): Color = Color(0xFF0A0A0A)

@Composable
fun GroqOverlayTheme(
    themeMode: String = "auto",
    accentColor: String = "blue",
    useDynamic: Boolean = true,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        "light" -> false
        "dark", "amoled" -> true
        else -> isSystemDark
    }

    val colorScheme = when {
        useDynamic && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = androidx.compose.ui.platform.LocalContext.current
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
}
        accentColor == "purple" -> if (isDark) PurpleDarkColors else PurpleLightColors
        accentColor == "pink" -> if (isDark) PinkDarkColors else PinkLightColors
        accentColor == "green" -> if (isDark) GreenDarkColors else GreenLightColors
        accentColor == "orange" -> if (isDark) OrangeDarkColors else OrangeLightColors
        accentColor == "teal" -> if (isDark) TealDarkColors else TealLightColors
        else -> if (isDark) BlueDarkColors else BlueLightColors
    }

    val finalColorScheme = if (themeMode == "amoled") {
        colorScheme.copy(
            background = getAmoledDark(),
            surface = getAmoledSurface()
        )
    } else colorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
        val window = (view.context as Activity).window
            window.statusBarColor = finalColorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    MaterialTheme(
            colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}
