package com.cc.creatorcircle.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cc.creatorcircle.R
import com.cc.creatorcircle.utils.FirebaseAnalyticsHelper

@Composable
fun FourthScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val isTablet = screenWidth >= 600.dp

    val titleFontSize = if (isTablet) 42.sp else 22.sp
    val titleLineHeight = if (isTablet) 48.sp else 28.sp
    val buttonFontSize = if (isTablet) 21.sp else 14.sp
    val overlayTextSize1 = if (isTablet) 27.sp else 18.sp
    val overlayTextSize2 = if (isTablet) 28.sp else 19.sp

    val imageWidth = (screenWidth * 0.42f).coerceAtLeast(180.dp)
    val imageHeight = (screenHeight * 0.3f).coerceAtLeast(240.dp)

    val topImageOffsetX = screenWidth * 0.12f
    val topImageOffsetY = screenHeight * 0.003f
    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
    val bottomImageOffsetY = screenHeight * 0.27f

    // Track screen view
    LaunchedEffect(Unit) {
        FirebaseAnalyticsHelper.logScreenView("ThirdOnboardingScreen", "FourthScreen")
        FirebaseAnalyticsHelper.logEvent("onboarding_carousel_page_viewed", mapOf(
            "page_number" to "3",
            "page_name" to "mentor_circle"
        ))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xE5893BCF),
                        Color(0x00FFFFFF),
                        Color(0x00FFFFFF),
                        Color(0xFFEA3BA1)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Mentor Circle",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                letterSpacing = (-0.02).sp
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.onboarding_third),
                    contentDescription = "First Image",
                    modifier = Modifier.fillMaxSize()
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
                    FirebaseAnalyticsHelper.logEvent("onboarding_carousel_completed", mapOf(
                        "total_pages" to "3",
                        "completion_method" to "finish_button"
                    ))
                    onFinish()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .height(if (isTablet) 64.dp else 44.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE749A0),
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isTablet) 8.dp else 6.dp)
            ) {
                Text(
                    text = "Finish",
                    fontSize = buttonFontSize,
                    fontFamily = FontFamily(Font(R.font.font_lexend))
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
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import androidx.compose.ui.text.font.FontWeight
//import com.cc.creatorcircle.R
//
//@Composable
//fun FourthScreen(onFinish: () -> Unit) {
//
//    val context = LocalContext.current
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
//    val overlayTextSize1 = if (isTablet) 27.sp else 18.sp // For SABO AI overlay text
//    val overlayTextSize2 = if (isTablet) 28.sp else 19.sp // For SABO AI Agents overlay text
//
//    // Calculate responsive image dimensions
//    val imageWidth = (screenWidth * 0.42f).coerceAtLeast(180.dp)
//    val imageHeight = (screenHeight * 0.3f).coerceAtLeast(240.dp)
//
//    // Calculate responsive offsets based on screen size
//    val topImageOffsetX = screenWidth * 0.12f
//    val topImageOffsetY = screenHeight * 0.003f
//    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
//    val bottomImageOffsetY = screenHeight * 0.27f
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.linearGradient(
//                    colors = listOf(
//                        Color(0xE5893BCF), // Purple
//                        Color(0x00FFFFFF), // Transparent White
//                        Color(0x00FFFFFF), // Transparent White
//                        Color(0xFFEA3BA1)  // Pink
//                    ),
//                    start = Offset(0f, 0f), // Top-left corner
//                    end = Offset.Infinite   // Bottom-right corner
//                )
//            )
//    ) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Mentor Circle",
//                fontSize = 28.sp,
//                fontWeight = FontWeight.Bold,
//                textAlign = TextAlign.Center,
//                letterSpacing = (-0.02).sp
//            )
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_third),
//                    contentDescription = "First Image",
//                    modifier = Modifier.fillMaxSize() // set size as needed
//                )
//            }
//        }
//
//        // "Finish" Button at bottom right (themed)
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(if (isTablet) 32.dp else 20.dp)
//        ) {
//            Button(
//                onClick = {
//                    onFinish()  // Call the shared finish logic
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(if (isTablet) 64.dp else 44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isTablet) 8.dp else 6.dp)
//            ) {
//                Text(
//                    text = "Finish",
//                    fontSize = buttonFontSize,
//                    fontFamily = FontFamily(Font(R.font.font_lexend))
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
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import com.cc.creatorcircle.R
//
//@Composable
//fun FourthScreen(onFinish: () -> Unit) {
//
//    val context = LocalContext.current
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
//    val overlayTextSize1 = if (isTablet) 27.sp else 18.sp // For SABO AI overlay text
//    val overlayTextSize2 = if (isTablet) 28.sp else 19.sp // For SABO AI Agents overlay text
//
//    // Calculate responsive image dimensions
//    val imageWidth = (screenWidth * 0.42f).coerceAtLeast(180.dp)
//    val imageHeight = (screenHeight * 0.3f).coerceAtLeast(240.dp)
//
//    // Calculate responsive offsets based on screen size
//    val topImageOffsetX = screenWidth * 0.12f
//    val topImageOffsetY = screenHeight * 0.003f
//    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
//    val bottomImageOffsetY = screenHeight * 0.27f
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
//                        painter = painterResource(id = R.drawable.onboarding_img_8),
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
//                        text = "SABO AI: Chat to\nEnhance profile",
//                        color = Color.White,
//                        fontSize = overlayTextSize1,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(
//                                start = if (isTablet) 9.dp else 6.dp,
//                                end = if (isTablet) 9.dp else 6.dp,
//                                bottom = if (isTablet) 36.dp else 24.dp
//                            )
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
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
//                        painter = painterResource(id = R.drawable.onboarding_img_9),
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
//                        text = "SABO AI Agents:\nTo bring brand collaboration",
//                        color = Color.White,
//                        fontSize = overlayTextSize2,
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
//                    text = "Your AI partner in growth\n— from ideation to\nmonetization with viral\nstrategies and brand\ncollabs",
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
//                }
//            }
//        }
//
//        // "Finish" Button at bottom right (themed)
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(if (isTablet) 32.dp else 20.dp)
//        ) {
//            Button(
//                onClick = {
//                    onFinish()  // Call the shared finish logic
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(if (isTablet) 64.dp else 44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = if (isTablet) 8.dp else 6.dp)
//            ) {
//                Text(
//                    text = "Finish",
//                    fontSize = buttonFontSize,
//                    fontFamily = FontFamily(Font(R.font.font_lexend))
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
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import com.cc.creatorcircle.R
//
//@Composable
//fun FourthScreen(onFinish: () -> Unit) {
//
//    val context = LocalContext.current
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
//    val topImageOffsetY = screenHeight * 0.003f
//    val bottomImageOffsetX = (-screenWidth * 0.12f).coerceAtMost((-60).dp)
//    val bottomImageOffsetY = screenHeight * 0.27f
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
//                        painter = painterResource(id = R.drawable.onboarding_img_8),
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
//                        text = "SABO AI: Chat to\nEnhance profile",
//                        color = Color.White,
//                        fontSize = 18.sp,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(start = 6.dp, end = 6.dp, bottom = 24.dp)
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
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
//                        painter = painterResource(id = R.drawable.onboarding_img_9),
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
//                        text = "SABO AI Agents:\nTo bring brand collaboration",
//                        color = Color.White,
//                        fontSize = 19.sp,
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
//                    text = "Your AI partner in growth\n— from ideation to\nmonetization with viral\nstrategies and brand\ncollabs",
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
//                }
//            }
//        }
//
//
//        // "Finish" Button at bottom right (themed)
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp)
//        ) {
//            Button(
//                onClick = {
//                    onFinish()  // 🔁 Call the shared finish logic
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
//            ) {
//                Text(
//                    text = "Finish",
//                    fontSize = 14.sp,
//                    fontFamily = FontFamily(Font(R.font.font_lexend))
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
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//
//
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import com.cc.creatorcircle.R
//
//@Composable
//fun FourthScreen(onFinish: () -> Unit) {
//
//    val context = LocalContext.current
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
//            // Top Image
//            Box(
//                modifier = Modifier
//                    .size(180.dp, 240.dp)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = 60.dp, y = 2.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_8),
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
//                        text = "SABO AI: Chat to\nEnhance profile",
//                        color = Color.White,
//                        fontSize = 18.sp,
//                        fontFamily = FontFamily(
//                            Font(R.font.font_lexend)
//                        ),
//                        textAlign = TextAlign.Center,
////                        fontWeight = FontWeight.Thin,
//                        modifier = Modifier
//                            .padding(start = 6.dp, end = 6.dp, bottom = 24.dp)
//                            .align(Alignment.BottomCenter)
//
//                    )
//                }
//
//            }
//
//
//
//            // Bottom Image
//            Box(
//                modifier = Modifier
//                    .size(180.dp, 240.dp)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(x = -60.dp, y = 214.dp)
//                    .clip(RoundedCornerShape(10.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Box {
//                    Image(
//                        painter = painterResource(id = R.drawable.onboarding_img_9),
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
//                        text = "SABO AI Agents:\nTo bring brand collaboration",
//                        color = Color.White,
//                        fontSize = 19.sp,
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
//                    text = "Your AI partner in growth\n— from ideation to\nmonetization with viral\nstrategies and brand\ncollabs",
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
//                }
//            }
//        }
//
//
//        // "Finish" Button at bottom right (themed)
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(20.dp)
//        ) {
//            Button(
//                onClick = {
//                    onFinish()  // 🔁 Call the shared finish logic
//                },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .height(44.dp),
//                shape = RoundedCornerShape(50),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Color(0xFFE749A0),
//                    contentColor = Color.White
//                ),
//                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
//            ) {
//                Text(
//                    text = "Finish",
//                    fontSize = 14.sp,
//                    fontFamily = FontFamily(Font(R.font.font_lexend))
//                )
//            }
//        }
//
//    }
//}
//
