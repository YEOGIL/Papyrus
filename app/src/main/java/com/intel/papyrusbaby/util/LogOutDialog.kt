package com.intel.papyrusbaby.util

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun LogOutDialog(
    onDismiss: () -> Unit,
    onLogOut: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("계정 로그 아웃") },
        text = { Text("로그 아웃 하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onLogOut()
            }) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismiss() }) {
                Text("취소")
            }
        }
    )
}