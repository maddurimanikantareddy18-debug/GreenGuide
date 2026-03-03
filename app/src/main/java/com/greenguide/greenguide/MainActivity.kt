package com.example.greenguide

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GreenGuideDashboard()
                }
            }
        }
    }
}

@Composable
fun GreenGuideDashboard() {
    val context = LocalContext.current
    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("Ready to identify a plant?") }

    // 1. Create the Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedImage = bitmap
            resultText = "AI is analyzing... 🌿"
        }
    }

    // 2. Create a Permission Launcher to prevent the crash
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch() // Only launch if user said YES
        } else {
            Toast.makeText(context, "Camera permission is required to scan plants", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "GreenGuide AI Scanner 🌿", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(20.dp))

            capturedImage?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Plant Photo",
                    modifier = Modifier.size(250.dp).padding(10.dp)
                )
            }
            Text(text = resultText, style = MaterialTheme.typography.bodyLarge)
        }

        Button(
            onClick = {
                // Fix: Request permission instead of just launching
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).fillMaxWidth(0.8f)
        ) {
            Text("SCAN PLANT 📸")
        }
    }
}