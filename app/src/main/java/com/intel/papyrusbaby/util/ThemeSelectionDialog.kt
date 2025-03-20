package com.intel.papyrusbaby.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.intel.papyrusbaby.screen.ThemeType

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeSelectionDialog(
    allThemes: List<ThemeType>,
    initiallySelected: List<ThemeType>,
    onDismiss: () -> Unit,
    onConfirm: (List<ThemeType>) -> Unit
) {
    // 내부에서 체크박스로 선택한 항목을 저장할 임시 상태
    var tempSelected by remember { mutableStateOf(initiallySelected) }

    // Dialog(또는 AlertDialog) 활용 (백버튼 등으로 닫힐 때도 onConfirm 호출)
    Dialog(onDismissRequest = { onConfirm(tempSelected) }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF7ECCD),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("테마 선택", fontWeight = FontWeight.Bold, fontSize = 20.sp)

                Spacer(modifier = Modifier.height(16.dp))

                // ThemeType 목록을 FlowRow로 표시 (한 줄에 여러 항목 배치, 최대 4개씩)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 4,
                ) {
                    allThemes.forEach { theme ->
                        val isSelected = theme in tempSelected

                        Box(
                            modifier = Modifier
                                .border(
                                    1.dp,
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFF5C5945)
                                )
                                .background(
                                    color = if (isSelected) Color(0xFF5C5945) else Color.Transparent,
                                    shape = RoundedCornerShape(5.dp)
                                )
                                .clickable {
                                    // 테마 항목 선택/해제시 자동으로 업데이트
                                    tempSelected = if (isSelected) {
                                        tempSelected - theme
                                    } else {
                                        tempSelected + theme
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = theme.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFFFFFAE6) else Color(0xFF5C5945)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 하단 버튼 영역: "초기화"와 "닫기" 버튼
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF5C5945),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { tempSelected = emptyList() }
                    ) {
                        Text(
                            text = "초기화",
                            color = Color(0xFFFFFAE6),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF5C5945),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onConfirm(tempSelected) }
                    ) {
                        Text(
                            text = "닫기",
                            color = Color(0xFFFFFAE6),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
