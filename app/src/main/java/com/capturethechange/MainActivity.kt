package com.capturethechange

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.capturethechange.ui.screens.CameraScreen
import com.capturethechange.ui.theme.CaptureTheChangeTheme
import com.capturethechange.viewmodel.CameraViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CaptureTheChangeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen(viewModel = viewModel)
                }
            }
        }
    }
}
