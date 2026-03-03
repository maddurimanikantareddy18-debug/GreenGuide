package com.greenguide.greenguide

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.MappedByteBuffer

class PlantClassifier(context: Context) {
    private val interpreter: Interpreter
    private val labels = listOf("Healthy", "Early Blight", "Late Blight", "Leaf Spot")

    init {
        // Load TFLite model from assets
        val model: MappedByteBuffer = FileUtil.loadMappedFile(context, "plant_model.tflite")
        interpreter = Interpreter(model)
    }

    fun classify(bitmap: Bitmap): String {
        // Resize image to 224x224 using BILINEAR interpolation
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.BILINEAR))
            .build()

        // Load bitmap into TensorImage
        var tensorImage = TensorImage(DataType.FLOAT32)
        tensorImage.load(bitmap)
        tensorImage = imageProcessor.process(tensorImage)

        // Prepare output buffer
        val outputBuffer = TensorBuffer.createFixedSize(intArrayOf(1, labels.size), DataType.FLOAT32)

        // Run the model
        interpreter.run(tensorImage.buffer, outputBuffer.buffer)

        // Get results
        val results = outputBuffer.floatArray
        val maxIndex = results.indices.maxByOrNull { index -> results[index] } ?: -1

        return if (maxIndex != -1) {
            "Prediction: ${labels[maxIndex]} (${(results[maxIndex] * 100).toInt()}% match)"
        } else {
            "Analysis failed"
        }
    }
}