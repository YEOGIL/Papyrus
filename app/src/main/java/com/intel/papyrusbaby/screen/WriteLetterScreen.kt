@file:Suppress("DEPRECATION")

package com.intel.papyrusbaby.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.intel.papyrusbaby.R
import com.intel.papyrusbaby.flask.OpenAiServer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WriteLetterScreen(navController: NavController) {
    // 키보드 닫기 위한 focusManager
    val focusManager = LocalFocusManager.current

    // 단일 프롬프트 정보를 저장하는 상태 변수
    var lastUserSelection by remember { mutableStateOf<UserSelect?>(null) }

    // 현재 입력 텍스트 상태 관리
    var currentInput by remember { mutableStateOf("") }

    // 옵션 선택 상태 관리
    var selectedWriters by remember { mutableStateOf(listOf<String>()) }
    var selectedFormats by remember { mutableStateOf(listOf<String>()) }

    // 서버 응답을 표시하기 위한 상태 변수
    var isLoading by remember { mutableStateOf(false) }
    var openAiResponse by remember { mutableStateOf("") }

    Column(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFFFFAE6))
        .pointerInput(Unit) {
            focusManager.clearFocus()
        }) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ExpandableFilter(
                title = if (selectedWriters.isNotEmpty()) {
                    selectedWriters.joinToString(", ")
                } else {
                    "작가"
                },
                options = listOf("윤동주", "김소월", "셰익스피어", "찰스 디킨스", "한강"),
                selectedOptions = selectedWriters,
                onOptionSelected = { option, selected ->
                    selectedWriters = if (selected) {
                        listOf(option) // 기존 선택 초기화 후 새 옵션만 추가
                    } else {
                        emptyList()    // 선택 해제 시 빈 리스트로
                    }
                },
                modifier = Modifier.weight(1f)
            )
            ExpandableFilter(
                title = if (selectedFormats.isNotEmpty()) {
                    selectedFormats.joinToString(", ")
                } else {
                    "편지 종류"
                },
                options = listOf("일기", "편지", "반성문", "단문", "엽서"),
                selectedOptions = selectedFormats,
                onOptionSelected = { option, selected ->
                    selectedFormats = if (selected) {
                        listOf(option)
                    } else {
                        emptyList()
                    }
                },
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        val docType = listOf(
            "결혼",
            "출산",
            "입학",
            "합격",
            "실패",
            "졸업",
            "군입대",
            "환갑",
            "상견례",
            "크리스마스",
            "삼일절",
            "광복절",
            "개천절",
            "부활절",
            "개업"
        )
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.size(10.dp))
            Icon(
                painter = painterResource(id = R.drawable.icon_filter),
                contentDescription = "themeFilter",
                tint = Color.Unspecified,
                modifier = Modifier.clickable {})
            Spacer(modifier = Modifier.size(10.dp))
            docType.forEach { writer ->
                Text(
                    text = writer,
                    color = Color(0xFF5C5945),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .border(
                            1.dp,
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFF94907F)
                        )
                        .clickable {}
                        .padding(horizontal = 10.dp, vertical = 5.dp))
                Spacer(modifier = Modifier.size(10.dp))
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
        Spacer(modifier = Modifier.size(20.dp))

        // 프롬프트 입력 영역
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CustomInputField(
                currentInput = currentInput,
                onValueChange = { currentInput = it },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = {
                if (currentInput.isNotBlank()) {
                    openAiResponse = ""
                    isLoading = true

                    // 입력값을 지역 변수에 저장
                    val authorInput = selectedWriters.joinToString(", ")
                    val documentTypeInput = selectedFormats.joinToString(", ")
                    val promptInput = currentInput

                    // UserSelect 인스턴스 생성
                    lastUserSelection = UserSelect(
                        writer = authorInput,
                        documentType = documentTypeInput,
                        prompt = promptInput
                    )

                    // 서버로 전송
                    OpenAiServer.sendRequestToServer(
                        author = authorInput,
                        documentType = documentTypeInput,
                        scenario = promptInput
                    ) { result, error ->
                        isLoading = false
                        if (error != null) {
                            // 에러 처리
                            // openAiResponse = "에러 발생: $error"
                            Log.d("openAiResponse", "에러 발생: $error")
                        } else {
                            // result를 UI에 표시하기 위해 상태 변수에 저장
                            openAiResponse = result ?: ""
                        }
                    }

                    // 입력값 및 선택값 초기화
                    currentInput = ""
                    selectedWriters = emptyList()
                    selectedFormats = emptyList()
                    focusManager.clearFocus()
                }
            }
            ) {
                Icon(
                    painter = painterResource(R.drawable.icon_writeletter),
                    tint = Color.Unspecified,
                    contentDescription = "전송"
                )
            }
        }
        // 출력 영역: UserSelect에 담긴 값들을 출력
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            lastUserSelection?.let { selection ->
                val currentDate = remember {
                    SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault()).format(Date())
                }
                Text(
                    text = "작성일 : $currentDate",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "작가: ${selection.writer}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "글 형식: ${selection.documentType}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Text(
                    text = "상세 내용: ${selection.prompt}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            if (isLoading) {
                // TODO: 서버 응답까지 로딩 중임을 표시하는 UI
                Box(modifier = Modifier.fillMaxSize(0.6f)) {
                    LoadingAnimation()
                }
            } else {
                Text(
                    text = openAiResponse,
                    fontSize = 16.sp,
                    color = Color(0xFF221F10),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color(0xFFF7ECCD), shape = RoundedCornerShape(20.dp))
                        .padding(20.dp)
                )
            }
        }
    }
}

