package com.intel.papyrusbaby.screen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.intel.papyrusbaby.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageGenerationScreen(
    navController: NavController,
    letterText: String // 전달받은 텍스트
) {
    val context = LocalContext.current

    // (1) 배경 선택
    var selectedBackgroundRes by remember { mutableIntStateOf(R.drawable.paper01) }

    // (2) 폰트 선택
    val defaultFont = FontFamily.Default
    val boldAndClearFont = FontFamily(Font(R.font.boldandclear))
    val cuteFont = FontFamily(Font(R.font.cute))
    val handwritingFont = FontFamily(Font(R.font.handwriting))
    val handwritingThinFont = FontFamily(Font(R.font.handwritingthin))
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }

    // (3) 미리보기 영역 크기 측정
    var previewWidth by remember { mutableIntStateOf(0) }
    var previewHeight by remember { mutableIntStateOf(0) }

    // 화면 레이아웃
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단: '뒤로가기' 등
        Row(
            modifier = Modifier.fillMaxSize(fraction = 0.1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "뒤로가기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp, shape = RoundedCornerShape(5.dp), color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 네비게이션 뒤로가기
                        navController.popBackStack()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "이미지 생성",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // 배경 선택
        Text("편지지 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(
                painter = painterResource(id = R.drawable.paper01),
                contentDescription = "Paper 1",
                modifier = Modifier
                    .size(60.dp)
                    .clickable { selectedBackgroundRes = R.drawable.paper01 }
                    .border(
                        width = 2.dp,
                        color = if (selectedBackgroundRes == R.drawable.paper01) Color.Blue else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
            Image(
                painter = painterResource(id = R.drawable.paper02),
                contentDescription = "Paper 2",
                modifier = Modifier
                    .size(60.dp)
                    .clickable { selectedBackgroundRes = R.drawable.paper02 }
                    .border(
                        width = 2.dp,
                        color = if (selectedBackgroundRes == R.drawable.paper02) Color.Blue else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    )
            )
        }

        // 폰트 선택
        Text("폰트 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Button(onClick = { selectedFontFamily = defaultFont }) {
                Text("Def")
            }
            Button(onClick = { selectedFontFamily = boldAndClearFont }) {
                Text("Bold")
            }
            Button(onClick = { selectedFontFamily = cuteFont }) {
                Text("Cute")
            }
            Button(onClick = { selectedFontFamily = handwritingFont }) {
                Text("Hand")
            }
            Button(onClick = { selectedFontFamily = handwritingThinFont }) {
                Text("Thin")
            }
        }

        // 미리보기 영역
        Box(
            modifier = Modifier
                .weight(1f) // 남은 공간 확장
                .clip(RoundedCornerShape(16.dp))
                .onGloballyPositioned { coordinates ->
                    previewWidth = coordinates.size.width
                    previewHeight = coordinates.size.height
                }
        ) {
            Image(
                painter = painterResource(id = selectedBackgroundRes),
                contentDescription = "미리보기 배경",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Text(
                text = letterText,
                fontSize = 20.sp,
                fontFamily = selectedFontFamily,
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp)
            )
        }

        // 공유 / 저장 버튼
        Row(
            modifier = Modifier
                .fillMaxSize(fraction = 0.2f),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (previewWidth > 0 && previewHeight > 0) {
                        val bitmap =
                            generatePreviewBitmap(previewWidth, previewHeight) { canvas ->
                                // 1. 배경 이미지 그리기
                                val bgBitmap = BitmapFactory.decodeResource(
                                    context.resources, selectedBackgroundRes
                                )
                                val destRect = Rect(0, 0, previewWidth, previewHeight)
                                canvas.drawBitmap(bgBitmap, null, destRect, null)

                                // 2. 텍스트 그리기 (중앙 정렬)
                                val paint = android.graphics.Paint().apply {
                                    color = android.graphics.Color.BLACK
                                    // 20.sp를 픽셀 단위로 변환 (대략적으로 density 곱)
                                    val density = context.resources.displayMetrics.density
                                    textSize = 20 * density
                                    textAlign = android.graphics.Paint.Align.CENTER
                                }
                                // 텍스트의 수직 중앙 위치 계산
                                val fm = paint.fontMetrics
                                val textHeight = fm.descent - fm.ascent
                                val x = previewWidth / 2f
                                val y = previewHeight / 2f + (textHeight / 2f - fm.descent)
                                canvas.drawText(letterText, x, y, paint)
                            }
                        // 공유용: 임시 캐시 파일에 저장 후 FileProvider를 통한 Uri 생성
                        val imageUri = saveBitmapToCache(context, bitmap)
                        imageUri?.let { uri ->
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, uri)
                                type = "image/jpeg"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(
                                    shareIntent, "이미지 공유"
                                )
                            )
                        }
                    }
                }) {
                Text("공유하기")
            }
            Button(onClick = {
                if (previewWidth > 0 && previewHeight > 0) {
                    val bitmap =
                        generatePreviewBitmap(previewWidth, previewHeight) { canvas ->
                            // 1. 배경 이미지 그리기
                            val bgBitmap = BitmapFactory.decodeResource(
                                context.resources,
                                selectedBackgroundRes
                            )
                            val destRect = Rect(0, 0, previewWidth, previewHeight)
                            canvas.drawBitmap(bgBitmap, null, destRect, null)

                            // 2. 텍스트 그리기 (중앙 정렬)
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.BLACK
                                // 20.sp를 픽셀 단위로 변환 (대략적으로 density 곱)
                                val density = context.resources.displayMetrics.density
                                textSize = 20 * density
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                            // 텍스트의 수직 중앙 위치 계산
                            val fm = paint.fontMetrics
                            val textHeight = fm.descent - fm.ascent
                            val x = previewWidth / 2f
                            val y = previewHeight / 2f + (textHeight / 2f - fm.descent)
                            canvas.drawText(letterText, x, y, paint)
                        }
                    val savedUri = saveBitmapToGallery(context, bitmap)
                    if (savedUri != null) {
                        // 저장 성공
                        // Toast 등으로 알림 (여기서는 간단하게 로그 출력)
                        Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                    } else {
                        // 저장 실패 알림
                    }
                }
            }) {
                Text("저장하기")
            }
        }
    }
}

