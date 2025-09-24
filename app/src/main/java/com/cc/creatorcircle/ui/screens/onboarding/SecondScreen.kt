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

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
//import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme


import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import com.cc.creatorcircle.R

@Composable
fun SecondScreen(pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Determine if device is a tablet (width >= 600dp is common tablet threshold)
    val isTablet = screenWidth >= 600.dp

    // Responsive font sizes (matching FirstScreen pattern)
    val titleFontSize = if (isTablet) 42.sp else 22.sp
    val titleLineHeight = if (isTablet) 48.sp else 28.sp
    val buttonFontSize = if (isTablet) 21.sp else 14.sp
    val overlayTextSize = if (isTablet) 30.sp else 20.sp

    // Calculate responsive image dimensions
    val imageWidth = (screenWidth * 0.42f).coerceAtLeast(180.dp)
    val imageHeight = (screenHeight * 0.3f).coerceAtLeast(240.dp)

    // Calculate responsive offsets based on screen size
    val topImageOffsetX = screenWidth * 0.12f
    val topImageOffsetY = screenHeight * 0.015f
    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
    val bottomImageOffsetY = screenHeight * 0.28f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xE5893BCF), // Purple
                        Color(0x00FFFFFF), // Transparent White
                        Color(0x00FFFFFF), // Transparent White
                        Color(0xFFEA3BA1)  // Pink
                    ),
                    start = Offset(0f, 0f), // Top-left corner
                    end = Offset.Infinite   // Bottom-right corner
                )
            )
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Brand Deal Agent",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.02).sp
            )


            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                Image(
                    painter = painterResource(id = R.drawable.onboarding_second_first),
                    contentDescription = "First Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.TopStart)
                        .offset(y = (-66).dp) // Move right 16dp, up 20dp
                )
                Image(
                    painter = painterResource(id = R.drawable.onboarding_second_second),
                    contentDescription = "Second Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.BottomEnd)
                        .offset(y = (+66).dp) // Move left 16dp, down 20dp
                )
            }
        }













        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isTablet) 32.dp else 20.dp)
        ) {
            Button(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .height(if (isTablet) 64.dp else 44.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE749A0),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(if (isTablet) 8.dp else 6.dp)
            ) {
                Text(
                    text = "Next",
                    fontSize = buttonFontSize,
                )
            }
        }

    }
}


