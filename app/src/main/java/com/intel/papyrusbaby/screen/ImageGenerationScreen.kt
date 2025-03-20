package com.intel.papyrusbaby.screen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.intel.papyrusbaby.R
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageGenerationScreen(
    navController: NavController,
    letterText: String
) {
    val context = LocalContext.current

    // 1. 폰트 패밀리 정의
    val defaultFont = FontFamily.Default
    val boldFont = FontFamily(Font(R.font.boldandclear))
    val cuteFont = FontFamily(Font(R.font.cute))
    val handFont = FontFamily(Font(R.font.handwriting))
    val handThinFont = FontFamily(Font(R.font.handwritingthin))

    // 2. Typeface는 한 번 로딩해서 캐싱
    val defaultTypeface = Typeface.DEFAULT
    val boldTypeface = ResourcesCompat.getFont(context, R.font.boldandclear) ?: Typeface.DEFAULT
    val cuteTypeface = ResourcesCompat.getFont(context, R.font.cute) ?: Typeface.DEFAULT
    val handwritingTypeface =
        ResourcesCompat.getFont(context, R.font.handwriting) ?: Typeface.DEFAULT
    val handwritingThinTypeface =
        ResourcesCompat.getFont(context, R.font.handwritingthin) ?: Typeface.DEFAULT

    // 3. FontFamily와 Typeface 매핑 (drawLetterOnCanvas에서 사용)
    val fontMapping = remember {
        mapOf(
            defaultFont to defaultTypeface,
            boldFont to boldTypeface,
            cuteFont to cuteTypeface,
            handFont to handwritingTypeface,
            handThinFont to handwritingThinTypeface
        )
    }

    // 배경 및 폰트 선택 상태
    var backgroundRes by remember { mutableIntStateOf(R.drawable.paper01) }
    var selectedFont by remember { mutableStateOf<FontFamily>(defaultFont) }

    // 고정 이미지 해상도 (원본 생성)
    val fixedWidth = 768
    val fixedHeight = 1024

    // 생성된 Bitmap 상태
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // 상태 변경 시마다 새 이미지를 생성 (try-catch로 예외 로깅 추가)
    LaunchedEffect(backgroundRes, selectedFont, letterText) {
        try {
            Log.d(
                "ImageGenerationScreen",
                "LaunchedEffect triggered - backgroundRes: $backgroundRes, selectedFont: $selectedFont, letterText: $letterText"
            )
            generatedBitmap = generateFixedSizeBitmap(fixedWidth, fixedHeight) { canvas ->
                drawLetterOnCanvas(
                    context = context,
                    canvas = canvas,
                    backgroundRes = backgroundRes,
                    letterText = letterText,
                    fontFamily = selectedFont,
                    width = fixedWidth,
                    height = fixedHeight,
                    fontMapping = fontMapping
                )
            }
            Log.d("ImageGenerationScreen", "New image generated")
        } catch (e: Exception) {
            Log.e("ImageGenerationScreen", "Error generating image", e)
        }
    }

    Column(
        modifier = Modifier
            .background(Color(0xFFfffae6))
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        TopBar(navController = navController, title = "이미지 생성")

        Spacer(modifier = Modifier.height(16.dp))

        // 편지지 선택
        BackgroundSelector(
            selectedBackgroundRes = backgroundRes,
            onBackgroundSelected = { res ->
                Log.d("BackgroundSelector", "Selected background: $res")
                backgroundRes = res
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 폰트 선택
        FontSelector(
            defaultFont = defaultFont,
            boldAndClearFont = boldFont,
            cuteFont = cuteFont,
            handwritingFont = handFont,
            handwritingThinFont = handThinFont,
            selectedFont = selectedFont,
            onFontSelected = { font ->
                Log.d("FontSelector", "Selected font: $font")
                selectedFont = font
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 생성된 이미지 미리보기
        generatedBitmap?.let { bitmap ->
            GeneratedLetterPreview(bitmap = bitmap)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 공유/저장 버튼
        generatedBitmap?.let { bitmap ->
            ActionButtons(context = context, bitmap = bitmap)
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
            listOf(
                R.drawable.paper01,
                R.drawable.paper02,
                R.drawable.paper03,
                R.drawable.paper04,
                R.drawable.paper05
            ).forEach { res ->
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
    selectedFont: FontFamily,
    onFontSelected: (FontFamily) -> Unit
) {
    Column {
        Text(text = "폰트 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
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
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFF5C5945)
                        )
                        .background(
                            color = if (selectedFont == fontFamily) Color(0xFF5C5945) else Color.Transparent,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable { onFontSelected(fontFamily) }
                ) {
                    Text(
                        text = label,
                        color = if (selectedFont == fontFamily) Color(0xFFFFFAE6) else Color(
                            0xFF5C5945
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

            }
        }
    }
}

@Composable
fun GeneratedLetterPreview(bitmap: Bitmap) {
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Generated Letter",
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
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
        Box(
            modifier = Modifier
                .border(
                    1.dp,
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFF5C5945)
                )
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(5.dp)
                )
                .clickable {
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
                            Toast
                                .makeText(context, "이미지 공유 실패", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch (e: Exception) {
                        Toast
                            .makeText(context, "공유 중 에러 발생: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        ) {
            Text(
                text = "공유하기",
                color = Color(
                    0xFF5C5945
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Box(
            modifier = Modifier
                .border(
                    1.dp,
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFF5C5945)
                )
                .background(
                    color = Color.Transparent,
                    shape = RoundedCornerShape(5.dp)
                )
                .clickable {
                    try {
                        val savedUri = saveBitmapToGallery(context, bitmap)
                        if (savedUri != null) {
                            Toast
                                .makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT)
                                .show()
                        } else {
                            Toast
                                .makeText(context, "저장 실패", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } catch (e: Exception) {
                        Toast
                            .makeText(context, "저장 중 에러 발생: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
        ) {
            Text(
                text = "저장하기",
                color = Color(
                    0xFF5C5945
                ),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

    }
}

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

fun drawLetterOnCanvas(
    context: Context,
    canvas: Canvas,
    backgroundRes: Int,
    letterText: String,
    fontFamily: FontFamily,
    width: Int,
    height: Int,
    fontMapping: Map<FontFamily, Typeface>
) {
    Log.d(
        "drawLetterOnCanvas",
        "Drawing with backgroundRes: $backgroundRes, letterText: $letterText"
    )

    // VectorDrawable을 Bitmap으로 변환 시도
    val bgBitmap = getBitmapFromVectorDrawable(context, backgroundRes, width, height)
        ?: BitmapFactory.decodeResource(context.resources, backgroundRes)

    if (bgBitmap != null) {
        canvas.drawBitmap(bgBitmap, null, Rect(0, 0, width, height), null)
    } else {
        canvas.drawColor(Color.White.toArgb())
    }

    // 텍스트 그리기
    val textPaint = TextPaint().apply {
        color = Color.Black.toArgb()
        val density = context.resources.displayMetrics.density
        textSize = 20 * density
        textAlign = Paint.Align.LEFT
        isAntiAlias = true
        typeface = fontMapping[fontFamily] ?: Typeface.DEFAULT
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
    val centerY = (height - textHeight) / 2f
    canvas.translate(padding.toFloat(), centerY)
    staticLayout.draw(canvas)
    canvas.restore()
}

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

fun getBitmapFromVectorDrawable(context: Context, resId: Int, width: Int, height: Int): Bitmap? {
    val drawable = AppCompatResources.getDrawable(context, resId) ?: return null
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, width, height)
    drawable.draw(canvas)
    return bitmap
}

@Preview(showBackground = true)
@Composable
fun ImageGenerationScreenPreview() {
    val context = LocalContext.current
    val navController = NavController(context)
    val sampleText =
        "서로의 손길로 이룬 꿈의 조각들, 여러분의 노고가 이 프로젝트를 빛나게 했습니다. 한 걸음 한 걸음 함께해 온 시간 속에서, 여러분의 열정과 헌신이 얼마나 큰 힘이 되었는지 모릅니다. 힘든 순간마다 서로를 격려하며 나아간 우리, 그 과정이야말로 진정한 가치입니다. 이제는 그 결실을 바라보며, 서로의 수고를 인정하고 감사하는 마음으로 더 나아갑시다. 우리의 작은 발걸음이 모여 큰 길을 이룰 것임을 믿습니다. 함께 해주셔서 진심으로 고맙습니다."
    ImageGenerationScreen(navController = navController, letterText = sampleText)
}
