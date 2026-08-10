package com.groqoverlay.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import com.groqoverlay.app.data.AppPreferences
import com.groqoverlay.app.ui.screens.HomeScreen
import com.groqoverlay.app.ui.screens.SettingsScreen
import com.groqoverlay.app.ui.theme.GroqOverlayTheme

data class BottomNavItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

class MainActivity : ComponentActivity() {

    private lateinit var prefs: AppPreferences
    private val overlayGrantedState = mutableStateOf(false)
    private val notificationsGrantedState = mutableStateOf(true)

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
            if (!granted) {
            Toast.makeText(this, "Без разрешения на уведомления сервис может работать нестабильно", Toast.LENGTH_LONG).show()
}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = AppPreferences(this)
        requestNotificationPermissionIfNeeded()
        requestBatteryOptimization()

        setContent {
            GroqOverlayTheme(
            themeMode = prefs.themeMode.value,
                accentColor = prefs.accentColor.value,
                useDynamic = prefs.dynamicColors.value
            ) {
        var selectedTab by remember { mutableStateOf(0) }

                val items = listOf(
                    BottomNavItem("Главная", Icons.Filled.Home, Icons.Outlined.Home),
                    BottomNavItem("Настройки", Icons.Filled.Settings, Icons.Outlined.Settings)
                )

                Scaffold(
            bottomBar = {
                        NavigationBar {
                            items.forEachIndexed { index, item ->
                                NavigationBarItem(
            icon = {
                                        Icon(
                                            if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.title
                                        )
},
                                    label = { Text(item.title) },
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index }
                                )
}
}
}
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        AnimatedContent(
            targetState = selectedTab,
                            transitionSpec = {
                                (slideInHorizontally { it / 3 } + fadeIn()).togetherWith(
                                    (slideOutHorizontally { -it / 3 } + fadeOut()))
},
                            label = "tabAnimation"
                        ) { tab ->
                            when (tab) {
                                0 -> HomeScreen(
            prefs = prefs,
                                    overlayGranted = overlayGrantedState.value,
                    notificationsGranted = notificationsGrantedState.value,
                                    onStartService = { startAiService(null) },
                                    onStopService = { stopService(Intent(this@MainActivity, AiForegroundService::class.java)) },
                                    onOpenOverlay = { openOverlay() },
                                    onOpenSettings = { selectedTab = 1 }
                                )
                                1 -> SettingsScreen(
            prefs = prefs
                                )
}
}
}
}
}
}
    }

    override fun onResume() {
        super.onResume()
        overlayGrantedState.value = Settings.canDrawOverlays(this)
        notificationsGrantedState.value = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
}
}
    }

    private fun startAiService(action: String?) {
        val intent = Intent(this, AiForegroundService::class.java)
        if (action != null) intent.action = action
        ContextCompat.startForegroundService(this, intent)
    }

    private fun openOverlay() {
        if (!Settings.canDrawOverlays(this)) {
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            Toast.makeText(this, "Сначала разрешите отображение поверх других окон", Toast.LENGTH_LONG).show()
            return
        }
        startAiService(AiForegroundService.ACTION_OPEN_OVERLAY)
    }
    private fun requestBatteryOptimization() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return
        val pm = getSystemService(POWER_SERVICE) as android.os.PowerManager
        if (pm.isIgnoringBatteryOptimizations(packageName)) return
        try {
            startActivity(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:$packageName")
})
} catch (_: Exception) {
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
} catch (_: Exception) { }
}
    }
}