//package com.cc.creatorcircle.ui.screens.onboarding
//
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//
//@Composable
//fun SecondScreen(pagerState: PagerState) {
//    val scope = rememberCoroutineScope()
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp
//
//    // Determine if device is a tablet (width >= 600dp is common tablet threshold)
//    val isTablet = screenWidth >= 600.dp
//
//    // Responsive font sizes (matching FirstScreen pattern)
//    val titleFontSize = if (isTablet) 42.sp else 22.sp
//    val titleLineHeight = if (isTablet) 48.sp else 28.sp
//    val buttonFontSize = if (isTablet) 21.sp else 14.sp
//    val overlayTextSize = if (isTablet) 30.sp else 20.sp
//
//    // Calculate responsive image dimensions
//    val imageWidth = (screenWidth * 0.42f).coerceAtLeast(180.dp)
//    val imageHeight = (screenHeight * 0.3f).coerceAtLeast(240.dp)
//
//    // Calculate responsive offsets based on screen size
//    val topImageOffsetX = screenWidth * 0.12f
//    val topImageOffsetY = screenHeight * 0.015f
//    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
//    val bottomImageOffsetY = screenHeight * 0.28f
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        Color(0xFFFCFCFC),
//                        Color(0xFFD6559D),
//                        Color(0xFF7921A4)
//                    )
//                )
//            )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//
//            // Bottom Image
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = bottomImageOffsetX, y = bottomImageOffsetY)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_5),
//                        contentDescription = "Influencer",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clip(RoundedCornerShape(10.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//
//                    // Gray overlay (same rounded corners)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//
//                    Text(
//                        text = "Get Feedback &\nAdvice",
//                        color = Color.White,
//                        fontSize = overlayTextSize,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(
//                                start = if (isTablet) 12.dp else 8.dp,
//                                end = if (isTablet) 12.dp else 8.dp,
//                                bottom = if (isTablet) 36.dp else 24.dp
//                            )
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//
//            // Top Image
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = topImageOffsetX, y = topImageOffsetY)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_4),
//                        contentDescription = "Influencer",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clip(RoundedCornerShape(10.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//
//
//                    // Gray overlay (same rounded corners)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//
//
//                    Text(
//                        text = "One on one\nguidance",
//                        color = Color.White,
//                        fontSize = overlayTextSize,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(
//                                start = if (isTablet) 12.dp else 8.dp,
//                                end = if (isTablet) 12.dp else 8.dp,
//                                bottom = if (isTablet) 36.dp else 24.dp
//                            )
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//            // Description Text
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(
//                        horizontal = if (isTablet) 24.dp else 12.dp,
//                        vertical = if (isTablet) 80.dp else 60.dp
//                    ),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Get personalized advice,\nreal feedback, and one\n-on-one guidance from\ntop influencers ",
//                    textAlign = TextAlign.Center,
//                    fontSize = titleFontSize,
//                    lineHeight = titleLineHeight,
//                    color = Color.White,
//                    fontFamily = FontFamily(
//                        Font(R.font.font_lexend)
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = if (isTablet) 32.dp else 24.dp)
//                )
//
//                Spacer(modifier = Modifier.height(if (isTablet) 32.dp else 24.dp))
//
//                // Indicator Dots
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    val dotSize = if (isTablet) 16.dp else 8.dp
//                    val activeDotWidth = if (isTablet) 48.dp else 24.dp
//                    val activeDotHeight = if (isTablet) 16.dp else 8.dp
//                    val spacerWidth = if (isTablet) 16.dp else 8.dp
//
//                    Box(
//                        modifier = Modifier
//                            .size(dotSize)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                    Spacer(modifier = Modifier.width(spacerWidth))
//                    Box(
//                        modifier = Modifier
//                            .width(activeDotWidth)
//                            .height(activeDotHeight)
//                            .clip(RoundedCornerShape(50)) // pill shape
//                            .background(Color(0xFFE749A0))
//                            .border(
//                                width = if (isTablet) 2.dp else 1.dp,
//                                color = Color.White,
//                                shape = RoundedCornerShape(50)
//                            )
//
//                    )
//                    Spacer(modifier = Modifier.width(spacerWidth))
//                    Box(
//                        modifier = Modifier
//                            .size(dotSize)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                    Spacer(modifier = Modifier.width(spacerWidth))
//                    Box(
//                        modifier = Modifier
//                            .size(dotSize)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                }
//            }
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(if (isTablet) 32.dp else 20.dp)
//        ) {
//            Button(
//                onClick = {
//                    scope.launch {
//                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(if (isTablet) 64.dp else 44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(if (isTablet) 8.dp else 6.dp)
//            ) {
//                Text(
//                    text = "Next",
//                    fontSize = buttonFontSize,
//                )
//            }
//        }
//
//    }
//}


