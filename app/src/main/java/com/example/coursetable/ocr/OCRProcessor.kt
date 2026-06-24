package com.example.coursetable.ocr

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await

/**
 * OCR processor using ML Kit text recognition.
 * Uses the Latin text recognizer (also handles most printed Chinese characters).
 */
class OCRProcessor {

    // 1. 创建识别器 —— 用拉丁文字识别器（也能处理中文）
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    data class OCRResult(
        val text: String,
        val blocks: List<TextBlock> = emptyList()
    )

    data class TextBlock(
        val text: String,
        val boundingBox: androidx.compose.ui.geometry.Rect? = null
    )

    /**
     * Process a bitmap through ML Kit text recognition.
     * 2. 处理图片的核心方法
     */

    suspend fun processBitmap(bitmap: Bitmap): OCRResult {
        val image = InputImage.fromBitmap(bitmap, 0)

        try {
            val visionText = recognizer.process(image).await()

            val text = visionText.text
            val blocks = visionText.textBlocks.map { block ->
                val box = block.boundingBox
                TextBlock(
                    text = block.text,
                    boundingBox = box?.let {
                        androidx.compose.ui.geometry.Rect(
                            left = it.left.toFloat(),
                            top = it.top.toFloat(),
                            right = it.right.toFloat(),
                            bottom = it.bottom.toFloat()
                        )
                    }
                )
            }

            return OCRResult(text = text, blocks = blocks)
        } catch (e: Exception) {
            throw OCRException("文字识别失败: ${e.message}", e)
        }
    }

    /**
     * Release recognizer resources.
     */
    fun close() {
        recognizer.close()
    }
}

class OCRException(message: String, cause: Throwable? = null) : Exception(message, cause)
