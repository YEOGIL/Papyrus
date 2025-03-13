package com.intel.papyrusbaby.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.intel.papyrusbaby.AppBar
import com.intel.papyrusbaby.PersonCategory
import com.intel.papyrusbaby.R


@Composable
fun HomeScreen(navController: NavController) {
    AppBar(content = { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFAF3))
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.size(30.dp))

            // Card with description
            Card(
                modifier = Modifier
                    .size(width = 200.dp, height = 100.dp)
                    .align(Alignment.CenterHorizontally),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "편지는 사람이 남길 수 있는 가장 중요한 기념물이다",
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.size(50.dp))

            // Row with horizontal scrolling and spacing between PersonBox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(30.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PersonCategory.getAll().forEach { category ->
                    PersonBox(category,navController)
                }
            }
        }
    }, navController = navController)
}

@Composable
fun PersonBox(person: PersonCategory, navController: NavController) {
    Box(
        modifier = Modifier.fillMaxWidth()
            .clickable {  navController.navigate("write") {
                launchSingleTop = true
                popUpTo("home")
            }  },
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(10.dp)
            )
        }
    }
}
