@file:Suppress("DEPRECATION")

package com.intel.papyrusbaby.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
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
import com.intel.papyrusbaby.firebase.Author
import com.intel.papyrusbaby.firebase.AuthorRepository
import com.intel.papyrusbaby.util.ThemeSelectionDialog
import java.net.URLEncoder

enum class ThemeType(val displayName: String) {
    Theme01("감사"),
    Theme02("결혼"),
    Theme03("출산"),
    Theme04("입학"),
    Theme05("합격"),
    Theme06("격려"),
    Theme07("실패"),
    Theme08("졸업"),
    Theme09("군입대"),
    Theme10("환갑"),
    Theme11("상견례"),
    Theme12("크리스마스"),
    Theme13("삼일절"),
    Theme14("광복절"),
    Theme15("개천절"),
    Theme16("부활절"),
    Theme17("개업")
}

@Composable
fun WriteLetterScreen(
    navController: NavController,
    writerParam: String = ""
) {
    // 키보드 닫기 위한 focusManager
    val focusManager = LocalFocusManager.current

    // Firestore에서 작가 목록 로드
    var authors by remember { mutableStateOf<List<Author>>(emptyList()) }
    var isAuthorsLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isAuthorsLoading = true
        val result = AuthorRepository.fetchAuthors()
        authors = result
        isAuthorsLoading = false
    }

    // Dropdown에 표시할 '작가 이름' (DB에 이미 한글로 저장되어 있다면 그대로 사용)
    val authorNames = authors.map { it.name }

    // 드롭다운 상태
    var selectedWriters by remember { mutableStateOf(listOf<String>()) }
    var selectedFormats by remember { mutableStateOf(listOf<String>()) }

    // NavArgument로 넘어온 writerParam이 있으면 초기 선택값에 반영
    LaunchedEffect(writerParam) {
        if (writerParam.isNotEmpty()) {
            selectedWriters = listOf(writerParam)
        }
    }

    // 입력 텍스트
    var currentInput by remember { mutableStateOf("") }

    // 테마 관련
    val allThemes = ThemeType.entries.toList()
    var selectedThemes by remember { mutableStateOf(listOf<ThemeType>()) }
    var showThemeSelectionDialog by remember { mutableStateOf(false) }

    if (showThemeSelectionDialog) {
        ThemeSelectionDialog(
            allThemes = allThemes,
            initiallySelected = selectedThemes,
            onDismiss = { showThemeSelectionDialog = false },
            onConfirm = { newSelection ->
                selectedThemes = newSelection
                showThemeSelectionDialog = false
            }
        )
    }

    // 화면 레이아웃
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAE6))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // 첫째 줄: 작가 / 글 종류 필터
        Row(modifier = Modifier.fillMaxWidth()) {
            // 작가 드롭다운
            ExpandableFilter(
                title = if (selectedWriters.isNotEmpty()) {
                    selectedWriters.joinToString(", ")
                } else {
                    "작가"
                },
                options = authorNames,
                selectedOptions = selectedWriters,
                onOptionSelected = { option, selected ->
                    selectedWriters = if (selected) {
                        listOf(option)
                    } else {
                        emptyList()
                    }
                },
                modifier = Modifier.weight(1f)
            )

            // 글 종류 드롭다운
            ExpandableFilter(
                title = if (selectedFormats.isNotEmpty()) {
                    selectedFormats.joinToString(", ")
                } else {
                    "글 종류"
                },
                options = listOf("일기", "편지", "장문", "단문", "엽서"),
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

        Spacer(modifier = Modifier.width(10.dp))

        // 둘째 줄: 테마 아이콘 + 테마 목록
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_filter),
                contentDescription = "themeFilter",
                tint = Color.Unspecified,
                modifier = Modifier.clickable { showThemeSelectionDialog = true }
            )
            // 선택된 테마를 표시(선택 토글은 팝업에서 함)
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                allThemes.forEach { theme ->
                    val isSelected = selectedThemes.contains(theme)
                    Box(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = Color(0xFF94907F),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .background(
                                color = if (isSelected) Color(0xFF5C5945) else Color.Transparent,
                                shape = RoundedCornerShape(5.dp)
                            )
                            .clickable {
                                // 선택/해제 토글
                                selectedThemes = if (isSelected) {
                                    selectedThemes - theme
                                } else {
                                    selectedThemes + theme
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = theme.displayName,
                            color = if (isSelected) Color(0xFFFFFAE6) else Color(0xFF5C5945),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // 프롬프트 입력 + 전송 버튼
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
                    // URL 인코딩
                    val authorInput = selectedWriters.joinToString(", ")
                    val documentTypeInput = selectedFormats.joinToString(", ")
                    val promptInput = currentInput

                    val encodedAuthor = URLEncoder.encode(authorInput, "UTF-8")
                    val encodedDocType = URLEncoder.encode(documentTypeInput, "UTF-8")
                    val encodedPrompt = URLEncoder.encode(promptInput, "UTF-8")

                    val themeStringList = selectedThemes.map { it.displayName }
                    val joinedTheme = themeStringList.joinToString(",")
                    val encodedTheme = URLEncoder.encode(joinedTheme, "UTF-8")

                    // Navigate to WrittenLetterScreen
                    navController.navigate(
                        "writtenLetter?writer=$encodedAuthor&documentType=$encodedDocType&prompt=$encodedPrompt&theme=$encodedTheme"
                    )

                    // 값 초기화
                    currentInput = ""
                    selectedWriters = emptyList()
                    selectedFormats = emptyList()
                    focusManager.clearFocus()
                }
            }) {
                Icon(
                    painter = painterResource(R.drawable.icon_writeletter),
                    tint = Color.Unspecified,
                    contentDescription = "전송"
                )
            }
        }
    }
}

// 간단한 커스텀 입력 필드
@Composable
fun CustomInputField(currentInput: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    BasicTextField(
        value = currentInput,
        onValueChange = onValueChange,
        modifier = modifier
            .background(Color(0xFFF7ECCD), shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        textStyle = TextStyle(
            color = Color(0xFF5C5945),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
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
        }
    )
}

// 드롭다운 필터
@Composable
fun ExpandableFilter(
    title: String = "Size",
    options: List<String>,
    selectedOptions: List<String>,
    onOptionSelected: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(vertical = 5.dp, horizontal = 10.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(width = 1.dp, color = Color(0xFF94907F), shape = RoundedCornerShape(20.dp))
    ) {
        // 필터 탭 헤더 (클릭 시 DropdownMenu 표시)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

        // DropdownMenu
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .background(Color(0xFFF0F0F0))
                .border(
                    width = 0.5.dp,
                    color = Color(0xFF777777),
                    shape = RoundedCornerShape(4.5.dp)
                )
        ) {
            // 최대 높이: 화면 높이의 50%
            val configuration = LocalConfiguration.current
            val maxDropdownHeight = configuration.screenHeightDp.dp * 0.5f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = maxDropdownHeight)
                    .verticalScroll(rememberScrollState())
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        onClick = {
                            val currentlySelected = selectedOptions.contains(option)
                            onOptionSelected(option, !currentlySelected)
                            expanded = false
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedOptions.contains(option),
                                    onCheckedChange = { checked ->
                                        onOptionSelected(option, checked)
                                        expanded = false
                                    }
                                )
                                Text(text = option)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WriteLetterScreenPreview() {
    WriteLetterScreen(navController = rememberNavController())
}
