package com.example.handwrittendigitrecognition

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var drawingView: DrawingView
    private lateinit var tvResult: TextView
    private lateinit var btnClear: Button
    private lateinit var btnPredict: Button

    private var digitClassifier: DigitClassifier? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find views
        drawingView = findViewById(R.id.drawingView)
        tvResult = findViewById(R.id.tvResult)
        btnClear = findViewById(R.id.btnClear)
        btnPredict = findViewById(R.id.btnPredict)

        // Initialize classifier with safety
        try {
            val classifier = DigitClassifier(this)
            classifier.initialize()
            digitClassifier = classifier
        } catch (e: Exception) {
            e.printStackTrace()
            tvResult.text = "Error loading model."
        }

        btnClear.setOnClickListener {
            drawingView.clearCanvas()
            tvResult.text = "Canvas cleared. Draw a digit."
        }

        btnPredict.setOnClickListener {
            val bitmap = drawingView.getBitmap()
            val classifier = digitClassifier

            if (bitmap == null) {
                tvResult.text = "Please draw a digit first!"
                return@setOnClickListener
            }

            if (classifier == null) {
                tvResult.text = "Model not ready."
                return@setOnClickListener
            }

            try {
                val inputBuffer = ImageUtils.convertBitmapToByteBuffer(bitmap)
                val (digit, confidence) = classifier.classify(inputBuffer)

                if (digit == -1) {
                    tvResult.text = "Prediction failed."
                } else {
                    val percent = (confidence * 100).toInt()
                    tvResult.text = "Predicted: $digit   (Confidence: $percent%)"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tvResult.text = "Error during prediction."
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        digitClassifier?.close()
        digitClassifier = null
    }
}
