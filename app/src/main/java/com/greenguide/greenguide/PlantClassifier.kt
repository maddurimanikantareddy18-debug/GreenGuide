package com.greenguide.greenguide

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer

class PlantClassifier(context: Context) {

    private val interpreter: Interpreter
    private val labels = listOf("Healthy", "Early Blight", "Late Blight", "Leaf Spot")

    init {
        // Ensure plant_model.tflite is in your assets folder
        val model: MappedByteBuffer = FileUtil.loadMappedFile(context, "plant_model.tflite")
        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }
        interpreter = Interpreter(model, options)
    }

    // ✅ MULTI-IMAGE FUSION LOGIC (Main function used by MainActivity)
    fun classifyWithFusion(
        leafBitmap: Bitmap,
        fruitBitmap: Bitmap,
        flowerBitmap: Bitmap
    ): String {
        val leafResult = getInferenceScores(leafBitmap)
        val fruitResult = getInferenceScores(fruitBitmap)
        val flowerResult = getInferenceScores(flowerBitmap)

        val fusedScores = FloatArray(labels.size)
        for (i in labels.indices) {
            // Weighted Logic: Leaf (50%), Fruit (30%), Flower (20%)
            fusedScores[i] = (leafResult[i] * 0.5f) + (fruitResult[i] * 0.3f) + (flowerResult[i] * 0.2f)
        }

        val maxIndex = fusedScores.indices.maxByOrNull { fusedScores[it] } ?: -1
        val confidence = if (maxIndex != -1) fusedScores[maxIndex] else 0f

        return formatResult(maxIndex, confidence)
    }

    // ✅ BACKUP: SINGLE IMAGE CLASSIFICATION
    @Suppress("unused")
    fun classify(bitmap: Bitmap): String {
        val scores = getInferenceScores(bitmap)
        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1
        val confidence = if (maxIndex != -1) scores[maxIndex] else 0f
        return formatResult(maxIndex, confidence)
    }

    private fun getInferenceScores(bitmap: Bitmap): FloatArray {
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        val outputShape = interpreter.getOutputTensor(0).shape()
        val outputBuffer = TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)

        interpreter.run(tensorImage.buffer, outputBuffer.buffer)
        return outputBuffer.floatArray
    }

    private fun formatResult(index: Int, confidence: Float): String {
        if (index == -1 || confidence < 0.15f) return "Diagnosis: Uncertain\nAction: Please retake photos."

        val diagnosis = labels[index]

        // Severity Logic
        val severity = when {
            diagnosis == "Healthy" -> "None"
            confidence > 0.80f -> "High - Urgent Treatment Needed"
            confidence > 0.45f -> "Medium - Monitor closely"
            else -> "Low - Early Detection"
        }

        // AI Insight/Explanation
        val explanation = when (diagnosis) {
            "Healthy" -> "No visual symptoms of disease detected."
            "Early Blight" -> "Detected concentric ring patterns typical of Alternaria fungi."
            "Late Blight" -> "Detected water-soaked lesions often caused by high humidity."
            "Leaf Spot" -> "Detected small dark spots with yellow halos."
            else -> "Pattern unrecognized."
        }

        return """
            Result: $diagnosis
            Confidence: ${(confidence * 100).toInt()}%
            Severity: $severity
            
            Insight: $explanation
        """.trimIndent()
    }

    fun close() {
        interpreter.close()
    }
}