// 사용자 선택 정보를 담을 data class
data class UserSelect(
    val writer: String, val documentType: String, val prompt: String
)

@Composable
fun CustomInputField(currentInput: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    BasicTextField(value = currentInput,
        onValueChange = onValueChange,
        modifier = modifier
            .background(Color(0xFFF7ECCD), shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        textStyle = TextStyle(
            color = Color(0xFF5C5945), fontSize = 18.sp, fontWeight = FontWeight.Bold
        ),
        decorationBox = { innerTextField ->
            if (currentInput.isEmpty()) {
                Text(
                    text = "상세 내용을 입력하세요.",
                    color = Color(0xFF5C5945),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            innerTextField()
        })
}

@Composable
fun ExpandableFilter(
    title: String = "Size",
    options: List<String>,
    selectedOptions: List<String>,
    onOptionSelected: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth() // 기본값은 fillMaxWidth()
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = Color(0xFF94907F), shape = RoundedCornerShape(20.dp))
    ) {
        // 필터 탭 헤더: 클릭 시 DropdownMenu를 표시합니다.
        Row(modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
//            .background(Color(0xFFFFFFFF))
            .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 16.sp,
                color = Color(0xFF5C5945),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color(0xFF5C5945)
            )
        }

        // DropdownMenu를 사용하여 필터 옵션들을 오버레이로 표시합니다.
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp, topEnd = 0.dp, bottomEnd = 10.dp, bottomStart = 10.dp
                    )
                )
                .background(Color(0xFFF0F0F0))
                .border(
                    width = 0.5.dp, color = Color(0xFF777777), shape = RoundedCornerShape(
                        bottomEnd = 10.dp, bottomStart = 10.dp
                    )
                )
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    val currentlySelected = selectedOptions.contains(option)
                    onOptionSelected(option, !currentlySelected)
                    expanded = false
                }, text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = selectedOptions.contains(option),
                            onCheckedChange = { checked ->
                                onOptionSelected(option, checked)
                                expanded = false
                            })
                        Text(
                            text = option, modifier = Modifier.padding(start = 5.dp)
                        )
                    }
                })
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

@Preview(showBackground = true)
@Composable
fun WriteLetterScreenPreview() {
    WriteLetterScreen(navController = rememberNavController())
}