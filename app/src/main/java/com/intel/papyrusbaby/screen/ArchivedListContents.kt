package com.intel.papyrusbaby.screen

import android.content.Intent
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intel.papyrusbaby.firebase.ArchiveItem
import com.intel.papyrusbaby.firebase.ArchiveRepository
import kotlinx.coroutines.launch

@Composable
fun ArchivedListContentsScreen(
    docId: String,
    navController: NavController
) {
    // 로컬 컨텍스트, 코루틴 스코프
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Firestore에서 받아올 ArchiveItem
    var archiveItem by remember { mutableStateOf<ArchiveItem?>(null) }
    // 로딩 상태
    var isLoading by remember { mutableStateOf(true) }

    // 화면 최초 진입 시 Firestore에서 문서 조회
    LaunchedEffect(docId) {
        scope.launch {
            val item = ArchiveRepository.getArchiveItem(docId)
            archiveItem = item
            isLoading = false
        }
    }

    // 실제 레터 내용 표시
    if (archiveItem == null) {
        // 문서 조회 실패 또는 없는 경우
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFAE6)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "아카이브 데이터를 불러올 수 없습니다.")
        }
    } else {
        // WrittenLetterScreen의 UI 레이아웃을 재사용하여 표시
        ShowArchivedLetterContents(archiveItem = archiveItem!!, navController = navController)
    }
}


// 아카이브된 Letter 내용 표시 UI
@Composable
fun ShowArchivedLetterContents(
    archiveItem: ArchiveItem,
    navController: NavController
) {
    // 복사 / 공유 / 토스트 등에 사용
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // 화면 구성 (WrittenLetterScreen 참고)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFAE6))
            .padding(16.dp)
    ) {

        // 1) 메타 정보 (작성일, 작가 등)
        Box(){
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "작성일: ${archiveItem.writtenDate}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "작가: ${archiveItem.author.ifEmpty { "무명 작가" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "글 형식: ${archiveItem.docType.ifEmpty { "단문" }}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
                Text(
                    text = "상세 내용: ${archiveItem.detail}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B1818)
                )
            }

            Text(
                text = "뒤로가기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 목록 화면으로 돌아가기
                        navController.popBackStack()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 2) 생성된 텍스트 영역
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color(0xFFF7ECCD), shape = RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            // 여기선 별도 아이콘(아카이브 아이콘) 클릭 기능은 굳이 필요 없다면 제거
            // (이미 저장된 상태)
            Text(
                text = archiveItem.generatedText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF221F10),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 3) “보내기”, “복사하기”, “나가기” 같은 버튼들
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "보내기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 공유 인텐트 생성
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, archiveItem.generatedText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            Text(
                text = "복사하기",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        clipboardManager.setText(AnnotatedString(archiveItem.generatedText))
                        Toast
                            .makeText(context, "복사되었습니다.", Toast.LENGTH_SHORT)
                            .show()
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )

            Text(
                text = "이미지 생성",
                color = Color(0xFF5C5945),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF94907F)
                    )
                    .clickable {
                        // 이미지 생성 화면
                    }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}
