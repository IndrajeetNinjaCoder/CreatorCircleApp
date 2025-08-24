package com.cc.creatorcircle.ui.screens.onboarding


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
//import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme


import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import com.cc.creatorcircle.R

@Composable
fun ThirdScreen(pagerState: PagerState) {

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFCFCFC),
                        Color(0xFFD6559D),
                        Color(0xFF7921A4)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {




            // Bottom Image
            Box(
                modifier = Modifier
                    .size(180.dp, 240.dp)
                    .graphicsLayer(rotationZ = 0f)
                    .align(Alignment.TopCenter)
                    .offset(x = 60.dp, y = 220.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                // Background Image
                Image(
                    painter = painterResource(id = R.drawable.onboarding_img_7),
                    contentDescription = "Influencer",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gray overlay (same rounded corners)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
                        .clip(RoundedCornerShape(10.dp))
                )

                // Top Text
                Text(
                    text = "AI Reel",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily(
                        Font(R.font.font_lexend)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                )

                // Bottom Row with Icons and Labels
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_share), // Replace with your actual icon
                            contentDescription = "Share",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "Share", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play), // Replace with your actual icon
                            contentDescription = "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "Play", color = Color.White, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_download), // Replace with your actual icon
                            contentDescription = "Download",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(text = "Download", color = Color.White, fontSize = 12.sp)
                    }
                }
            }



            // Top Image
            Box(
                modifier = Modifier
                    .size(180.dp, 240.dp)
                    .graphicsLayer(rotationZ = 0f)
                    .align(Alignment.TopCenter)
                    .offset(x = -60.dp, y = 10.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.DarkGray)
            ) {
                Box {
                    Image(
                        painter = painterResource(id = R.drawable.onboarding_img_6),
                        contentDescription = "Influencer",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    // Gray overlay (same rounded corners)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
                            .clip(RoundedCornerShape(10.dp))
                    )

                    Text(
                        text = "Idea to Reel\nin Sec with AI",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(
                            Font(R.font.font_lexend)
                        ),
                        textAlign = TextAlign.Center,
//                        fontWeight = FontWeight.Thin,
                        modifier = Modifier
                            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
                            .align(Alignment.BottomCenter)

                    )
                }

            }




            // Description Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Turn ideas into high\n-engagement reels in\nseconds with AI — from\ntext to video instantly",
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    lineHeight = 28.sp,
                    color = Color.White,
                    fontFamily = FontFamily(
                        Font(R.font.font_lexend)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
//                        .padding(horizontal = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)) // pill shape
                            .background(Color(0xFFE749A0))
                            .border(
                                width = 1.dp,
                                color = Color.White,
                                shape = RoundedCornerShape(50)
                            )

                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .height(44.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE749A0),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = 14.sp
                )
            }
        }

    }
}

