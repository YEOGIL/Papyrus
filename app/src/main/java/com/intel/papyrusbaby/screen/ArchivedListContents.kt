package com.intel.papyrusbaby.screen

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.intel.papyrusbaby.R
import com.intel.papyrusbaby.firebase.ArchiveItem
import com.intel.papyrusbaby.firebase.ArchiveRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@Composable
fun ArchivedListContentsScreen(
    docId: String,
    navController: NavController
) {
    // 로컬 컨텍스트, 코루틴 스코프
    val scope = rememberCoroutineScope()

    // Firestore에서 받아올 ArchiveItem
    var archiveItem by remember { mutableStateOf<ArchiveItem?>(null) }
    // 로딩 상태
    var isLoading by remember { mutableStateOf(true) }

    // 화면 최초 진입 시 Firestore에서 문서 조회
    LaunchedEffect(docId) {
        scope.launch {
            val item = ArchiveRepository.getArchiveItem(docId)
            archiveItem = item
            isLoading = false
        }
    }

    // 실제 레터 내용 표시
    if (archiveItem == null) {
        // 문서 조회 실패 또는 없는 경우
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFAE6)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "아카이브 데이터를 불러올 수 없습니다.")
        }
    } else {
        // WrittenLetterScreen의 UI 레이아웃을 재사용하여 표시
        ShowArchivedLetterContents(archiveItem = archiveItem!!, navController = navController)
    }
}


