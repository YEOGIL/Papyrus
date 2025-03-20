package com.intel.papyrusbaby.util

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface

@Composable
fun DeleteConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF7ECCD),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("삭제 확인", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "이 항목을 삭제하시겠습니까?\n삭제된 항목은 복구할 수 없습니다.",
                    fontSize = 16.sp,
                    color = Color(0xFF5C5945)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // 취소 버튼 (테두리만 있는 스타일)
                    Box(
                        modifier = Modifier
                            .border(BorderStroke(1.dp, Color(0xFF5C5945)), shape = RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                    ) {
                        Text(
                            text = "취소",
                            color = Color(0xFF5C5945),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    // 확인 버튼 (채워진 스타일)
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF5C5945), shape = RoundedCornerShape(8.dp))
                            .clickable {
                                onDismiss()
                                onConfirm()
                            }
                    ) {
                        Text(
                            text = "확인",
                            color = Color(0xFFFFFAE6),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
