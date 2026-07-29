package com.capturethechange.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.capturethechange.viewmodel.CameraViewModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val ObsidianBlack = Color(0xFF131313)
val VividYellow = Color(0xFFFFD700)
val SignalGreen = Color(0xFF00FF00)
val GlassBackground = Color(0x66121212)

@Composable
fun TopHUD(viewModel: CameraViewModel, modifier: Modifier = Modifier) {
    val res by viewModel.resolution.collectAsState()
    val isAELocked by viewModel.isAELocked.collectAsState()
    val isAWBLocked by viewModel.isAWBLocked.collectAsState()
    val isAFLocked by viewModel.isAFLocked.collectAsState()
    val stabMode by viewModel.stabilizationMode.collectAsState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassBackground)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Box(modifier = Modifier.clickable {
                val modes = com.capturethechange.viewmodel.ResolutionLevel.values()
                val nextIdx = (res.ordinal + 1) % modes.size
                viewModel.resolution.value = modes[nextIdx]
            }) {
                HudText("RES", res.name.replace("RES_", ""))
            }
            
            Box(modifier = Modifier.clickable {
                val modes = com.capturethechange.viewmodel.StabilizationMode.values()
                val nextIdx = (stabMode.ordinal + 1) % modes.size
                viewModel.stabilizationMode.value = modes[nextIdx]
            }) {
                HudText("STAB", stabMode.name.take(3))
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("AE", color = if (isAELocked) VividYellow else Color.White, fontSize = 12.sp, modifier = Modifier.clickable { viewModel.isAELocked.value = !isAELocked })
            Text("AWB", color = if (isAWBLocked) VividYellow else Color.White, fontSize = 12.sp, modifier = Modifier.clickable { viewModel.isAWBLocked.value = !isAWBLocked })
            Text("AF", color = if (isAFLocked) VividYellow else Color.White, fontSize = 12.sp, modifier = Modifier.clickable { viewModel.isAFLocked.value = !isAFLocked })
        }
    }
}

@Composable
fun HudText(label: String, value: String) {
    Column(horizontalAlignment = Alignment.Start) {
        Text(text = label, color = Color.White, fontSize = 10.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BottomHUD(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier,
    isProcessing: Boolean,
    onShutterClick: () -> Unit
) {
    val outputMode by viewModel.outputMode.collectAsState()
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassBackground)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Output Mode Toggle (Left)
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clip(RoundedCornerShape(4.dp))
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                .clickable {
                    val modes = com.capturethechange.viewmodel.OutputMode.values()
                    val nextIdx = (outputMode.ordinal + 1) % modes.size
                    viewModel.outputMode.value = modes[nextIdx]
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(outputMode.name.replace("_", " "), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
        
        // Record / Shutter Button (Center)
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(64.dp) // Slightly bigger since it's centered
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.White, CircleShape)
                .clickable(enabled = !isProcessing, onClick = onShutterClick),
            contentAlignment = Alignment.Center
        ) {
            if (isProcessing) {
                androidx.compose.material3.CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
            } else {
                Box(modifier = Modifier.size(54.dp).clip(CircleShape).background(Color.White)) // White button
            }
        }
        
        // Auto/Pro Toggles (Right)
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically, 
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isPro by viewModel.isProMode.collectAsState()
            val isAuto by viewModel.isAutoMode.collectAsState()
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isPro) Color.White else Color.Transparent)
                    .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    .clickable { 
                        viewModel.isProMode.value = true
                        viewModel.isAutoMode.value = false
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("PRO", color = if (isPro) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isAuto) Color.White else Color.Transparent)
                    .border(1.dp, Color.White, RoundedCornerShape(4.dp))
                    .clickable { 
                        viewModel.isAutoMode.value = true
                        viewModel.isProMode.value = false
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("AUTO", color = if (isAuto) Color.Black else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@Composable
fun CenterOverlay(viewModel: CameraViewModel, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // Crosshair
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val crossSize = 20.dp.toPx()
            
            drawLine(Color.White.copy(alpha = 0.5f), Offset(cx - crossSize, cy), Offset(cx + crossSize, cy), 1.dp.toPx())
            drawLine(Color.White.copy(alpha = 0.5f), Offset(cx, cy - crossSize), Offset(cx, cy + crossSize), 1.dp.toPx())
        }
    }
}
