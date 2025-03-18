package com.intel.papyrusbaby.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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

@Composable
fun HomeScreen(navController: NavController) {
    val focusManager = LocalFocusManager.current

    // 코루틴 스코프
    val coroutineScope = rememberCoroutineScope()

    // 작가 리스트를 저장할 상태
    var authors by remember { mutableStateOf<List<Author>>(emptyList()) }

    // HomeScreen이 구성될 때 한 번만 Firestore에서 데이터를 가져옴
    LaunchedEffect(Unit) {
        authors = AuthorRepository.fetchAuthors()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFfffae6))
            .pointerInput(Unit) {
                // 포커스 해제
                focusManager.clearFocus()
            },
    ) {
        Spacer(modifier = Modifier.size(30.dp))
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
        val writerType = listOf("작가", "대통령", "재외동포 박현진", "시인", "철학자", "정치인", "과학자", "가수", "교장선생님")
        Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
            Spacer(modifier = Modifier.size(10.dp))
            writerType.forEach { writer ->
                Text(
                    text = writer,
                    color = Color(0xFF5C5945),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .border(
                            1.dp,
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFF94907F)
                        )
                        .clickable {}
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
                Spacer(modifier = Modifier.size(10.dp))
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
        // Row with horizontal scrolling and spacing between PersonBox
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(30.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 불러온 작가 리스트를 순회하며 Box를 생성
            authors.forEach { author ->
                AuthorBox(author = author, navController = navController)
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
            .clickable {
                // 작가 정보 다이얼로그 표시
                showInfoDialog = true
            },
        contentAlignment = Alignment.Center
    ) {
        // 배경 이미지(기존 personbox 배경)
        Image(
            painter = painterResource(id = R.drawable.personbox),
            contentDescription = "personBox background",
            modifier = Modifier.offset(y = (-5).dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
        ) {
            // 1) 네트워크 이미지 로딩 (Coil)
            AsyncImage(
                model = author.imageUrl,
                contentDescription = "Author Image",
                modifier = Modifier.height(100.dp)
            )


            // 2) 이름
            Text(
                text = author.name,
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
