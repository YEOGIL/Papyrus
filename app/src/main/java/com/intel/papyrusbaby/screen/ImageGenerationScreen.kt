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
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
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
    letterText: String
) {
    val context = LocalContext.current

    // 배경 및 폰트 선택 상태
    var backgroundRes by remember { mutableIntStateOf(R.drawable.paper01) }
    val defaultFont = FontFamily.Default
    val boldFont = FontFamily(Font(R.font.boldandclear))
    val cuteFont = FontFamily(Font(R.font.cute))
    val handFont = FontFamily(Font(R.font.handwriting))
    val handThinFont = FontFamily(Font(R.font.handwritingthin))
    var selectedFont by remember { mutableStateOf<FontFamily>(defaultFont) }

    // 고정 이미지 크기
    val fixedWidth = 1024
    val fixedHeight = 768

    // 편지 이미지를 고정 크기로 생성
    // 배경, 폰트, 텍스트가 바뀔 때마다 새로운 이미지를 생성하여 미리보기에 표시
    val generatedBitmap = remember(backgroundRes, letterText, selectedFont) {
        generateFixedSizeBitmap(fixedWidth, fixedHeight) { canvas ->
            drawLetterOnCanvas(
                context = context,
                canvas = canvas,
                backgroundRes = backgroundRes,
                letterText = letterText,
                fontFamily = selectedFont,
                width = fixedWidth,
                height = fixedHeight
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TopBar(navController = navController, title = "이미지 생성")

        Spacer(modifier = Modifier.height(16.dp))

        // 편지지 선택
        BackgroundSelector(
            selectedBackgroundRes = backgroundRes,
            onBackgroundSelected = { backgroundRes = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 폰트 선택
        FontSelector(selectedFont = selectedFont, onFontSelected = { selectedFont = it })

        Spacer(modifier = Modifier.height(16.dp))

        // 생성된 편지 이미지 미리보기 (화면 너비에 맞춰 비율 유지)
        GeneratedLetterPreview(bitmap = generatedBitmap)

        Spacer(modifier = Modifier.height(16.dp))

        // 공유/저장 버튼 (원본 Bitmap 사용)
        ActionButtons(context = context, bitmap = generatedBitmap)
    }
}

@Composable
fun TopBar(navController: NavController, title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "뒤로가기",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF5C5945),
            modifier = Modifier
                .clickable { navController.popBackStack() }
                .border(1.dp, shape = RoundedCornerShape(5.dp), color = Color(0xFF94907F))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun BackgroundSelector(
    selectedBackgroundRes: Int,
    onBackgroundSelected: (Int) -> Unit
) {
    Column {
        Text(text = "편지지 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(R.drawable.paper01, R.drawable.paper02).forEach { res ->
                Image(
                    painter = painterResource(id = res),
                    contentDescription = "Paper Image",
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onBackgroundSelected(res) }
                        .border(
                            width = 2.dp,
                            color = if (selectedBackgroundRes == res) Color.Blue else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun FontSelector(
    selectedFont: FontFamily,
    onFontSelected: (FontFamily) -> Unit
) {
    Column {
        Text(text = "폰트 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            val fonts = listOf(
                "Def" to FontFamily.Default,
                "Bold" to FontFamily(Font(R.font.boldandclear)),
                "Cute" to FontFamily(Font(R.font.cute)),
                "Hand" to FontFamily(Font(R.font.handwriting)),
                "Thin" to FontFamily(Font(R.font.handwritingthin))
            )
            fonts.forEach { (label, fontFamily) ->
                Button(onClick = { onFontSelected(fontFamily) }) {
                    Text(text = label)
                }
            }
        }
    }
}

@Composable
fun GeneratedLetterPreview(bitmap: Bitmap) {
    // 원본 비율 계산: 가로/세로
    val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

    // 화면 너비에 맞춰 aspectRatio로 보여주기
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Generated Letter",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .clip(RoundedCornerShape(16.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
fun ActionButtons(context: Context, bitmap: Bitmap) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 공유하기 버튼
        Button(onClick = {
            try {
                val imageUri = saveBitmapToCache(context, bitmap)
                if (imageUri != null) {
                    val shareIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_STREAM, imageUri)
                        type = "image/png"
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "이미지 공유"))
                } else {
                    Toast.makeText(context, "이미지 공유 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "공유 중 에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("공유하기")
        }

        // 저장하기 버튼
        Button(onClick = {
            try {
                val savedUri = saveBitmapToGallery(context, bitmap)
                if (savedUri != null) {
                    Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "저장 실패", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "저장 중 에러 발생: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text("저장하기")
        }
    }
}

/**
 * 고정 크기의 Bitmap을 생성하는 함수.
 */
fun generateFixedSizeBitmap(
    width: Int = 1024, // 원하는 고정 너비
    height: Int = 768, // 원하는 고정 높이
    onDraw: (Canvas) -> Unit
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    onDraw(canvas)
    return bitmap
}

/**
 * 배경 이미지와 텍스트를 캔버스에 그려 실제 편지와 같은 이미지를 생성하는 함수.
 */
fun drawLetterOnCanvas(
    context: Context,
    canvas: Canvas,
    backgroundRes: Int,
    letterText: String,
    fontFamily: FontFamily,
    width: Int,
    height: Int
) {
    // 1. 배경 이미지 그리기 (null 체크)
    val bgBitmap = BitmapFactory.decodeResource(context.resources, backgroundRes)
    if (bgBitmap != null) {
        canvas.drawBitmap(bgBitmap, null, Rect(0, 0, width, height), null)
    } else {
        canvas.drawColor(android.graphics.Color.WHITE)
    }

    // 2. 텍스트 그리기 (StaticLayout 사용)
    val textPaint = TextPaint().apply {
        color = android.graphics.Color.BLACK
        val density = context.resources.displayMetrics.density
        textSize = 20 * density
        textAlign = android.graphics.Paint.Align.LEFT // StaticLayout에 맞춘 LEFT 정렬
        isAntiAlias = true
    }

    // 여백 적용
    val padding = (50 * context.resources.displayMetrics.density).toInt()
    val textWidth = width - 2 * padding

    // StaticLayout으로 줄바꿈 처리
    val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        StaticLayout.Builder.obtain(letterText, 0, letterText.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(0f, 1.2f)
            .build()
    } else {
        @Suppress("DEPRECATION")
        StaticLayout(
            letterText,
            textPaint,
            textWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1.2f,
            0f,
            false
        )
    }

    val textHeight = staticLayout.height
    // 수직 중앙 배치
    canvas.save()
    val centerY = (height - textHeight) / 2f
    canvas.translate(padding.toFloat(), centerY)
    staticLayout.draw(canvas)
    canvas.restore()
}

/**
 * 갤러리에 Bitmap을 저장하는 함수 (JPEG 포맷).
 */
fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "generated_image.jpg"
): android.net.Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/PapyrusBaby")
        }
    }
    val resolver = context.contentResolver
    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    uri?.let {
        resolver.openOutputStream(it)?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        }
    }
    return uri
}

/**
 * 임시 캐시 디렉토리에 Bitmap을 저장하고 FileProvider를 통해 Uri를 생성 (공유용, PNG 포맷).
 */
fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "temp_image.png"
): android.net.Uri? {
    val cacheDir = File(context.cacheDir, "images")
    if (!cacheDir.exists()) cacheDir.mkdirs()
    val file = File(cacheDir, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Preview(showBackground = true)
@Composable
fun ImageGenerationScreenPreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    ImageGenerationScreen(navController = navController, letterText = "안녕하세요! 미리보기 텍스트입니다.")
}
