package com.intel.papyrusbaby.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun AuthorInfoDialog(author: Author, navController: NavController, onDismiss: () -> Unit) {
    val fontColor = Color(0xFF221F10)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF7ECCD),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Color(0xFFFFFAE6),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .background(color = Color(0xFF5C5945), shape = RoundedCornerShape(8.dp))
                            .clickable { onDismiss() }
                    )
                }
                Row {
                    // 1) 네트워크 이미지 로딩 (Coil)
                    AsyncImage(
                        model = author.imageUrl,
                        contentDescription = "Author Image",
                        modifier = Modifier.height(80.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.End
                    ) {
                        // 2) 이름
                        Text(
                            text = author.name,
                            fontWeight = FontWeight.ExtraBold,
                            color = fontColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3) 직업 (occupation)
                        // 예: ["작가", "시인"] -> "작가 시인"
                        val occupationText = author.occupation.joinToString(", ")
                        if (occupationText.isNotEmpty()) {
                            Text(
                                text = occupationText,
                                fontWeight = FontWeight.Bold,
                                color = fontColor,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 4) 장르를 해시태그 형식으로 표시
                        // 예: ["철학", "담담한"] -> "#철학 #담담한"
                        val genreText = author.genres.joinToString("  ") { "#$it" }
                        if (genreText.isNotEmpty()) {
                            Text(
                                text = genreText,
                                fontWeight = FontWeight.Bold,
                                color = fontColor,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 5) 작가 작품
                        // 예: ["작품1", "작품2"] -> "작품1, 작품2"
                        val worksText = author.works.joinToString("  ")
                        if (worksText.isNotEmpty()) {
                            Text(
                                text = worksText,
                                fontWeight = FontWeight.Bold,
                                color = fontColor,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 6) 작가 명언
                        // 예: ["명언1", "명언2"] -> "명언1, 명언2"
                        val quotesText = author.quotes.joinToString("\n") { quote -> "\"$quote\"" }
                        if (quotesText.isNotEmpty()) {
                            Text(
                                text = quotesText,
                                fontWeight = FontWeight.Bold,
                                color = fontColor,
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier.background(
                                color = Color(0xFF5C5945),
                                shape = RoundedCornerShape(8.dp)
                            )
                        ) {
                            Text(
                                text = "작성하기",
                                color = Color(0xFFFFFAE6),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .clickable {
                                        // 1) 다이얼로그 닫기
                                        onDismiss()

                                        // 2) author.name 을 "write?writer=..." 로 이동
                                        navController.navigate(Screen.Write.createRoute(author.name))
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}