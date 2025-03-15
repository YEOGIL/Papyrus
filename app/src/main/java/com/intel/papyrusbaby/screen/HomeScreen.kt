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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.intel.papyrusbaby.PersonCategory
import com.intel.papyrusbaby.R


@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFfffae6))
    ) {
        Spacer(modifier = Modifier.size(30.dp))
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp)) {
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
            PersonCategory.getAll().forEach { category ->
                PersonBox(category, navController)
            }
        }
    }
}

@Composable
fun PersonBox(person: PersonCategory, navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate("write") {
                    launchSingleTop = true
                }
            },
        contentAlignment = Alignment.Center

    ) {
        Image(
            painter = painterResource(id = R.drawable.personbox),
            contentDescription = "personBox background",
            modifier = Modifier.offset(y = (-5).dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Image(
                painter = painterResource(id = person.thumbnail),
                contentDescription = "category thumbnail",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                person.name,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Text(
                person.description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
            )
            Text(
                person.tags.joinToString(" "),
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp)
            )
        }
    }
}
