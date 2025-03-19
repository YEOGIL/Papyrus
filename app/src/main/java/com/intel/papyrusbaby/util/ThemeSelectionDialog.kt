package com.intel.papyrusbaby.util

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.intel.papyrusbaby.screen.ThemeType

@Composable
fun ThemeSelectionDialog(
    allThemes: List<ThemeType>,
    initiallySelected: List<ThemeType>,
    onDismiss: () -> Unit,
    onConfirm: (List<ThemeType>) -> Unit
) {
    // 내부에서 체크박스로 선택한 항목을 저장할 임시 상태
    var tempSelected by remember { mutableStateOf(initiallySelected) }

    // Dialog(또는 AlertDialog) 활용
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, shape = RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("테마 선택", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Spacer(modifier = Modifier.height(10.dp))

                // ThemeType 목록을 체크박스로 표시
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    allThemes.forEach { theme ->
                        val isChecked = theme in tempSelected
                        Row(
                            modifier = Modifier
                                .clickable {
                                    // 체크/해제 토글
                                    tempSelected = if (isChecked) {
                                        tempSelected - theme
                                    } else {
                                        tempSelected + theme
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    tempSelected = if (checked) {
                                        tempSelected + theme
                                    } else {
                                        tempSelected - theme
                                    }
                                }
                            )
                            Text(theme.displayName)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 확인 & 취소 버튼
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.1f)
                ) {
                    Text(
                        text = "취소",
                        modifier = Modifier
                            .clickable { onDismiss() }
                            .padding(8.dp)
                    )
                    Text(
                        text = "확인",
                        modifier = Modifier
                            .clickable {
                                onConfirm(tempSelected)
                            }
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}
