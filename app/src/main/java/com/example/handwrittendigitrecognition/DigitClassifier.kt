package com.example.handwrittendigitrecognition

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DigitClassifier(private val context: Context) {

    private var interpreter: Interpreter? = null

    fun initialize() {
        try {
            val modelBuffer = loadModelFile("mnist.tflite")
            interpreter = Interpreter(modelBuffer)
            Log.d("DigitClassifier", "Model loaded successfully")
        } catch (e: Exception) {
            Log.e("DigitClassifier", "Error initializing model", e)
            interpreter = null
        }
    }

    /**
     * Load model from assets into a direct ByteBuffer.
     * This works even if the asset is compressed.
     */
    private fun loadModelFile(modelPath: String): ByteBuffer {
        try {
            context.assets.open(modelPath).use { inputStream ->
                val bytes = inputStream.readBytes()
                val buffer = ByteBuffer.allocateDirect(bytes.size)
                buffer.order(ByteOrder.nativeOrder())
                buffer.put(bytes)
                buffer.rewind()
                return buffer
            }
        } catch (io: IOException) {
            throw RuntimeException("Error reading model file from assets: $modelPath", io)
        }
    }

    /**
     * Input: ByteBuffer of 1 x 28 x 28 floats
     * Output: Pair<digit, confidence>
     */
    fun classify(inputBuffer: ByteBuffer): Pair<Int, Float> {
        val interpreter = interpreter
        if (interpreter == null) {
            Log.e("DigitClassifier", "Interpreter is null, did initialize() fail?")
            return Pair(-1, 0f)
        }

        val output = Array(1) { FloatArray(10) } // digits 0–9

        return try {
            interpreter.run(inputBuffer, output)

            var maxIndex = 0
            var maxProb = 0f

            for (i in 0 until 10) {
                if (output[0][i] > maxProb) {
                    maxProb = output[0][i]
                    maxIndex = i
                }
            }
            Pair(maxIndex, maxProb)
        } catch (e: Exception) {
            Log.e("DigitClassifier", "Error during inference", e)
            Pair(-1, 0f)
        }
    }

    fun close() {
        try {
            interpreter?.close()
        } catch (_: Exception) {
        } finally {
            interpreter = null
        }
    }
}
