package com.intel.papyrusbaby.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.intel.papyrusbaby.firebase.Author
import com.intel.papyrusbaby.navigation.Screen

@Composable
fun AuthorInfoDialog(
    author: Author,
    navController: NavController,
    onDismiss: () -> Unit
) {
    val fontColor = Color(0xFF221F10)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF7ECCD),
            modifier = Modifier.fillMaxWidth()
        ) {
            // Column 전체에 padding 적용하고, 필요시 스크롤 가능하도록 함.
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                // 상단: 닫기 아이콘 (우측 정렬)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFFFFAE6),
                        modifier = Modifier
                            .background(color = Color(0xFF5C5945), shape = RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                            .padding(4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1) 이미지 (상단 중앙)
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = author.imageUrl,
                        contentDescription = "Author Image",
                        modifier = Modifier.size(150.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2) 작가명과 직업 (한 줄에 배치)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = author.name,
                        fontWeight = FontWeight.ExtraBold,
                        color = fontColor,
                        textAlign = TextAlign.Start
                    )
                    val occupationText = author.occupation.joinToString(" ")
                    if (occupationText.isNotEmpty()) {
                        Text(
                            text = occupationText,
                            fontWeight = FontWeight.Bold,
                            color = fontColor,
                            textAlign = TextAlign.End
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3) 대표작
                val worksText = author.works.joinToString("  ")
                if (worksText.isNotEmpty()) {
                    Text(
                        text = worksText,
                        fontWeight = FontWeight.Bold,
                        color = fontColor,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 4) 명언 (첫 번째만 표시)
                if (author.quotes.isNotEmpty()) {
                    val quote = "\"${author.quotes[0]}\""
                    Text(
                        text = quote,
                        fontWeight = FontWeight.Bold,
                        color = fontColor,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 5) 태그 (genres를 해시태그 형태로 표시)
                val genreText = author.genres.joinToString("  ") { "#$it" }
                if (genreText.isNotEmpty()) {
                    Text(
                        text = genreText,
                        fontWeight = FontWeight.Bold,
                        color = fontColor,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // 하단: 작성하기 버튼 (우측 정렬)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF5C5945),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                onDismiss()
                                navController.navigate(Screen.Write.createRoute(author.name))
                            }
                    ) {
                        Text(
                            text = "작성하기",
                            color = Color(0xFFFFFAE6),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}
