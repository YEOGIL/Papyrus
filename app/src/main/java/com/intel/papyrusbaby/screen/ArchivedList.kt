package com.intel.papyrusbaby.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intel.papyrusbaby.firebase.ArchiveItem
import com.intel.papyrusbaby.firebase.ArchiveRepository
import com.intel.papyrusbaby.navigation.Screen
import com.intel.papyrusbaby.util.DeleteConfirmDialog
import kotlinx.coroutines.launch

@Composable
fun ArchivedListScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    var archiveList by remember { mutableStateOf<List<ArchiveItem>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            // Firestore에서 아카이브 목록 불러오기
            archiveList = ArchiveRepository.getAllArchives()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAE6))
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        if (archiveList.isEmpty()) {
            // 데이터가 없을 때 표시할 영역
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "보관함이 비어있습니다.\n당신의 이야기를 채워보세요.",
                    color = Color(0xFF94907F),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            // 데이터가 있을 때 LazyColumn으로 표시
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                items(archiveList) { item ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(8.dp)
                            .background(color = Color(0xFF94907F), shape = RoundedCornerShape(8.dp))
                            .clickable {
                                navController.navigate(Screen.ArchiveDetail.createRoute(item.docId)) {
                                    popUpTo(Screen.ArchiveDetail.route) { inclusive = true }
                                    launchSingleTop = true
                                }

                            }
                            .padding(16.dp)
                    ) {
                        // 삭제 아이콘 버튼: 우측 상단에 배치
                        IconButton(
                            onClick = {
                                // 삭제 다이얼로그를 띄우기 위해 선택된 아이템의 docId 저장
                                selectedItemId = item.docId
                                showDeleteDialog = true
                            },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = "삭제",
                                tint = Color.White,
                            )
                        }

                        // 아카이브 항목 내용
                        Column {
                            Text(
                                text = "작성일: ${item.writtenDate}",
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "작가: ${item.author.ifEmpty { "선택 없음" }}",
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "글 형식: ${item.docType.ifEmpty { "선택 없음" }}",
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            if (item.themeList.isNotEmpty()) {
                                Text(
                                    "테마: ${item.themeList.joinToString(", ").ifEmpty { "선택 없음" }}",
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            Text(
                                text = "주제: ${item.detail}",
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // 삭제 확인 다이얼로그 표시
        if (showDeleteDialog && selectedItemId != null) {
            DeleteConfirmDialog(
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    scope.launch {
                        ArchiveRepository.deleteArchiveItem(selectedItemId!!)
                        archiveList = ArchiveRepository.getAllArchives()
                    }
                    showDeleteDialog = false
                }
            )
        }
    }
}
