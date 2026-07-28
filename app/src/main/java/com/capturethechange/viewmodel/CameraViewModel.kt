package com.capturethechange.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.capturethechange.engine.DeltaEngine
import com.capturethechange.engine.FrameBuffer
import kotlinx.coroutines.flow.MutableStateFlow

enum class OutputMode {
    COLOR_MOTION,
    GRAYSCALE_MOTION,
    COLOR_STATIC,
    GRAYSCALE_STATIC
}

enum class ResolutionLevel(val width: Int, val height: Int) {
    RES_480P(640, 480),
    RES_720P(1280, 720),
    RES_1080P(1920, 1080)
}

sealed class CaptureState {
    object Idle : CaptureState()
    object Processing : CaptureState()
    data class Saved(val uri: String) : CaptureState()
}

class CameraViewModel : ViewModel() {
    val isProMode = MutableStateFlow(false)
    val isAELocked = MutableStateFlow(false)
    val isAWBLocked = MutableStateFlow(false)
    
    val noiseThreshold = MutableStateFlow(15f) // 5 to 50
    val timeFrameMs = MutableStateFlow(500f) // 100 to 2000 ms
    
    val outputMode = MutableStateFlow(OutputMode.COLOR_MOTION)
    val resolution = MutableStateFlow(ResolutionLevel.RES_720P)
    
    val captureState = MutableStateFlow<CaptureState>(CaptureState.Idle)
    
    val livePreviewBitmap = MutableStateFlow<Bitmap?>(null)
    val analysisDimensions = MutableStateFlow<Pair<Int, Int>?>(null)

    val frameBuffer = FrameBuffer()
    val deltaEngine = DeltaEngine()
}
