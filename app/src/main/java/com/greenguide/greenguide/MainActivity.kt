package com.greenguide.greenguide

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {

    private lateinit var classifier: PlantClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        classifier = PlantClassifier(this)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GreenGuideDashboard(classifier)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        classifier.close()
    }
}

@Composable
fun GreenGuideDashboard(classifier: PlantClassifier) {
    val context = LocalContext.current
    var leafImage by remember { mutableStateOf<Bitmap?>(null) }
    var fruitImage by remember { mutableStateOf<Bitmap?>(null) }
    var flowerImage by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf("Capture images for Fusion Analysis.") }
    var currentCaptureType by remember { mutableStateOf("leaf") }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) {
            when (currentCaptureType) {
                "leaf" -> leafImage = bitmap
                "fruit" -> fruitImage = bitmap
                "flower" -> flowerImage = bitmap
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("GreenGuide Fusion AI 🌿", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            ImageSlot("Leaf", leafImage) { currentCaptureType = "leaf"; cameraLauncher.launch() }
            ImageSlot("Fruit", fruitImage) { currentCaptureType = "fruit"; cameraLauncher.launch() }
            ImageSlot("Flower", flowerImage) { currentCaptureType = "flower"; cameraLauncher.launch() }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(resultText, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                if (leafImage != null && fruitImage != null && flowerImage != null) {
                    resultText = classifier.classifyWithFusion(leafImage!!, fruitImage!!, flowerImage!!)
                } else {
                    Toast.makeText(context, "Please capture all 3 images!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("RUN FUSION DIAGNOSIS 🚀")
        }
    }
}

@Composable
fun ImageSlot(label: String, bitmap: Bitmap?, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.size(90.dp).background(Color.LightGray, MaterialTheme.shapes.medium).clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                Image(bitmap.asImageBitmap(), contentDescription = label, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Text(label, style = MaterialTheme.typography.labelMedium)
            }
        }
        Text("Add Photo", fontSize = 10.sp, color = Color.Gray)
    }
}