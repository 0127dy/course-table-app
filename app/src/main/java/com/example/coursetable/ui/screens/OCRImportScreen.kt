package com.example.coursetable.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.view.Surface
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.coursetable.data.model.Course
import com.example.coursetable.ocr.OCRProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.compose.material.icons.filled.Check

/**
 * Screen for OCR import: capture or pick image, recognize text, parse courses.
 * Compose 页面负责整个用户交互流程
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OCRImportScreen(
    onCoursesParsed: (List<Course>) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("") }
    var parsedCourses by remember { mutableStateOf<List<Course>>(emptyList()) }
    var showCamera by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                capturedBitmap = bitmap
                showCamera = false
            } catch (e: Exception) {
                errorMessage = "无法加载图片"
            }
        }
    }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("OCR导入", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Camera preview or captured image
            if (showCamera && capturedBitmap == null) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val preview = Preview.Builder().build().also {
                                it.surfaceProvider = previewView.surfaceProvider
                            }
                            imageCapture = ImageCapture.Builder()
                                .setTargetRotation(previewView.display?.rotation ?: Surface.ROTATION_0)
                                .build()
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            val cameraProvider = cameraProviderFuture.get()
                            cameraProvider.unbindAll()
                            //CameraX 自动感知 Activity 生命周期，页面退到后台自动释放相机资源，不需要手动 `release()`
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner, cameraSelector, preview, imageCapture
                            )
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledIconButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.size(48.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoLibrary,
                                    contentDescription = "从相册选择",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }

                            FilledIconButton(
                                onClick = {
                                    val capture = imageCapture ?: return@FilledIconButton
                                    val photoFile = File.createTempFile("ocr_", ".jpg", context.cacheDir)
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                    capture.takePicture(
                                        outputOptions, ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                                                if (bitmap != null) {
                                                    // CameraX 拍照回调中：
                                                    val matrix = Matrix()
                                                    matrix.postRotate(90f)// 后置摄像头默认旋转90度
                                                    val rotated = Bitmap.createBitmap(
                                                        bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                                                    )
                                                    capturedBitmap = rotated
                                                    showCamera = false
                                                }
                                            }
                                            override fun onError(exception: ImageCaptureException) {
                                                errorMessage = "拍照失败: ${exception.message}"
                                            }
                                        }
                                    )
                                },
                                modifier = Modifier.size(72.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color.White)
                            ) {
                                Box(
                                    modifier = Modifier.size(56.dp).clip(CircleShape)
                                        .background(Color.White)
                                        .border(3.dp, Color(0xFF6750A4), CircleShape)
                                )
                            }
                            Box(modifier = Modifier.size(48.dp))
                        }
                    }
                }
            } else if (capturedBitmap != null) {
                // Show captured/preview image
                Column(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    Image(
                        bitmap = capturedBitmap!!.asImageBitmap(),
                        contentDescription = "拍摄的图片",
                        modifier = Modifier.fillMaxWidth().weight(1f).padding(8.dp),
                        contentScale = ContentScale.Fit
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedBitmap = null; showCamera = true
                                recognizedText = ""; parsedCourses = emptyList(); errorMessage = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Filled.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("重新拍摄")
                        }

                        Button(
                            onClick = {
                                isProcessing = true
                                errorMessage = null
                                // 在 OCRImportScreen 中直接启动协程
                                kotlinx.coroutines.MainScope().launch {
                                    try {
                                        val processor = OCRProcessor()
                                        // withContext(Dispatchers.IO) → OCR 在 IO 线程执行
                                        val result = withContext(Dispatchers.IO) {
                                            processor.processBitmap(capturedBitmap!!)
                                        }
                                        processor.close()
                                        recognizedText = result.text
                                        parsedCourses = parseCourseText(result.text)
                                    } catch (e: Exception) {
                                        errorMessage = "识别失败: ${e.message}"
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp
                                )
                            } else {
                                Icon(imageVector = Icons.Filled.DocumentScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isProcessing) "识别中..." else "识别文字")
                        }
                    }
                }
            } else {
                // No camera, show gallery pick option
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.PhotoLibrary, contentDescription = null,
                            modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { galleryLauncher.launch("image/*") }, modifier = Modifier.padding(horizontal = 32.dp)) {
                            Text("从相册选择图片")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "选择包含课程表的截图或照片",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Results section
            if (recognizedText.isNotBlank() || parsedCourses.isNotEmpty() || errorMessage != null) {
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(300.dp).padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (errorMessage != null) {
                        item {
                            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (recognizedText.isNotBlank()) {
                        item {
                            Text("识别到的文字:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = recognizedText, modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (parsedCourses.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "解析到${parsedCourses.size}门课程",
                                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold
                            )
                        }

                        items(parsedCourses) { course ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(course.color).copy(alpha = 0.15f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(course.color)))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(course.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "${Course.DAY_LABELS.getOrElse(course.dayOfWeek - 1) { "?" }} ${course.startTime}-${course.endTime}",
                                            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { onCoursesParsed(parsedCourses) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("导入 ${parsedCourses.size} 门课程")
                            }
                        }
                    } else if (recognizedText.isNotBlank() && !isProcessing && errorMessage == null) {
                        item {
                            Text(
                                text = "未能自动解析课程信息，可手动添加课程",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

/**
 * Smart parsing of recognized text into course objects.
 * 自动提取出结构化的课程信息
 */
