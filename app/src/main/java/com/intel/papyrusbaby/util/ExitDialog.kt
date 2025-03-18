package com.intel.papyrusbaby.util

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ExitDialog(
    onDismiss: () -> Unit,
    activity: Activity?
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("앱 종료") },
        text = { Text("앱을 종료하시겠습니까?") },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                activity?.finish()
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