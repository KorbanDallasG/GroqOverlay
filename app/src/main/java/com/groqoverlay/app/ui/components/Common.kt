package com.groqoverlay.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun GradientHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    useGradient: Boolean,
    modifier: Modifier = Modifier
) {
    val materialColors = MaterialTheme.colorScheme
    val brush = if (useGradient) Brush.horizontalGradient(colors = listOf(materialColors.primary, materialColors.secondary, materialColors.tertiary)) else null

    Box(
            modifier = modifier.fillMaxWidth().then(
            if (brush != null) Modifier.background(brush, RoundedCornerShape(24.dp)).padding(24.dp)
            else Modifier.background(materialColors.surfaceVariant, RoundedCornerShape(24.dp)).padding(24.dp)
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(40.dp), tint = if (brush != null) Color.White else materialColors.primary)
            Spacer(Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleLarge, color = if (brush != null) Color.White else materialColors.onSurface)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = if (brush != null) Color.White.copy(alpha = 0.8f) else materialColors.onSurfaceVariant)
}
}
    }
}

@Composable
fun SettingsCard(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
        Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
        if (icon != null) { Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp)); Spacer(Modifier.width(12.dp)) }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMedium)
        if (description != null) Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}
}
            Spacer(Modifier.height(12.dp))
            content()
}
    }
}

@Composable
fun ChipChoice(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    FilterChip(selected = selected, onClick = onClick, label = { Text(label) }, modifier = modifier)
}

@Composable
fun SliderSetting(label: String, value: Float, onValueChange: (Float) -> Unit, valueRange: ClosedFloatingPointRange<Float>, suffix: String = "", modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Text(text = "${value.toInt()}$suffix", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
}
        Slider(value = value, onValueChange = onValueChange, valueRange = valueRange, modifier = Modifier.fillMaxWidth())
    }
}
