package com.intel.papyrusbaby.screen

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.intel.papyrusbaby.R
import com.intel.papyrusbaby.firebase.Author
import com.intel.papyrusbaby.firebase.AuthorRepository
import com.intel.papyrusbaby.util.AuthorInfoDialog
import com.intel.papyrusbaby.util.ExitDialog
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val activity = context as? Activity

    // 뒤로가기 다이얼로그
    var showExitDialog by remember { mutableStateOf(false) }
    BackHandler { showExitDialog = true }
    if (showExitDialog) {
        ExitDialog(
            onDismiss = { showExitDialog = false },
            activity = activity
        )
    }

    // 포커스 매니저
    val focusManager = LocalFocusManager.current

    // 로딩 상태를 나타낼 변수
    var isLoading by remember { mutableStateOf(true) }

    // 작가 리스트를 저장할 상태
    var authors by remember { mutableStateOf<List<Author>>(emptyList()) }

    // 현재 선택된 직업 (없으면 null)
    var selectedOccupation by remember { mutableStateOf<String?>(null) }

    // 서버에서 데이터를 가져올 때 코루틴 사용
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            // 로딩 시작
            isLoading = true

            // Firestore에서 가져오기
            val result = AuthorRepository.fetchAuthors()
            authors = result

            // 로딩 끝
            isLoading = false
        }
    }

    // authors가 갱신될 때마다 모든 직업을 집계
    // flatMap -> 중첩 리스트를 1차원으로 펼친 뒤, distinct()로 중복 제거
    val allOccupations = remember(authors) {
        authors.flatMap { it.occupation }.distinct()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFfffae6))
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            },
    ) {
        Spacer(modifier = Modifier.size(30.dp))

        // 상단 로고
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_quotationmark),
                contentDescription = "papyrusLogo",
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.size(30.dp))
            Text(
                text = "편지를 교환하지 않는\n" +
                        "사람들은\n" + "서로에 대해 모른다.",
                color = Color(0xFF5C5945),
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.size(30.dp))
            Text(
                text = "콘스탄틴 로디브\n" +
                        "(러시아의 작가/철학자)",
                color = Color(0xFF5C5945),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.align(Alignment.End)
            )
        }

        Spacer(modifier = Modifier.size(70.dp))

        // (1) 모든 직업을 상단에 표시하는 탭
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.size(10.dp))
            // "전체" 탭을 하나 추가하고, 누르면 selectedOccupation을 null로 (즉, 필터 해제)
            Box(
                modifier = Modifier
                    .border(
                        1.dp,
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFF5C5945)
                    )
                    .background(
                        color = if (selectedOccupation == null) Color(0xFF5C5945) else Color.Transparent,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .clickable { selectedOccupation = null }
            ) {
                Text(
                    text = "전체",
                    color = if (selectedOccupation == null) Color(0xFFFFFAE6) else Color(0xFF5C5945),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
            Spacer(modifier = Modifier.size(10.dp))

            // allOccupations를 순회하며 버튼 생성
            allOccupations.forEach { occupation ->
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFF5C5945)
                        )
                        .background(
                            color = if (selectedOccupation == occupation) Color(0xFF5C5945) else Color.Transparent,
                            shape = RoundedCornerShape(5.dp)
                        )
                        .clickable {
                            selectedOccupation = occupation
                        }
                ) {
                    Text(
                        text = occupation,
                        color = if (selectedOccupation == occupation) Color(0xFFFFFAE6) else Color(
                            0xFF5C5945
                        ),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                Spacer(modifier = Modifier.size(10.dp))
            }
            Spacer(modifier = Modifier.size(10.dp))
        }

        // (2) 리스트 표시
        when {
            // (A) 로딩중
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("데이터를 불러오는 중입니다...", fontSize = 18.sp, color = Color(0xFF5C5945))
                }
            }

            // (B) 로딩은 끝났는데, authors가 비어있으면 안내
            authors.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("불러올 데이터가 없습니다.")
                }
            }

            // (C) 정상적으로 데이터가 있는 경우
            else -> {
                // selectedOccupation이 null이면 전체, 아니면 필터
                val filteredAuthors = if (selectedOccupation != null) {
                    authors.filter { it.occupation.contains(selectedOccupation) }
                } else {
                    authors
                }

                // Row로 감싸서 가로 스크롤 형태로 작가 목록 표시
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(30.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    filteredAuthors.forEach { author ->
                        AuthorBox(author = author, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun AuthorBox(author: Author, navController: NavController) {
    var showInfoDialog by remember { mutableStateOf(false) }
    if (showInfoDialog) {
        AuthorInfoDialog(
            author = author,
            navController = navController,
            onDismiss = { showInfoDialog = false }
        )
    }

    Box(
        modifier = Modifier
            .size(width = 180.dp, height = 240.dp)
            .background(color = Color(0xFFF7ECCD), shape = RoundedCornerShape(20.dp))
            .clickable {
                // 작가 정보 다이얼로그 표시
                showInfoDialog = true
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(8.dp)
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            // 1) 네트워크 이미지 로딩 (Coil)
            AsyncImage(
                model = author.imageUrl,
                contentDescription = "Author Image",
                modifier = Modifier.height(100.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 2) 이름
            Text(
                text = author.name,
                fontWeight = FontWeight.Bold
            )

            // 3) 직업 (occupation)
            // 예: ["작가", "시인"] -> "작가 시인"
            val occupationText = author.occupation.joinToString(" ")
            if (occupationText.isNotEmpty()) {
                Text(
                    text = occupationText,
                )
            }

            // 4) 장르를 해시태그 형식으로 표시
            // 예: ["철학", "담담한"] -> "#철학 #담담한"
            val genreText = author.genres.joinToString(" ") { "#$it" }
            if (genreText.isNotEmpty()) {
                Text(
                    text = genreText,
                )
            }
        }
    }
}