private fun parseCourseText(text: String): List<Course> {
    val lines = text.trim().lines().map { it.trim() }.filter { it.isNotBlank() }
    if (lines.isEmpty()) return emptyList()

    val courses = mutableListOf<Course>()

    val dayKeywords = mapOf(
        "周一" to 1, "星期二" to 2, "星期三" to 3, "星期四" to 4, "星期五" to 5, "星期六" to 6, "星期日" to 7,
        "周二" to 2, "周三" to 3, "周四" to 4, "周五" to 5, "周六" to 6, "周日" to 7,
        "星期" to 0, "一" to 1, "二" to 2, "三" to 3, "四" to 4, "五" to 5, "六" to 6, "日" to 7,
        "Mon" to 1, "MON" to 1, "monday" to 1, "Monday" to 1,
        "Tue" to 2, "TUE" to 2, "tuesday" to 2, "Tuesday" to 2,
        "Wed" to 3, "WED" to 3, "wednesday" to 3, "Wednesday" to 3,
        "Thu" to 4, "THU" to 4, "thursday" to 4, "Thursday" to 4,
        "Fri" to 5, "FRI" to 5, "friday" to 5, "Friday" to 5,
        "Sat" to 6, "SAT" to 6, "saturday" to 6, "Saturday" to 6,
        "Sun" to 7, "SUN" to 7, "sunday" to 7, "Sunday" to 7,
    )

    val colors = listOf(
        0xFF6750A4, 0xFF4A90D9, 0xFFE91E63, 0xFF009688,
        0xFFFF9800, 0xFF4CAF50, 0xFFF44336, 0xFF9C27B0,
        0xFF00BCD4, 0xFFFF5722, 0xFF607D8B
    )
    // 匹配 "08:00-09:35" 或 "8:00~9:35" 等格式
    var colorIndex = 0
    val timePattern = Regex("""(\d{1,2}):(\d{2})\s*[-~]\s*(\d{1,2}):(\d{2})""")

    for (line in lines) {
        var dayOfWeek = 0
        var courseName = ""
        var startTime = "08:00"
        var endTime = "09:35"

        val lineLower = line.lowercase()
        // 遍历每一行，查找关键词
        for ((keyword, day) in dayKeywords) {
            if (line.contains(keyword, ignoreCase = true) || lineLower.contains(keyword.lowercase())) {
                dayOfWeek = day
                break
            }
        }
        if (dayOfWeek == 0) continue

        val timeMatch = timePattern.find(line)
        if (timeMatch != null) {
            startTime = "${timeMatch.groupValues[1].padStart(2, '0')}:${timeMatch.groupValues[2]}"
            endTime = "${timeMatch.groupValues[3].padStart(2, '0')}:${timeMatch.groupValues[4]}"
        } else {
            val singleTimePattern = Regex("""(\d{1,2}):(\d{2})""")
            val times = singleTimePattern.findAll(line).toList()
            if (times.size >= 2) {
                startTime = "${times[0].groupValues[1].padStart(2, '0')}:${times[0].groupValues[2]}"
                endTime = "${times[1].groupValues[1].padStart(2, '0')}:${times[1].groupValues[2]}"
            }
        }

        val cleanLine = line
            .replace(timePattern, "")
            .replace(Regex("""\d{1,2}:\d{2}"""), "")
            .replace(Regex("""[周星期一二三四五六日MonTueWedThuFriSatSun]"""), " ")
            .replace(Regex("""[-~/\\]"""), " ").trim()

        val tokens = cleanLine.split(Regex("""[\s,，\t|]+""")).filter {
            it.isNotBlank() && it.length >= 2 && !it.all { c -> c.isDigit() }
        }

        if (tokens.isNotEmpty()) {
            courseName = tokens.first()
            if (courseName.length > 10 || courseName.any { it.isDigit() && it.isLetter().not() }) {
                courseName = tokens.firstOrNull { it.length in 2..12 && it.any { c -> c.isLetter() } } ?: tokens.first()
            }
        }

        if (courseName.isNotBlank() && dayOfWeek in 1..7) {
            courses.add(
                Course(
                    name = courseName, dayOfWeek = dayOfWeek, startTime = startTime, endTime = endTime,
                    color = colors[colorIndex % colors.size].toInt(), startWeek = 1, endWeek = 20, weekType = "ALL"
                )
            )
            colorIndex++
        }
    }

    return courses.distinctBy { "${it.name}_${it.dayOfWeek}_${it.startTime}" }
}