// 아카이브된 Letter 내용 표시 UI
@Composable
fun ShowArchivedLetterContents(
    archiveItem: ArchiveItem,
    navController: NavController
) {
    // 복사 / 공유 / 토스트 등에 사용
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var showImageGenDialog by remember { mutableStateOf(false) }

    // 화면 구성 (WrittenLetterScreen 참고)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAE6))
            .padding(16.dp)
    ) {

        // 1) 메타 정보 (작성일, 작가 등)
        Box() {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "작성일: ${archiveItem.writtenDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "작가: ${archiveItem.author.ifEmpty { "선택 없음" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "글 형식: ${archiveItem.docType.ifEmpty { "선택 없음" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "테마: ${archiveItem.themeList.joinToString(", ").ifEmpty { "선택 없음" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "상세 내용: ${archiveItem.detail}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
            }

            Text(
                text = "뒤로가기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 목록 화면으로 돌아가기
                        navController.popBackStack()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2) 생성된 텍스트 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF7ECCD), shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            // 여기선 별도 아이콘(아카이브 아이콘) 클릭 기능은 굳이 필요 없다면 제거
            // (이미 저장된 상태)
            Text(
                text = archiveItem.generatedText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF221F10),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3) “보내기”, “복사하기”, “나가기” 같은 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "보내기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 공유 인텐트 생성
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, archiveItem.generatedText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            Text(
                text = "복사하기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        clipboardManager.setText(AnnotatedString(archiveItem.generatedText))
                        Toast
                            .makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            Text(
                text = "이미지 생성",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 이미지 생성 화면
                        showImageGenDialog = true
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            // 다이얼로그 표시
            if (showImageGenDialog) {
                ShowImageGenerationDialog(
                    letterText = archiveItem.generatedText,
                    onDismiss = { showImageGenDialog = false },
                    onShare = { bitmap ->
                        // 공유 인텐트 예시
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "이미지 공유") // 임시 텍스트
                            type = "image/jpeg"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    },
                    onSave = { bitmap ->
                        // 디바이스에 저장하는 함수 구현 (예: saveBitmapToGallery(bitmap))
                        val savedUri = saveBitmapToGallery(context, bitmap)
                        if (savedUri != null) {
                            Toast.makeText(context, "저장되었습니다.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "저장 실패.", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }

        }
    }
}

// placeholder 함수: 미리보기 영역을 Bitmap으로 캡처하는 로직을 구현해야 합니다.
// 실제 구현 시 Accompanist의 Capture API 또는 Android View 캡처 기능을 사용할 수 있습니다.
fun generatePreviewBitmap(previewWidth: Int, previewHeight: Int, onDraw: (Canvas) -> Unit): Bitmap {
    val bitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    onDraw(canvas)
    return bitmap
}

@Composable
fun ShowImageGenerationDialog(
    letterText: String,
    onDismiss: () -> Unit,
    onShare: (Bitmap) -> Unit,
    onSave: (Bitmap) -> Unit
) {
    // 배경 선택: drawable에 paper1, paper2가 있다고 가정 (R.drawable.paper1, R.drawable.paper2)
    var selectedBackgroundRes by remember { mutableIntStateOf(R.drawable.paper1) }
    // 폰트 선택
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }
    // 미리보기 영역의 크기를 측정하기 위한 상태
    var previewWidth by remember { mutableIntStateOf(0) }
    var previewHeight by remember { mutableIntStateOf(0) }
    // 미리보기 영역에 그려진 내용을 Bitmap으로 캡처한 결과 (실제 구현 시 onShare/onSave에서 호출)
    var previewBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val defaultFont = FontFamily.Default
    val boldAndClearFont = FontFamily(Font(R.font.boldandclear))
    val cuteFont = FontFamily(Font(R.font.cute))
    val handwritingFont = FontFamily(Font(R.font.handwriting))
    val handwritingThinFont = FontFamily(Font(R.font.handwritingthin))

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { /* confirm 버튼은 따로 사용하지 않고 하단 액션 버튼으로 처리 */ },
        title = {
            Text(
                text = "이미지 생성",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 배경 선택 영역
                Text(text = "편지지 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // paper1 버튼
                    Image(
                        painter = painterResource(id = R.drawable.paper1),
                        contentDescription = "Paper 1",
                        modifier = Modifier
                            .clickable { selectedBackgroundRes = R.drawable.paper1 }
                            .border(
                                width = 2.dp,
                                color = if (selectedBackgroundRes == R.drawable.paper1) Color.Blue else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                    // paper2 버튼
                    Image(
                        painter = painterResource(id = R.drawable.paper2),
                        contentDescription = "Paper 2",
                        modifier = Modifier
                            .clickable { selectedBackgroundRes = R.drawable.paper2 }
                            .border(
                                width = 2.dp,
                                color = if (selectedBackgroundRes == R.drawable.paper2) Color.Blue else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }

                // 폰트 선택 영역
                Text(text = "폰트 선택", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Default
                    Button(onClick = { selectedFontFamily = defaultFont }) {
                        Text("Def")
                    }
                    //Bold & Clear
                    Button(onClick = { selectedFontFamily = boldAndClearFont }) {
                        Text("Bold")
                    }
                    // Cute
                    Button(onClick = { selectedFontFamily = cuteFont }) {
                        Text("Cute")
                    }
                    // Handwriting
                    Button(onClick = { selectedFontFamily = handwritingFont }) {
                        Text("Hand")
                    }
                    // Handwriting Thin
                    Button(onClick = { selectedFontFamily = handwritingThinFont }) {
                        Text("HandT")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 미리보기 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
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
                Row(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
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
                                            shareIntent,
                                            "이미지 공유"
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
        },
        dismissButton = {
            // 다이얼로그 닫기 버튼 (선택 사항)
            Button(onClick = onDismiss) {
                Text("취소")
            }
        },
        // 다이얼로그 하단에 공유하기, 저장하기 버튼을 별도의 Column 아래 배치
    )

    // 다이얼로그의 하단 액션 버튼 영역은 AlertDialog의 content 외 별도로 배치하는 방법도 있습니다.
    // 아래는 다이얼로그 외부에 별도의 Row를 배치하는 예시입니다.
}

// 갤러리에 Bitmap을 저장하는 함수 (MediaStore API 사용)
fun saveBitmapToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String = "generated_image"
): Uri? {
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$fileName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        // API 29 이상에서는 RELATIVE_PATH를 사용합니다.
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

// 임시 캐시 디렉토리에 Bitmap을 저장하고, FileProvider를 통해 Uri를 생성하는 함수 (공유용)
fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String = "temp_image.jpg"): Uri? {
    // 캐시 디렉토리의 "images" 폴더 생성
    val cachePath = File(context.cacheDir, "images")
    cachePath.mkdirs()
    val file = File(cachePath, fileName)
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
    }
    // FileProvider를 통해 content:// Uri를 생성 (AndroidManifest.xml에 provider 등록 필요)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}