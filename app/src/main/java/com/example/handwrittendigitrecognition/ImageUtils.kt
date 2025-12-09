package com.example.handwrittendigitrecognition

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer
import java.nio.ByteOrder

object ImageUtils {

    const val IMAGE_WIDTH = 28
    const val IMAGE_HEIGHT = 28

    // Converts a bitmap from DrawingView into a ByteBuffer (1x28x28x1) for TFLite
    fun convertBitmapToByteBuffer(sourceBitmap: Bitmap): ByteBuffer {
        // Resize to 28x28
        val scaledBitmap = Bitmap.createScaledBitmap(
            sourceBitmap,
            IMAGE_WIDTH,
            IMAGE_HEIGHT,
            true
        )

        // Allocate buffer for 28*28 floats (4 bytes each)
        val byteBuffer =
            ByteBuffer.allocateDirect(4 * IMAGE_WIDTH * IMAGE_HEIGHT)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.rewind()

        // Loop through each pixel and convert to grayscale (0..1)
        for (y in 0 until IMAGE_HEIGHT) {
            for (x in 0 until IMAGE_WIDTH) {
                val pixel = scaledBitmap.getPixel(x, y)

                // MNIST is white digit on black background, we already draw like that
                val r = Color.red(pixel)
                val g = Color.green(pixel)
                val b = Color.blue(pixel)

                val gray = (r + g + b) / 3.0f      // 0..255
                val normalized = gray / 255.0f     // 0..1

                byteBuffer.putFloat(normalized)
            }
        }

        byteBuffer.rewind()
        return byteBuffer
    }
}