/**
 * 실제 캔버스에 배경 이미지와 텍스트를 그리는 함수
 */
fun drawLetterOnCanvas(
    context: Context,
    canvas: Canvas,
    backgroundRes: Int,
    letterText: String
) {
    val previewWidth = canvas.width
    val previewHeight = canvas.height

    // 1. 배경 이미지 그리기
    val bgBitmap = BitmapFactory.decodeResource(context.resources, backgroundRes)
    val destRect = Rect(0, 0, previewWidth, previewHeight)
    canvas.drawBitmap(bgBitmap, null, destRect, null)

    // 2. 텍스트 그리기 (중앙 정렬)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        val density = context.resources.displayMetrics.density
        textSize = 20 * density
        textAlign = android.graphics.Paint.Align.CENTER
    }
    // 텍스트의 수직 중앙 위치 계산
    val fm = paint.fontMetrics
    val textHeight = fm.descent - fm.ascent
    val x = previewWidth / 2f
    val y = previewHeight / 2f + (textHeight / 2f - fm.descent)
    canvas.drawText(letterText, x, y, paint)
}

/**
 * 미리보기 영역을 Bitmap으로 캡처 (임시)
 */
fun generatePreviewBitmap(
    previewWidth: Int,
    previewHeight: Int,
    onDraw: (Canvas) -> Unit
): Bitmap {
    val bitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    onDraw(canvas)
    return bitmap
}

/**
 * 디바이스의 갤러리에 Bitmap을 저장 (MediaStore)
 */
fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "generated_image"
): android.net.Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/PapyrusBaby")
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let {
        resolver.openOutputStream(it)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        }
    }
    return uri
}

/**
 * 임시 캐시 디렉토리에 Bitmap을 저장 후 FileProvider로 Uri 생성 (공유용)
 */
fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "temp_image.jpg"
): android.net.Uri? {
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Preview(showBackground = true)
@Composable
fun ImageGenerationScreenPreview() {
    val navController = NavController(LocalContext.current)
    ImageGenerationScreen(
        navController = navController,
        letterText = "안녕하세요! 이것은 미리보기입니다."
    )
}