//package com.cc.creatorcircle.ui.screens.onboarding
//
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//
//@Composable
//fun SecondScreen(pagerState: PagerState) {
//    val scope = rememberCoroutineScope()
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp
//
//    // Calculate responsive image dimensions
//    val imageWidth = (screenWidth * 0.42f).coerceAtLeast(180.dp)
//    val imageHeight = (screenHeight * 0.3f).coerceAtLeast(240.dp)
//
//    // Calculate responsive offsets based on screen size
//    val topImageOffsetX = screenWidth * 0.12f
//    val topImageOffsetY = screenHeight * 0.015f
//    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
//    val bottomImageOffsetY = screenHeight * 0.28f
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        Color(0xFFFCFCFC),
//                        Color(0xFFD6559D),
//                        Color(0xFF7921A4)
//                    )
//                )
//            )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//
//
//
//
//            // Bottom Image
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = bottomImageOffsetX, y = bottomImageOffsetY)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_5),
//                        contentDescription = "Influencer",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clip(RoundedCornerShape(10.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//
//                    // Gray overlay (same rounded corners)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//
//                    Text(
//                        text = "Get Feedback &\nAdvice",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//
//            // Top Image
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = topImageOffsetX, y = topImageOffsetY)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_4),
//                        contentDescription = "Influencer",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clip(RoundedCornerShape(10.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//
//
//                    // Gray overlay (same rounded corners)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//
//
//                    Text(
//                        text = "One on one\nguidance",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//
//
//
//            // Description Text
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(horizontal = 12.dp, vertical = 60.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Get personalized advice,\nreal feedback, and one\n-on-one guidance from\ntop influencers ",
//                    textAlign = TextAlign.Center,
//                    fontSize = 22.sp,
//                    lineHeight = 28.sp,
//                    color = Color.White,
//                    fontFamily = FontFamily(
//                        Font(R.font.font_lexend)
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
////                        .padding(horizontal = 24.dp)
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Indicator Dots
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .width(24.dp)
//                            .height(8.dp)
//                            .clip(RoundedCornerShape(50)) // pill shape
//                            .background(Color(0xFFE749A0))
//                            .border(
//                                width = 1.dp,
//                                color = Color.White,
//                                shape = RoundedCornerShape(50)
//                            )
//
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                }
//            }
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp)
//        ) {
//            Button(
//                onClick = {
//                    scope.launch {
//                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(6.dp)
//            ) {
//                Text(
//                    text = "Next",
//                    fontSize = 14.sp,
//                )
//            }
//        }
//
//    }
//}


//package com.cc.creatorcircle.ui.screens.onboarding
//
//
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//
//
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.graphicsLayer
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//
//@Composable
//fun SecondScreen(pagerState: PagerState) {
//    val scope = rememberCoroutineScope()
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        Color(0xFFFCFCFC),
//                        Color(0xFFD6559D),
//                        Color(0xFF7921A4)
//                    )
//                )
//            )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//
//
//
//
//            // Bottom Image
//            Box(
//                modifier = Modifier
//                    .size(180.dp, 240.dp)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = -60.dp, y = 220.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_5),
//                        contentDescription = "Influencer",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clip(RoundedCornerShape(10.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//
//                    // Gray overlay (same rounded corners)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//
//                    Text(
//                        text = "Get Feedback &\nAdvice",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//
//            // Top Image
//            Box(
//                modifier = Modifier
//                    .size(180.dp, 240.dp)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = 60.dp, y = 10.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_4),
//                        contentDescription = "Influencer",
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .clip(RoundedCornerShape(10.dp)),
//                        contentScale = ContentScale.Crop
//                    )
//
//
//                    // Gray overlay (same rounded corners)
//                    Box(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .background(Color(0x80000000)) // Semi-transparent gray (60% opacity)
//                            .clip(RoundedCornerShape(10.dp))
//                    )
//
//
//                    Text(
//                        text = "One on one\nguidance",
//                        color = Color.White,
//                        fontSize = 20.sp,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(start = 8.dp, end = 8.dp, bottom = 24.dp)
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//
//
//
//            // Description Text
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(horizontal = 12.dp, vertical = 60.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text(
//                    text = "Get personalized advice,\nreal feedback, and one\n-on-one guidance from\ntop influencers ",
//                    textAlign = TextAlign.Center,
//                    fontSize = 22.sp,
//                    lineHeight = 28.sp,
//                    color = Color.White,
//                    fontFamily = FontFamily(
//                        Font(R.font.font_lexend)
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
////                        .padding(horizontal = 24.dp)
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Indicator Dots
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .width(24.dp)
//                            .height(8.dp)
//                            .clip(RoundedCornerShape(50)) // pill shape
//                            .background(Color(0xFFE749A0))
//                            .border(
//                                width = 1.dp,
//                                color = Color.White,
//                                shape = RoundedCornerShape(50)
//                            )
//
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Box(
//                        modifier = Modifier
//                            .size(8.dp)
//                            .clip(CircleShape)
//                            .background(Color.White)
//                    )
//                }
//            }
//        }
//
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp)
//        ) {
//            Button(
//                onClick = {
//                    scope.launch {
//                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
//                    }
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(6.dp)
//            ) {
//                Text(
//                    text = "Next",
//                    fontSize = 14.sp,
//                )
//            }
//        }
//
//    }
//}
//
