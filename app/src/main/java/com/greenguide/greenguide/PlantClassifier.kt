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
        val model: MappedByteBuffer =
            FileUtil.loadMappedFile(context, "plant_model.tflite")

        val options = Interpreter.Options()
        options.setNumThreads(4)

        interpreter = Interpreter(model, options)
    }

    // ✅ SINGLE IMAGE CLASSIFICATION
    fun classify(bitmap: Bitmap): String {

        val scores = getInferenceScores(bitmap)

        val maxIndex = scores.indices.maxByOrNull { scores[it] } ?: -1
        val confidence = if (maxIndex != -1) scores[maxIndex] else 0f

        return if (maxIndex != -1) {
            "Diagnosis: ${labels[maxIndex]}\n" +
                    "Confidence: ${(confidence * 100).toInt()}%"
        } else {
            "Analysis failed"
        }
    }

    // ✅ THREE IMAGE FUSION CLASSIFICATION
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
            fusedScores[i] =
                (leafResult[i] * 0.5f) +
                        (fruitResult[i] * 0.3f) +
                        (flowerResult[i] * 0.2f)
        }

        val maxIndex = fusedScores.indices.maxByOrNull { fusedScores[it] } ?: -1
        val confidence = if (maxIndex != -1) fusedScores[maxIndex] else 0f

        return if (maxIndex != -1) {
            "Diagnosis: ${labels[maxIndex]}\n" +
                    "Confidence: ${(confidence * 100).toInt()}%"
        } else {
            "Analysis failed"
        }
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
        val outputBuffer =
            TensorBuffer.createFixedSize(outputShape, DataType.FLOAT32)

        interpreter.run(tensorImage.buffer, outputBuffer.buffer)

        return outputBuffer.floatArray
    }

    fun close() {
        interpreter.close()
    }
}