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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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

/**
 * 개선된 ImageGenerationScreen:
 * - FileProvider 설정 확인 필요 (AndroidManifest.xml과 res/xml/file_paths.xml)
 * - 미리보기 영역 크기가 유효한 경우에만 공유/저장 처리
 * - drawLetterOnCanvas() 함수를 재사용하여 코드 중복 제거
 * - 고정 높이와 padding 등을 사용해 레이아웃 안정성을 향상
 * - 예외 처리를 추가해 에러 발생 시 사용자에게 알림
 */
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
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(defaultFont) }

    // (3) 미리보기 영역 크기 측정
    var previewWidth by remember { mutableIntStateOf(0) }
    var previewHeight by remember { mutableIntStateOf(0) }

    // 화면 레이아웃
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 상단 영역: 뒤로가기 버튼과 화면 제목 (고정 높이)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(
                text = "뒤로가기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(1.dp, shape = RoundedCornerShape(5.dp), color = Color(0xFF94907F))
                    .clickable {
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

        Spacer(modifier = Modifier.height(16.dp))

        // 배경 선택 영역
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

        // 폰트 선택 영역
        Text("폰트 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { selectedFontFamily = defaultFont }) { Text("Def") }
            Button(onClick = { selectedFontFamily = boldAndClearFont }) { Text("Bold") }
            Button(onClick = { selectedFontFamily = cuteFont }) { Text("Cute") }
            Button(onClick = { selectedFontFamily = handwritingFont }) { Text("Hand") }
            Button(onClick = { selectedFontFamily = handwritingThinFont }) { Text("Thin") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 미리보기 영역 (남은 공간 확장)
        Box(
            modifier = Modifier
                .weight(1f)
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

        Spacer(modifier = Modifier.height(16.dp))

        // 하단 버튼 영역: 공유하기와 저장하기 (고정 높이)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    if (previewWidth <= 0 || previewHeight <= 0) {
                        Toast.makeText(context, "미리보기 영역이 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    try {
                        val bitmap = generatePreviewBitmap(previewWidth, previewHeight) { canvas ->
                            drawLetterOnCanvas(context, canvas, selectedBackgroundRes, letterText)
                        }
                        // 공유용: 임시 캐시 파일에 저장 후 FileProvider를 통한 Uri 생성
                        val imageUri = saveBitmapToCache(context, bitmap)
                        if (imageUri != null) {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_STREAM, imageUri)
                                type = "image/jpeg"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "이미지 공유"))
                        } else {
                            Toast.makeText(context, "이미지 공유 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "공유 중 에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("공유하기")
            }
            Button(
                onClick = {
                    if (previewWidth <= 0 || previewHeight <= 0) {
                        Toast.makeText(context, "미리보기 영역이 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    try {
                        val bitmap = generatePreviewBitmap(previewWidth, previewHeight) { canvas ->
                            drawLetterOnCanvas(context, canvas, selectedBackgroundRes, letterText)
                        }
                        val savedUri = saveBitmapToGallery(context, bitmap)
                        if (savedUri != null) {
                            Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "저장 중 에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("저장하기")
            }
        }
    }
}

/**
 * 캔버스에 배경 이미지와 텍스트를 그리는 함수.
 * 공유 및 저장 버튼에서 재사용합니다.
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
    val fm = paint.fontMetrics
    val textHeight = fm.descent - fm.ascent
    val x = previewWidth / 2f
    val y = previewHeight / 2f + (textHeight / 2f - fm.descent)
    canvas.drawText(letterText, x, y, paint)
}

/**
 * 미리보기 영역을 Bitmap으로 캡처하는 함수.
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
 * 디바이스 갤러리에 Bitmap을 저장하는 함수 (MediaStore 사용).
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
 * 임시 캐시 디렉토리에 Bitmap을 저장하고 FileProvider를 통해 Uri를 생성하는 함수 (공유용).
 * AndroidManifest.xml과 res/xml/file_paths.xml에 적절한 설정이 필요합니다.
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
    // Preview에서는 빈 NavController 사용
    val context = LocalContext.current
    val navController = NavController(context)
    ImageGenerationScreen(
        navController = navController,
        letterText = "안녕하세요! 이것은 미리보기입니다."
    )
}
