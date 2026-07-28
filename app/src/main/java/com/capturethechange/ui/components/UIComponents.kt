package com.capturethechange.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.capturethechange.viewmodel.OutputMode

@Composable
fun ShutterButton(
    isProcessing: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color.White)
            .border(4.dp, Color.Gray, CircleShape)
            .clickable(enabled = !isProcessing, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isProcessing) {
            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, CircleShape)
            )
        }
    }
}

@Composable
fun ControlSlider(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    formatValue: (Float) -> String
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, color = Color.White, style = MaterialTheme.typography.bodySmall)
            Text(text = formatValue(value), color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
fun ModeToggle(
    currentMode: OutputMode,
    onModeSelected: (OutputMode) -> Unit
) {
    val modes = OutputMode.entries
    
    // Simple segmented button for modes (simplified for basic UI)
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text("Mode", color = Color.White, modifier = Modifier.padding(horizontal = 16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { mode ->
                val isSelected = currentMode == mode
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.DarkGray)
                        .clickable { onModeSelected(mode) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.name.replace("_", " "),
                        color = if (isSelected) Color.Black else Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
