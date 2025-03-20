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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.intel.papyrusbaby.ui.theme.LetterBackground
import com.intel.papyrusbaby.ui.theme.LetterFont
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageGenerationScreen(
    navController: NavController,
    letterText: String
) {
    val context = LocalContext.current

    // 상태 변경: enum을 이용
    var selectedBackground by remember { mutableStateOf(LetterBackground.PAPER_1) }
    var selectedFont by remember { mutableStateOf(LetterFont.BOLD) }

    // 고정 이미지 해상도
    val fixedWidth = 1440
    val fixedHeight = 1920

    // 생성된 Bitmap 상태
    var generatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Typeface 매핑을 enum을 사용해 생성 (LetterFont별)
    val fontMapping: Map<FontFamily, Typeface> = remember {
        LetterFont.entries.associate { letterFont ->
            letterFont.getFontFamily() to letterFont.getTypeface(context)
        }
    }

    LaunchedEffect(selectedBackground, selectedFont, letterText) {
        try {
            generatedBitmap = generateFixedSizeBitmap(fixedWidth, fixedHeight) { canvas ->
                drawLetterOnCanvas(
                    context = context,
                    canvas = canvas,
                    backgroundRes = selectedBackground.resId,
                    letterText = letterText,
                    fontFamily = selectedFont.getFontFamily(),
                    width = fixedWidth,
                    height = fixedHeight,
                    fontMapping = fontMapping
                )
            }
        } catch (e: Exception) {
            Log.e("ImageGenerationScreen", "Error generating image", e)
        }
    }
    Column(
        modifier = Modifier
            .background(Color(0xFFfffae6))
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        BackgroundSelector(
            selectedBackground = selectedBackground,
            onBackgroundSelected = { selectedBackground = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        FontSelector(
            selectedFont = selectedFont,
            onFontSelected = { selectedFont = it }
        )
        Spacer(modifier = Modifier.height(16.dp))
        generatedBitmap?.let { bitmap ->
            GeneratedLetterPreview(bitmap = bitmap)
        }
        Spacer(modifier = Modifier.height(16.dp))
        generatedBitmap?.let { bitmap ->
            ActionButtons(context = context, bitmap = bitmap)
        }
    }
}

@Composable
fun BackgroundSelector(
    selectedBackground: LetterBackground,
    onBackgroundSelected: (LetterBackground) -> Unit
) {
    Column {
        Text(text = "편지지를 골라주세요", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LetterBackground.entries.forEach { background ->
                Image(
                    painter = painterResource(id = background.resId),
                    contentDescription = "편지지 ${background.name}",
                    modifier = Modifier
                        .size(60.dp)
                        .clickable { onBackgroundSelected(background) }
                        .border(
                            width = if (selectedBackground == background) 2.dp else 1.dp,
                            color = if (selectedBackground == background) Color(0xFF5C5945) else Color(
                                0xFF94907F
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun FontSelector(
    selectedFont: LetterFont,
    onFontSelected: (LetterFont) -> Unit
) {
    Column {
        Text(text = "글꼴을 골라주세요", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
        ) {
            LetterFont.entries.forEach { letterFont ->
                Box(
                    modifier = Modifier
                        .border(1.dp, shape = RoundedCornerShape(5.dp), color = Color(0xFF5C5945))
                        .background(
                            color = if (selectedFont == letterFont) Color(0xFF5C5945) else Color.Transparent,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable { onFontSelected(letterFont) }
                ) {
                    Text(
                        text = letterFont.label,
                        color = if (selectedFont == letterFont) Color(0xFFFFFAE6) else Color(
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
