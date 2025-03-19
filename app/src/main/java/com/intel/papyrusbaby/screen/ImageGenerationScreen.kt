package com.intel.papyrusbaby.screen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
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
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.NavController
import com.caverock.androidsvg.SVG
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
    val defaultTypeface = Typeface.DEFAULT
    val boldTypeface = ResourcesCompat.getFont(context, R.font.boldandclear) ?: Typeface.DEFAULT
    val cuteTypeface = ResourcesCompat.getFont(context, R.font.cute) ?: Typeface.DEFAULT
    val handwritingTypeface = ResourcesCompat.getFont(context, R.font.handwriting) ?: Typeface.DEFAULT
    val handwritingThinTypeface = ResourcesCompat.getFont(context, R.font.handwritingthin) ?: Typeface.DEFAULT

    var selectedFont by remember { mutableStateOf<FontFamily>(defaultFont) }

    // 고정 이미지 해상도 (원본 생성)
    val fixedWidth = 768
    val fixedHeight = 1024

    // 가장 최근에 생성된 원본 Bitmap 상태
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 선택이 변경될 때마다 전체 해상도의 편지 이미지를 새로 생성 후 미리보기로 업데이트
    LaunchedEffect(backgroundRes, selectedFont, letterText) {
        Log.d("ImageGenerationScreen", "LaunchedEffect triggered - backgroundRes: $backgroundRes, selectedFont: $selectedFont, letterText: $letterText")
        generatedBitmap = generateFixedSizeBitmap(fixedWidth, fixedHeight) { canvas ->
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
        Log.d("ImageGenerationScreen", "New image generated")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()) // 화면이 넘어가면 스크롤
            .padding(16.dp)
    ) {
        TopBar(navController = navController, title = "이미지 생성")

        Spacer(modifier = Modifier.height(16.dp))

        // 1) 편지지 선택
        BackgroundSelector(
            selectedBackgroundRes = backgroundRes,
            onBackgroundSelected = { res ->
                Log.d("BackgroundSelector", "Selected background: $res")
                backgroundRes = res
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 2) 폰트 선택
        FontSelector(
            defaultFont = defaultFont,
            boldAndClearFont = boldFont,
            cuteFont = cuteFont,
            handwritingFont = handFont,
            handwritingThinFont = handThinFont,
            onFontSelected = { font ->
                Log.d("FontSelector", "Selected font: $font")
                selectedFont = font
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3) 생성된 전체 원본 편지 이미지 미리보기
        if (generatedBitmap != null) {
            GeneratedLetterPreview(bitmap = generatedBitmap!!)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4) 공유/저장 버튼 (원본 Bitmap 사용)
        if (generatedBitmap != null) {
            ActionButtons(context = context, bitmap = generatedBitmap!!)
        }
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
    defaultFont: FontFamily,
    boldAndClearFont: FontFamily,
    cuteFont: FontFamily,
    handwritingFont: FontFamily,
    handwritingThinFont: FontFamily,
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
                "Def" to defaultFont,
                "Bold" to boldAndClearFont,
                "Cute" to cuteFont,
                "Hand" to handwritingFont,
                "Thin" to handwritingThinFont
            )
            fonts.forEach { (label, fontFamily) ->
                Button(onClick = { onFontSelected(fontFamily) }) {
                    Text(text = label)
                }
            }
        }
    }
}

/**
 * 생성된 Bitmap을 비율에 맞춰 화면에 표시하는 미리보기
 * - 원본 전체 이미지를 화면 너비에 맞춰 축소하여 표시
 */
@Composable
fun GeneratedLetterPreview(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Generated Letter",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f) // 가로 3 : 세로 4 비율로 고정
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
        // 공유하기 버튼 (원본 Bitmap 사용)
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

        // 저장하기 버튼 (원본 Bitmap 사용)
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

/** 고정 크기의 Bitmap을 생성하는 함수 (원본 해상도) */
fun generateFixedSizeBitmap(
    width: Int = 768,
    height: Int = 1024,
    onDraw: (Canvas) -> Unit
): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    onDraw(canvas)
    return bitmap
}

/** 배경 이미지 + 텍스트를 그려 편지 형태로 만드는 함수 */
fun drawLetterOnCanvas(
    context: Context,
    canvas: Canvas,
    backgroundRes: Int,
    letterText: String,
    fontFamily: FontFamily,
    width: Int,
    height: Int
) {
    Log.d("drawLetterOnCanvas", "Drawing with backgroundRes: $backgroundRes, letterText: $letterText")

    // 배경 이미지 처리: SVG 파일을 Bitmap으로 변환 시도
    val bgBitmap = getBitmapFromSvgResource(context, backgroundRes, width, height)
        ?: BitmapFactory.decodeResource(context.resources, backgroundRes)

    if (bgBitmap != null) {
        canvas.drawBitmap(bgBitmap, null, Rect(0, 0, width, height), null)
    } else {
        canvas.drawColor(Color.White.toArgb())
    }

    // 텍스트 그리기 (StaticLayout 사용)
    val textPaint = TextPaint().apply {
        color = Color.Black.toArgb()
        val density = context.resources.displayMetrics.density
        textSize = 20 * density
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
        // fontFamily에 따라 적절한 Typeface 적용 (앞서 설정한 Typeface 변수 사용)
        typeface = when (fontFamily) {
            FontFamily.Default -> Typeface.DEFAULT
            // 예시로 bold 폰트 처리 (필요에 따라 다른 폰트도 처리)
            else -> Typeface.DEFAULT
        }
    }

    val padding = (50 * context.resources.displayMetrics.density).toInt()
    val textWidth = width - 2 * padding

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
    canvas.save()
    // 텍스트를 수직 중앙에 배치
    val centerY = (height - textHeight) / 2f
    canvas.translate(padding.toFloat(), centerY)
    staticLayout.draw(canvas)
    canvas.restore()
}

/** 갤러리에 Bitmap을 저장 (JPEG) */
fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "generated_image.jpg"
): android.net.Uri? {
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Pictures")
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

/** 임시 캐시 디렉토리에 Bitmap을 저장하고 FileProvider로 Uri 생성 (PNG) */
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

/** SVG 리소스를 Bitmap으로 변환하는 함수 */
fun getBitmapFromSvgResource(context: Context, resId: Int, width: Int, height: Int): Bitmap? {
    return try {
        // SVG 파일을 InputStream으로 열기
        val inputStream = context.resources.openRawResource(resId)
        val svg = SVG.getFromInputStream(inputStream)
        // 문서 크기를 지정해줍니다.
        svg.setDocumentWidth(width.toFloat())
        svg.setDocumentHeight(height.toFloat())
        // Bitmap 생성 및 캔버스 연결
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        // SVG를 캔버스에 렌더링
        svg.renderToCanvas(canvas)
        bitmap
    } catch (e: Exception) {
        Log.e("SVGConversion", "Error converting SVG to Bitmap", e)
        null
    }
}

@Preview(showBackground = true)
@Composable
fun ImageGenerationScreenPreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    ImageGenerationScreen(navController = navController, letterText = "안녕하세요! 미리보기 텍스트입니다.")
}
