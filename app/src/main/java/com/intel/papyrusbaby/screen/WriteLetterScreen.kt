package com.intel.papyrusbaby.screen

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intel.papyrusbaby.AppBar

@Composable
fun WriteLetterScreen(navController: NavController) {
    AppBar(content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFAF3))
                .padding(paddingValues)
        ) {
            // 메시지 리스트 상태 관리
            val messages = remember { mutableStateListOf<UserPrompt>() }
            val style = remember { mutableStateListOf<SelectedOptions>() }

            // 현재 입력 텍스트 상태 관리
            var currentInput by remember { mutableStateOf("") }

            var selectedWriters by remember { mutableStateOf(listOf<String>()) }
            var selectedFormats by remember { mutableStateOf(listOf<String>()) }

            //키보드 닫기 위한 focusManager
            val focusManager = LocalFocusManager.current

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
                            emptyList()    // 선택 해제하면 빈 리스트로
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                ExpandableFilter(
                    title = if (selectedFormats.isNotEmpty()) {
                        selectedFormats.joinToString(", ")
                    } else {
                        "글 형식"
                    },
                    options = listOf("일기", "편지", "반성문", "단문", "엽서"),
                    selectedOptions = selectedFormats,
                    onOptionSelected = { option, selected ->
                        selectedFormats = if (selected) {
                            listOf(option) // 기존 선택 초기화 후 새 옵션만 추가
                        } else {
                            emptyList()    // 선택 해제하면 빈 리스트로
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            // 프롬프트 입력 영역
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFFFFFFF))
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomInputField(
                    currentInput = currentInput,
                    onValueChange = { currentInput = it },
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        if (currentInput.isNotBlank()) {
                            // 사용자의 메시지 추가
                            messages.add(UserPrompt(text = currentInput, isUser = true))
                            // TODO: 서버에 currentInput 전송 후 챗봇 응답 처리
                            currentInput = ""
                            style.add(
                                SelectedOptions(
                                    writer = selectedWriters.joinToString(", "),
                                    type = selectedFormats.joinToString(", ")
                                )
                            )
                            selectedWriters = emptyList()
                            selectedFormats = emptyList()
                            focusManager.clearFocus()
                        }
                    }
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "전송")
                }
            }
            // 메시지 리스트 영역
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                itemsIndexed(messages) { index, message ->
                    ChatBubble(message = message)
                    // 사용자의 메시지에 대한 스타일 정보가 있다면 출력
                    if (index < style.size) {
                        val selectedOption = style[index]
                        Text(
                            text = "작가: ${selectedOption.writer}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B1818),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "글 형식: ${selectedOption.type}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B1818),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }        }
    }, navController = navController)
}


@Composable
fun ChatBubble(message: UserPrompt) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEBFFFE))
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = Color(0xFF1B1818),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun CustomInputField(currentInput: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    BasicTextField(
        value = currentInput,
        onValueChange = onValueChange,
        modifier = modifier
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        textStyle = TextStyle(
            color = Color(0xFF1B1818),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        ),
        decorationBox = { innerTextField ->
            if (currentInput.isEmpty()) {
                Text(
                    text = "상세 내용을 입력하세요",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            innerTextField()
        }
    )
}

// Selected Style 클래스
data class SelectedOptions(
    val writer: String,
    val type: String
)

// 프롬프트 클래스
data class UserPrompt(
    val text: String,
    val isUser: Boolean
)

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
            .clip(RoundedCornerShape(10.dp))
            .border(width = 1.dp, color = Color(0xFFA4A4A4), shape = RoundedCornerShape(10.dp))
    ) {
        // 필터 탭 헤더: 클릭 시 DropdownMenu를 표시합니다.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .background(Color(0xFFFFFFFF))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = Color(0xFFA4A4A4)
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
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomEnd = 10.dp,
                        bottomStart = 10.dp
                    )
                )
                .background(Color(0xFFF0F0F0))
                .border(
                    width = 0.5.dp,
                    color = Color(0xFF777777),
                    shape = RoundedCornerShape(
                        bottomEnd = 10.dp,
                        bottomStart = 10.dp
                    )
                )
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        val currentlySelected = selectedOptions.contains(option)
                        onOptionSelected(option, !currentlySelected)
                        expanded = false
                    },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedOptions.contains(option),
                                onCheckedChange = { checked ->
                                    onOptionSelected(option, checked)
                                    expanded = false
                                }
                            )
                            Text(
                                text = option,
                                modifier = Modifier.padding(start = 5.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}
