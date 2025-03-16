package com.intel.papyrusbaby.screen

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
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
import com.intel.papyrusbaby.flask.OpenAiServer
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WrittenLetterScreen(
    writer: String,
    documentType: String,
    prompt: String,
    navController: NavController
) {
    var isLoading by remember { mutableStateOf(true) }
    var openAiResponse by remember { mutableStateOf("") }

    // 현재 날짜 생성 (작성일)
    val currentDate = remember {
        SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date())
    }
// 전달받은 prompt를 디코딩
    val decodedWriter = URLDecoder.decode(writer, "UTF-8")
    val decodedDocumentType = URLDecoder.decode(documentType, "UTF-8")
    val decodedPrompt = URLDecoder.decode(prompt, "UTF-8")

    // 서버 요청: 화면이 시작될 때 실행
    LaunchedEffect(Unit) {
        OpenAiServer.sendRequestToServer(
            author = decodedWriter,
            documentType = decodedDocumentType,
            scenario = decodedPrompt
        ) { result, error ->
            isLoading = false
            openAiResponse = result ?: "응답 없음"
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
            text = "작가: $decodedWriter",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF1B1818)
        )
        Text(
            text = "글 형식: $decodedDocumentType",
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
                Icon(
                    painter = painterResource(
                        R.drawable.icon_archive_outline
                    ),
                    tint = Color.Unspecified,
                    contentDescription = "ArchivedLetters",
                    modifier = Modifier
                        .align(alignment = Alignment.End)
                        .height(20.dp)
                        .clickable {
                        }
                )

                Text(
                    text = openAiResponse,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF221F10),
                )
            }
        }
        val clipboardManager = LocalClipboardManager.current
        val context = LocalContext.current
        Row() {
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
                    .padding(horizontal = 10.dp, vertical = 5.dp))
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