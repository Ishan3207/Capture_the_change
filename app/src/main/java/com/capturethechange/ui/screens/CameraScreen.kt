package com.capturethechange.ui.screens

import android.Manifest
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.capturethechange.camera.CameraConfigurator
import com.capturethechange.capture.CaptureManager
import com.capturethechange.engine.MotionAnalyzer
import com.capturethechange.ui.components.ControlSlider
import com.capturethechange.ui.components.TopHUD
import com.capturethechange.ui.components.BottomHUD
import com.capturethechange.ui.components.CenterOverlay
import com.capturethechange.viewmodel.CameraViewModel
import com.capturethechange.viewmodel.CaptureState
import com.capturethechange.viewmodel.ResolutionLevel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(viewModel: CameraViewModel) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermissionState = rememberPermissionState(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    
    LaunchedEffect(Unit) {
        if (!storagePermissionState.status.isGranted) {
            storagePermissionState.launchPermissionRequest()
        }
    }

    if (cameraPermissionState.status.isGranted) {
        CameraPreviewContent(viewModel)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permission required.", color = Color.White)
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun CameraPreviewContent(viewModel: CameraViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    
    val livePreviewBitmap by viewModel.livePreviewBitmap.collectAsState()
    val isProMode by viewModel.isProMode.collectAsState()
    val captureState by viewModel.captureState.collectAsState()
    
    val threshold by viewModel.noiseThreshold.collectAsState()
    val timeFrame by viewModel.timeFrameMs.collectAsState()
    val outputMode by viewModel.outputMode.collectAsState()
    val resolution by viewModel.resolution.collectAsState()

    val executor = remember { Executors.newSingleThreadExecutor() }
    val imageCapture = remember { ImageCapture.Builder().build() }
    
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(captureState) {
        if (captureState is CaptureState.Saved) {
            Toast.makeText(context, "Saved!", Toast.LENGTH_SHORT).show()
            viewModel.captureState.value = CaptureState.Idle
        }
    }

    LaunchedEffect(resolution) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = androidx.camera.core.Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val targetRes = android.util.Size(resolution.width, resolution.height)
            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(ResolutionStrategy(targetRes, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
                .build()
                
            val imageAnalyzer = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
                .also {
                    it.setAnalyzer(executor, MotionAnalyzer(viewModel, viewModel.frameBuffer, viewModel.deltaEngine) { bmp ->
                        viewModel.livePreviewBitmap.value = bmp
                    })
                }

            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner, 
                    CameraSelector.DEFAULT_BACK_CAMERA, 
                    preview, 
                    imageCapture, 
                    imageAnalyzer
                )
                
                // Check hardware capabilities
                CameraConfigurator.checkCapabilities(camera, viewModel)
                
                coroutineScope.launch {
                    viewModel.isAELocked.collect { locked ->
                        CameraConfigurator.setAELock(camera, locked)
                    }
                }
                coroutineScope.launch {
                    viewModel.isAWBLocked.collect { locked ->
                        CameraConfigurator.setAWBLock(camera, locked)
                    }
                }
                coroutineScope.launch {
                    viewModel.isAFLocked.collect { locked ->
                        CameraConfigurator.setAFLock(camera, locked)
                    }
                }
                coroutineScope.launch {
                    viewModel.stabilizationMode.collect { mode ->
                        CameraConfigurator.setStabilizationMode(camera, mode)
                    }
                }

            } catch (e: Exception) {
                Log.e("CameraScreen", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        val isLandscape = maxWidth > maxHeight
        
        // 1. CameraX Preview
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { previewView }
        )

        // 2. Live Delta Preview Overlay
        livePreviewBitmap?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Live Preview",
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Fit
            )
        }

        // 3. UI Overlay
        CenterOverlay(viewModel = viewModel)

        val isProcessing = captureState is CaptureState.Processing
        val onShutterClick = {
            viewModel.captureState.value = CaptureState.Processing
            imageCapture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(image: ImageProxy) {
                        coroutineScope.launch {
                            val captureManager = CaptureManager(viewModel, viewModel.deltaEngine)
                            val uri = captureManager.processAndSaveCapture(
                                context,
                                viewModel.frameBuffer,
                                image
                            )
                            if (uri != null) {
                                viewModel.captureState.value = CaptureState.Saved(uri.toString())
                            } else {
                                viewModel.captureState.value = CaptureState.Idle
                            }
                        }
                    }
                    override fun onError(exception: ImageCaptureException) {
                        viewModel.captureState.value = CaptureState.Idle
                        Log.e("CameraScreen", "Capture failed", exception)
                    }
                }
            )
        }

        if (isLandscape) {
            // Landscape layout
            TopHUD(viewModel = viewModel, modifier = Modifier.align(Alignment.TopCenter))
            
            if (isProMode) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
                        .width(300.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    val threshold by viewModel.noiseThreshold.collectAsState()
                    val timeFrame by viewModel.timeFrameMs.collectAsState()

                    ControlSlider(
                        label = "Sensitivity",
                        value = threshold,
                        valueRange = 5f..255f,
                        onValueChange = { viewModel.noiseThreshold.value = it },
                        formatValue = { "${it.toInt()}" }
                    )
                    
                    ControlSlider(
                        label = "Timeframe",
                        value = timeFrame,
                        valueRange = 100f..5000f,
                        onValueChange = { viewModel.timeFrameMs.value = it },
                        formatValue = { "${it.toInt()} ms" }
                    )
                }
            }
            
            BottomHUD(
                viewModel = viewModel, 
                modifier = Modifier.align(Alignment.BottomCenter),
                isProcessing = isProcessing,
                onShutterClick = onShutterClick
            )
        } else {
            // Portrait layout
            TopHUD(viewModel = viewModel, modifier = Modifier.align(Alignment.TopCenter))
            
            if (isProMode) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 100.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    val threshold by viewModel.noiseThreshold.collectAsState()
                    val timeFrame by viewModel.timeFrameMs.collectAsState()

                    ControlSlider(
                        label = "Sensitivity",
                        value = threshold,
                        valueRange = 5f..255f,
                        onValueChange = { viewModel.noiseThreshold.value = it },
                        formatValue = { "${it.toInt()}" }
                    )
                    
                    ControlSlider(
                        label = "Timeframe",
                        value = timeFrame,
                        valueRange = 100f..5000f,
                        onValueChange = { viewModel.timeFrameMs.value = it },
                        formatValue = { "${it.toInt()} ms" }
                    )
                }
            }
            
            BottomHUD(
                viewModel = viewModel, 
                modifier = Modifier.align(Alignment.BottomCenter),
                isProcessing = isProcessing,
                onShutterClick = onShutterClick
            )
        }
    }
}
