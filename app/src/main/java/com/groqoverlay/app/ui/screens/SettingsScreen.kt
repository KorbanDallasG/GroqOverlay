package com.groqoverlay.app.ui.screens

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.groqoverlay.app.data.AppDatabase
import com.groqoverlay.app.data.AppPreferences
import com.groqoverlay.app.data.GroqClient
import com.groqoverlay.app.data.PrefsKeys
import com.groqoverlay.app.ui.components.ChipChoice
import com.groqoverlay.app.ui.components.GradientHeader
import com.groqoverlay.app.ui.components.SettingsCard
import com.groqoverlay.app.ui.components.SliderSetting
import kotlinx.coroutines.launch

private val ACCENT_COLORS = listOf(
    "blue" to Color(0xFF1565C0),
    "purple" to Color(0xFF7B1FA2),
    "pink" to Color(0xFFD81B60),
    "green" to Color(0xFF2E7D32),
    "orange" to Color(0xFFE65100),
    "teal" to Color(0xFF00695C)
)

private val THEME_MODES = listOf(
    "auto" to "Авто",
    "light" to "Светлая",
    "dark" to "Тёмная",
    "amoled" to "AMOLED"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: AppPreferences
) {
    var apiKeyVisible by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf("") }
    var testing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
        val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
        val msgs = AppDatabase.getDatabase(context).messageDao().getAllMessages()
                    val text = msgs.joinToString("\n\n") {
                        (if (it.role == "user") "Я: " else "AI: ") + it.content
                    }
                    context.contentResolver.openOutputStream(uri)?.use { it.write(text.toByteArray()) }
} catch (_: Exception) { }
}
}
    }

    Column(
            modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GradientHeader(
            title = "Настройки",
            subtitle = "Настройте приложение под себя",
            icon = Icons.Default.Settings,
            useGradient = prefs.useGradient.value
        )

        SettingsCard(
            title = "Groq API",
            description = "Получите ключ на console.groq.com",
            icon = Icons.Default.Key
        ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
            value = prefs.groqKey.value,
                    onValueChange = { prefs.saveGroqKey(it) },
                    label = { Text("API ключ") },
                    placeholder = { Text("gsk_...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (apiKeyVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { apiKeyVisible = !apiKeyVisible }) {
                            Icon(
                                if (apiKeyVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null
                            )
}
}
                )

                Button(
            onClick = {
                        testing = true
                        testResult = ""
                        scope.launch {
                            testResult = GroqClient.testKey(prefs.groqKey.value)
                            testing = false
                        }
},
                    enabled = !testing && prefs.groqKey.value.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (testing) "Проверка..." else "🔍 Проверить ключ")
}

                if (testResult.isNotEmpty()) {
                    Text(text = testResult, style = MaterialTheme.typography.bodyMedium)
}

                Text(text = "Модель", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(PrefsKeys.MODELS) { m ->
                        FilterChip(
            selected = prefs.groqModel.value == m,
                            onClick = { prefs.saveGroqModel(m) },
                            label = { Text(m, fontSize = 11.sp) }
                        )
}
}

                OutlinedTextField(
            value = prefs.systemPrompt.value,
                    onValueChange = { prefs.saveSystemPrompt(it) },
                    label = { Text("Системный промпт") },
                    placeholder = { Text("Ты - дружелюбный ассистент...") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedButton(
            onClick = { exportLauncher.launch("groq_chat.txt") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📤 Экспорт диалога в файл")
}
}
}

        SettingsCard(
            title = "Тема оформления",
            icon = Icons.Default.Palette
        ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Режим темы", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(THEME_MODES) { (mode, label) ->
                        ChipChoice(
            label = label,
                            selected = prefs.themeMode.value == mode,
                            onClick = {
                                prefs.saveThemeMode(mode)
}
                        )
}
}

                Text(text = "Акцентный цвет", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ACCENT_COLORS.forEach { (name, color) ->
                        val selected = prefs.accentColor.value == name
                        IconButton(
            onClick = {
                                prefs.saveAccentColor(name)
},
                    modifier = Modifier
                                .size(48.dp)
            .background(color, CircleShape)
                        ) {
        if (selected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
}
}
}
}

                Row(
            modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
        Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Material You цвета", style = MaterialTheme.typography.bodyLarge)
                        Text(
            text = "Использовать цвета обоев системы",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
}
                    Switch(
            checked = prefs.dynamicColors.value,
                        onCheckedChange = {
                            prefs.saveDynamicColors(it)
},
                        enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    )
}

                Row(
            modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
        Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Градиенты", style = MaterialTheme.typography.bodyLarge)
                        Text(
            text = "Градиентные заголовки",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
}
                    Switch(
            checked = prefs.useGradient.value,
                        onCheckedChange = { prefs.saveUseGradient(it) }
                    )
}

                Row(
            modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
        Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Анимации", style = MaterialTheme.typography.bodyLarge)
                        Text(
            text = "Анимации и эффекты",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
}
                    Switch(
            checked = prefs.animations.value,
                        onCheckedChange = { prefs.saveAnimations(it) }
                    )
}
}
}

        SettingsCard(
            title = "Плавающее окно",
            description = "Настройки AI overlay",
            icon = Icons.Default.Layers
        ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SliderSetting(
            label = "Прозрачность фона",
                    value = prefs.overlayOpacity.value.toFloat(),
                    onValueChange = { prefs.saveOverlayOpacity(it.toInt()) },
                    valueRange = 50f..100f,
                    suffix = "%"
                )
                SliderSetting(
            label = "Размер шрифта",
                    value = prefs.overlayFontSize.value.toFloat(),
                    onValueChange = { prefs.saveOverlayFontSize(it.toInt()) },
                    valueRange = 10f..24f,
                    suffix = "sp"
                )
                SliderSetting(
            label = "Скругление углов",
                    value = prefs.overlayCornerRadius.value.toFloat(),
                    onValueChange = { prefs.saveOverlayCornerRadius(it.toInt()) },
                    valueRange = 0f..40f,
                    suffix = "dp"
                )
                Row(
            modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
        Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Размытие фона", style = MaterialTheme.typography.bodyLarge)
                        Text(
            text = "Эффект размытия (Android 12+)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
}
                    Switch(
            checked = prefs.overlayBlurBg.value,
                        onCheckedChange = { prefs.saveOverlayBlurBg(it) }
                    )
}
}
}

        Spacer(Modifier.height(80.dp))
    }
}
