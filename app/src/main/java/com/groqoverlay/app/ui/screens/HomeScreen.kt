package com.groqoverlay.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.outlined.BubbleChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groqoverlay.app.ui.components.GradientHeader
import com.groqoverlay.app.ui.components.SettingsCard
import com.groqoverlay.app.data.AppPreferences

@Composable
fun HomeScreen(
    prefs: AppPreferences,
    overlayGranted: Boolean,
    notificationsGranted: Boolean,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    onOpenOverlay: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val hasKey = prefs.groqKey.value.isNotBlank()
    val pm = context.getSystemService(android.content.Context.POWER_SERVICE) as android.os.PowerManager
    val batteryIgnored = android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M || pm.isIgnoringBatteryOptimizations(context.packageName)
        val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
        val iconRotation by infiniteTransition.animateFloat(
            initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotate"
    )

    Column(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradientHeader(
            title = "Groq Overlay",
            subtitle = "AI всегда под рукой",
            icon = Icons.Outlined.BubbleChart,
            useGradient = prefs.useGradient.value
        )

        // Hero card с анимированным фоном
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
        Box(
            modifier = Modifier
                    .fillMaxWidth()
            .padding(24.dp)
            ) {
        if (prefs.useGradient.value && prefs.animations.value) {
        Box(
            modifier = Modifier
                            .size(100.dp)
            .align(Alignment.TopEnd)
            .rotate(iconRotation)
            .background(
                                Brush.radialGradient(
            colors = listOf(
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = glowAlpha),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            )
                    )
}

                Column {
                    Text(
            text = if (hasKey) "Готово к работе" else "Нужна настройка",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
            text = if (hasKey) {
                            "Ключ Groq установлен. Можно использовать AI в любом приложении."
                        } else {
                            "Сначала введите Groq API ключ в настройках."
                        },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
        if (!hasKey) {
                        Spacer(Modifier.height(16.dp))
                        Button(
            onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Перейти в настройки")
}
}
}
}
}

        // Статус разрешений
        SettingsCard(
            title = "Состояние",
            icon = Icons.Default.CheckCircle
        ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PermissionStatusRow(
            label = "Ключ Groq",
                    granted = hasKey,
                    onFix = onOpenSettings
                )

                PermissionStatusRow(
            label = "Поверх других окон",
                    granted = overlayGranted,
                    onFix = {
        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
}
                )

                PermissionStatusRow(
                label = "Уведомления",
                granted = notificationsGranted,
                onFix = {
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        android.net.Uri.parse("package:" + context.packageName)
                    )
                    context.startActivity(intent)
                }
            )
            PermissionStatusRow(
            label = "Оптимизация батареи",
                    granted = batteryIgnored,
                    onFix = {
                        try {
                            context.startActivity(
                                Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                                    data = android.net.Uri.parse("package:" + context.packageName)
}
                            )
} catch (_: Exception) {
                            try {
                                context.startActivity(Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
} catch (_: Exception) { }
}
}
                )
}
}

        // Кнопки действий
        SettingsCard(
            title = "Управление",
            icon = Icons.Default.PlayArrow
        ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
            onClick = onStartService,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Запустить сервис")
}

                Button(
            onClick = onOpenOverlay,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasKey && overlayGranted,
                    colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Открыть AI окно")
}

                OutlinedButton(
            onClick = onStopService,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Остановить сервис")
}
}
}

        // Подсказка
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
            )
        ) {
        Row(
            modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Bolt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(Modifier.width(12.dp))
                Text(
            text = "Перетаскивайте окно за заголовок и меняйте размер за кружок в правом нижнем углу.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
}
}

        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun PermissionStatusRow(
    label: String,
    granted: Boolean,
    onFix: () -> Unit
) {
    Row(
            modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
        ) {
            Icon(
            imageVector = if (granted) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (granted) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
            text = label,
                    style = MaterialTheme.typography.bodyMedium
            )
}

        if (!granted) {
            TextButton(onClick = onFix) {
                Text("Исправить", fontSize = 12.sp)
}
} else {
            Text(
            text = "✓",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
            )
}
    }
}
