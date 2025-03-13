package com.intel.papyrusbaby.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intel.papyrusbaby.AppBar

@Composable
fun WriteLetterScreen(navController: NavController){
    AppBar(content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFAF3))
                .padding(paddingValues)
        ) {
            // 메시지 리스트 상태 관리
            val messages = remember { mutableStateListOf<ChatMessage>() }
            // 현재 입력 텍스트 상태 관리
            var currentInput by remember { mutableStateOf("") }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFFFAF3))
            ) {
                // 메시지 리스트 영역
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    items(items = messages) { message ->
                        ChatBubble(message = message)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // 하단 입력 영역
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color(0xFFFFFFFF))
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomInputField(
                        currentInput = currentInput,
                        onValueChange = { currentInput = it },
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                // 사용자의 메시지 추가
                                messages.add(ChatMessage(text = currentInput, isUser = true))
                                // TODO: 서버에 currentInput 전송 후 챗봇 응답 처리
                                currentInput = ""
                            }
                        }
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "전송")
                    }
                }
            }
        }
    }, navController = navController)
}


@Composable
fun ChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFEBFFFE))
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                color = Color(0xFF1B1818),
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun CustomInputField(currentInput: String, onValueChange: (String) -> Unit, modifier: Modifier) {
    BasicTextField(
        value = currentInput,
        onValueChange = onValueChange,
        modifier = modifier
            .background(Color(0xFFF0F0F0), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        textStyle = TextStyle(
            color = Color(0xFF1B1818),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        ),
        decorationBox = { innerTextField ->
            if (currentInput.isEmpty()) {
                Text(
                    text = "메시지를 입력하세요",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            innerTextField()
        }
    )
}

// 메시지 데이터 클래스
data class ChatMessage(
    val text: String,
    val isUser: Boolean
)