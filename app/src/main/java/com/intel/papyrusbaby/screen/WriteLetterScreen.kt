@file:Suppress("DEPRECATION")

package com.intel.papyrusbaby.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.intel.papyrusbaby.R
import java.net.URLEncoder

@Composable
fun WriteLetterScreen(navController: NavController) {
    // 키보드 닫기 위한 focusManager
    val focusManager = LocalFocusManager.current


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
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
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
                    "글 종류"
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

                    // URL 인코딩 (전달 시 특수문자 문제 방지)
                    val encodedAuthor = URLEncoder.encode(authorInput, "UTF-8")
                    val encodedDocType = URLEncoder.encode(documentTypeInput, "UTF-8")
                    val encodedPrompt = URLEncoder.encode(promptInput, "UTF-8")


                    // 입력값 및 선택값 초기화
                    currentInput = ""
                    selectedWriters = emptyList()
                    selectedFormats = emptyList()
                    focusManager.clearFocus()

                    // WrittenLetterScreen으로 이동하며 인자 전달
                    navController.navigate("writtenLetter?writer=$encodedAuthor&documentType=$encodedDocType&prompt=$encodedPrompt")

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


@Preview(showBackground = true)
@Composable
fun WriteLetterScreenPreview() {
    WriteLetterScreen(navController = rememberNavController())
}