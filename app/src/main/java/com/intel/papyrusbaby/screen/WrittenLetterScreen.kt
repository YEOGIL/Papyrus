@file:Suppress("DEPRECATION")

package com.intel.papyrusbaby.screen

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.intel.papyrusbaby.R
import com.intel.papyrusbaby.firebase.ArchiveItem
import com.intel.papyrusbaby.firebase.ArchiveRepository
import com.intel.papyrusbaby.flask.OpenAiServer
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WrittenLetterScreen(
    writer: String,
    documentType: String,
    prompt: String,
    theme: String?,
    navController: NavController
) {
    // 코루틴 스코프 생성
    val scope = rememberCoroutineScope()

    // 로컬 컨텍스트
    val context = LocalContext.current

    // 로딩 상태와 결과 상태를 저장하는 변수
    var isLoading by remember { mutableStateOf(true) }
    var isFinished by remember { mutableStateOf(false) }

    // 정상적으로 생성된 답변에 대해서만 처리하는 변수
    var generationSuccessful by remember { mutableStateOf(false) }

    // 아카이브 성공 여부를 저장하는 변수
    var archivingSuccessful by remember { mutableStateOf(false) }

    // 현재 날짜 생성 (작성일)
    val currentDate = remember {
        SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date())
    }

    // 전달받은 prompt를 디코딩
    val decodedWriter = URLDecoder.decode(writer, "UTF-8")
    val decodedDocumentType = URLDecoder.decode(documentType, "UTF-8")
    val decodedPrompt = URLDecoder.decode(prompt, "UTF-8")
    val decodedThemeList = theme?.let {
        val rawStr = URLDecoder.decode(it, "UTF-8") // "결혼,입학,합격"
        rawStr.split(",")                          // ["결혼","입학","합격"]
    } ?: emptyList()

    // 서버 응답을 저장하는 변수
    var openAiResponse by remember { mutableStateOf("") }

    // 서버 요청: 화면이 시작될 때 실행
    LaunchedEffect(Unit) {
        OpenAiServer.sendRequestToServer(
            author = decodedWriter,
            documentType = decodedDocumentType,
            themeType = decodedThemeList,
            scenario = decodedPrompt
        ) { serverResponse, error ->
            isLoading = false
            isFinished = true
            openAiResponse = serverResponse?.result ?: "응답 없음"
            generationSuccessful = serverResponse?.isSuccessful ?: false
        }
    }

    // 화면 구성
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAE6))
            .padding(16.dp)
    ) {
        Text(
            text = "작성일: $currentDate",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1818)
        )
        Text(
            text = "작가: ${decodedWriter.ifEmpty { "무명 작가" }}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1818)
        )
        Text(
            text = "글 형식: ${decodedDocumentType.ifEmpty { "단문" }}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1818)
        )
        Text(
            text = "상세 내용: $decodedPrompt",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1818)
        )
        Spacer(modifier = Modifier.height(20.dp))

        if (isLoading) {
            // 로딩 애니메이션 표시
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                LoadingAnimation()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7ECCD), shape = RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                if (generationSuccessful) {
                    Icon(
                        painter = painterResource(
                            if (archivingSuccessful) R.drawable.icon_archive_filled else R.drawable.icon_archive_outline
                        ),
                        tint = Color.Unspecified,
                        contentDescription = "ArchivedLetters",
                        modifier = Modifier
                            .align(alignment = Alignment.End)
                            .height(20.dp)
                            .clickable(enabled = !archivingSuccessful) {
                                // 아카이브 저장
                                val archiveItem = ArchiveItem(
                                    writtenDate = currentDate,
                                    author = decodedWriter.ifEmpty { "무명 작가" },
                                    docType = decodedDocumentType.ifEmpty { "단문" },
                                    detail = decodedPrompt,
                                    generatedText = openAiResponse
                                )

                                // 코루틴 처리
                                scope.launch {
                                    try {
                                        ArchiveRepository.addArchiveItem(archiveItem)
                                        // 저장 성공 시 처리
                                        archivingSuccessful = true
                                        // 토스트 메세지 출력
                                        Toast
                                            .makeText(context, "보관함에 저장 되었습니다!", Toast.LENGTH_SHORT)
                                            .show()

                                    } catch (e: Exception) {
                                        // 실패 처리
                                        Log.e("Archive", "DB Save Error: ${e.localizedMessage}", e)
                                    }
                                }
                            }
                    )
                }

                Text(
                    text = openAiResponse,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF221F10),
                    modifier = Modifier
                        .fillMaxHeight(0.5f)
                        .verticalScroll(rememberScrollState())
                )
            }
        }
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        if (isFinished) {
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (generationSuccessful) {
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
                                    putExtra(Intent.EXTRA_TEXT, openAiResponse) // 전송할 텍스트
                                    type = "text/plain"
                                }
                                // 앱 선택 창 띄우기
                                val shareIntent = Intent.createChooser(sendIntent, null)
                                context.startActivity(shareIntent)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp))
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
                                clipboardManager.setText(AnnotatedString(openAiResponse))
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = "다시 작성하기",
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
                            navController.popBackStack()
                        }
                        .padding(horizontal = 10.dp, vertical = 5.dp))
            }

            if (generationSuccessful) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    FontSelectionScreen(openAiResponse)
                }
            }
        }
    }
}

@Composable
fun LoadingAnimation() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.animation_writing))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    LottieAnimation(
        composition = composition,
        progress = progress
    )
}

@Composable
fun FontSelectionScreen(openAiResponse: String) {
    // 기본 폰트와 커스텀 폰트 정의
    val defaultFont = FontFamily.Default
    val boldAndClearFont = FontFamily(Font(R.font.boldandclear))
    val cuteFont = FontFamily(Font(R.font.cute))
    val handwritingFont = FontFamily(Font(R.font.handwriting))
    val handwritingThinFont = FontFamily(Font(R.font.handwritingthin))

    // 선택된 폰트를 상태로 관리 (초기값은 기본 폰트)
    var selectedFontFamily by remember { mutableStateOf<FontFamily>(FontFamily.Default) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // 폰트 선택 버튼 Row
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

        // 선택한 폰트로 openAiResponse 출력
        Text(
            text = openAiResponse,
            fontSize = 16.sp,
            fontFamily = selectedFontFamily,
            color = Color(0xFF221F10)
        )
    }
}