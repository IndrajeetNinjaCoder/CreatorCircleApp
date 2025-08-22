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
import androidx.compose.ui.text.style.TextAlign
//import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme


import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import kotlinx.coroutines.launch
import com.cc.creatorcircle.R

@Composable
fun FirstScreen(pagerState: PagerState) {

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFFCFCFC),
                        Color(0xF0D6BDE2),
                        Color(0xCC7921A4),
                        Color(0xE3E749A0)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            // Left rotated image (top-left)
            Box(
                modifier = Modifier
                    .size(170.dp, 260.dp)
                    .graphicsLayer(rotationZ = -12f)
                    .align(Alignment.TopStart)
                    .offset(x = (-40).dp, y = 40.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Gray)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_img_1),
                    contentDescription = "Influencer",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Right rotated image (top-right)
            Box(
                modifier = Modifier
                    .size(170.dp, 260.dp)
                    .graphicsLayer(rotationZ = 10f)
                    .align(Alignment.TopEnd)
                    .offset(x = 75.dp, y = 160.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.LightGray)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_img_3),
                    contentDescription = "Influencer",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Center front image (placed last so it’s on top)
            Box(
                modifier = Modifier
                    .size(170.dp, 260.dp)
                    .graphicsLayer(rotationZ = 0f)
                    .align(Alignment.TopCenter)
                    .offset(y = 100.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.DarkGray)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_img_2),
                    contentDescription = "Influencer",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
            }


            // Description Text
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 12.dp, vertical = 60.dp),
//                    .padding(bottom = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Text(
                    text = "Connect with fellow\ncreators and influencers\nto collaborate, share\nideas, and grow together",
                    textAlign = TextAlign.Center,
                    fontSize = 24.sp,
                    lineHeight = 28.sp,
                    color = Color.White,
                    fontFamily = FontFamily(
                        Font(R.font.font_lexend)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
//                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }

        // Next → Button
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
                    fontSize = 14.sp,
                )
            }
        }

    }
}
