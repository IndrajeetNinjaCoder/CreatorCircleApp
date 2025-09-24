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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.platform.LocalConfiguration
import kotlinx.coroutines.launch
import com.cc.creatorcircle.R

@Composable
fun FirstScreen(pagerState: PagerState) {

    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // Determine if device is a tablet (width >= 600dp is common tablet threshold)
    val isTablet = screenWidth >= 600.dp

//    // Responsive font sizes
//    val titleFontSize = if (isTablet) 56.sp else 28.sp // Doubled for tablet
//    val titleLineHeight = if (isTablet) 64.sp else 32.sp
//    val buttonFontSize = if (isTablet) 28.sp else 14.sp

    // Responsive font sizes
    val titleFontSize = if (isTablet) 42.sp else 28.sp // Doubled for tablet
    val titleLineHeight = if (isTablet) 48.sp else 32.sp
    val buttonFontSize = if (isTablet) 21.sp else 14.sp


    // Calculate responsive image dimensions
    val imageWidth = (screenWidth * 0.4f).coerceAtLeast(170.dp)
    val imageHeight = (screenHeight * 0.32f).coerceAtLeast(260.dp)

    // Calculate responsive offsets based on screen size
    val leftImageOffsetX = (-screenWidth * 0.08f).coerceAtMost((-40).dp)
    val leftImageOffsetY = screenHeight * 0.05f
    val rightImageOffsetX = screenWidth * 0.15f
    val rightImageOffsetY = screenHeight * 0.2f
    val centerImageOffsetY = screenHeight * 0.12f

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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.onboarding_first),
                contentDescription = "First Image",
                modifier = Modifier.fillMaxSize() // set size as needed
            )
        }


        // Next → Button
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
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//
//@Composable
//fun FirstScreen(pagerState: PagerState) {
//
//    val scope = rememberCoroutineScope()
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp
//
//    // Determine if device is a tablet (width >= 600dp is common tablet threshold)
//    val isTablet = screenWidth >= 600.dp
//
////    // Responsive font sizes
////    val titleFontSize = if (isTablet) 56.sp else 28.sp // Doubled for tablet
////    val titleLineHeight = if (isTablet) 64.sp else 32.sp
////    val buttonFontSize = if (isTablet) 28.sp else 14.sp
//
//    // Responsive font sizes
//    val titleFontSize = if (isTablet) 42.sp else 28.sp // Doubled for tablet
//    val titleLineHeight = if (isTablet) 48.sp else 32.sp
//    val buttonFontSize = if (isTablet) 21.sp else 14.sp
//
//
//
//    // Calculate responsive image dimensions
//    val imageWidth = (screenWidth * 0.4f).coerceAtLeast(170.dp)
//    val imageHeight = (screenHeight * 0.32f).coerceAtLeast(260.dp)
//
//    // Calculate responsive offsets based on screen size
//    val leftImageOffsetX = (-screenWidth * 0.08f).coerceAtMost((-40).dp)
//    val leftImageOffsetY = screenHeight * 0.05f
//    val rightImageOffsetX = screenWidth * 0.15f
//    val rightImageOffsetY = screenHeight * 0.2f
//    val centerImageOffsetY = screenHeight * 0.12f
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        Color(0xFFFCFCFC),
//                        Color(0xF0D6BDE2),
//                        Color(0xCC7921A4),
//                        Color(0xE3E749A0)
//                    )
//                )
//            )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//
//            // Left rotated image (top-left)
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = -12f)
//                    .align(Alignment.TopStart)
//                    .offset(x = leftImageOffsetX, y = leftImageOffsetY)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.Gray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_1),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Right rotated image (top-right)
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 10f)
//                    .align(Alignment.TopEnd)
//                    .offset(x = rightImageOffsetX, y = rightImageOffsetY)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.LightGray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_3),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Center front image (placed last so it's on top)
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(y = centerImageOffsetY)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_2),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
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
////                    .padding(bottom = 30.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            )
//            {
//                Text(
//                    text = "Connect with fellow\ncreators and influencers\nto collaborate, share\nideas, and grow together",
//                    textAlign = TextAlign.Center,
//                    fontSize = titleFontSize,
//                    lineHeight = titleLineHeight,
//                    color = Color.White,
//                    fontFamily = FontFamily(
//                        Font(R.font.font_lexend)
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
////                        .padding(horizontal = 24.dp)
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
//        // Next → Button
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
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import androidx.compose.ui.platform.LocalConfiguration
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//
//@Composable
//fun FirstScreen(pagerState: PagerState) {
//
//    val scope = rememberCoroutineScope()
//    val configuration = LocalConfiguration.current
//    val screenWidth = configuration.screenWidthDp.dp
//    val screenHeight = configuration.screenHeightDp.dp
//
//    // Calculate responsive image dimensions
//    val imageWidth = (screenWidth * 0.4f).coerceAtLeast(170.dp)
//    val imageHeight = (screenHeight * 0.32f).coerceAtLeast(260.dp)
//
//    // Calculate responsive offsets based on screen size
//    val leftImageOffsetX = (-screenWidth * 0.08f).coerceAtMost((-40).dp)
//    val leftImageOffsetY = screenHeight * 0.05f
//    val rightImageOffsetX = screenWidth * 0.15f
//    val rightImageOffsetY = screenHeight * 0.2f
//    val centerImageOffsetY = screenHeight * 0.12f
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        Color(0xFFFCFCFC),
//                        Color(0xF0D6BDE2),
//                        Color(0xCC7921A4),
//                        Color(0xE3E749A0)
//                    )
//                )
//            )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//
//            // Left rotated image (top-left)
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = -12f)
//                    .align(Alignment.TopStart)
//                    .offset(x = leftImageOffsetX, y = leftImageOffsetY)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.Gray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_1),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Right rotated image (top-right)
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 10f)
//                    .align(Alignment.TopEnd)
//                    .offset(x = rightImageOffsetX, y = rightImageOffsetY)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.LightGray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_3),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Center front image (placed last so it's on top)
//            Box(
//                modifier = Modifier
//                    .size(imageWidth, imageHeight)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(y = centerImageOffsetY)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_2),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//
//            // Description Text
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(horizontal = 12.dp, vertical = 60.dp),
////                    .padding(bottom = 30.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            )
//            {
//                Text(
//                    text = "Connect with fellow\ncreators and influencers\nto collaborate, share\nideas, and grow together",
//                    textAlign = TextAlign.Center,
//                    fontSize = 24.sp,
//                    lineHeight = 28.sp,
//                    color = Color.White,
//                    fontFamily = FontFamily(
//                        Font(R.font.font_lexend)
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
////                        .padding(horizontal = 24.dp)
//                        .padding(bottom = 24.dp)
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Indicator Dots
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
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
//        // Next → Button
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
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.text.style.TextAlign
////import com.example.creatorcircleapp.ui.theme.CreatorCircleAppTheme
//
//
//import androidx.compose.foundation.pager.PagerState
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.ui.text.font.Font
//import androidx.compose.ui.text.font.FontFamily
//import kotlinx.coroutines.launch
//import com.cc.creatorcircle.R
//
//@Composable
//fun FirstScreen(pagerState: PagerState) {
//
//    val scope = rememberCoroutineScope()
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    listOf(
//                        Color(0xFFFCFCFC),
//                        Color(0xF0D6BDE2),
//                        Color(0xCC7921A4),
//                        Color(0xE3E749A0)
//                    )
//                )
//            )
//    ) {
//        Box(
//            modifier = Modifier
//                .fillMaxSize()
//        ) {
//
//            // Left rotated image (top-left)
//            Box(
//                modifier = Modifier
//                    .size(170.dp, 260.dp)
//                    .graphicsLayer(rotationZ = -12f)
//                    .align(Alignment.TopStart)
//                    .offset(x = (-40).dp, y = 40.dp)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.Gray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_1),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Right rotated image (top-right)
//            Box(
//                modifier = Modifier
//                    .size(170.dp, 260.dp)
//                    .graphicsLayer(rotationZ = 10f)
//                    .align(Alignment.TopEnd)
//                    .offset(x = 75.dp, y = 160.dp)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.LightGray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_3),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//            // Center front image (placed last so it’s on top)
//            Box(
//                modifier = Modifier
//                    .size(170.dp, 260.dp)
//                    .graphicsLayer(rotationZ = 0f)
//                    .align(Alignment.TopCenter)
//                    .offset(y = 100.dp)
//                    .clip(RoundedCornerShape(20.dp))
//                    .background(Color.DarkGray)
//            ) {
//                Image(
//                    painter = painterResource(id = R.drawable.onboarding_img_2),
//                    contentDescription = "Influencer",
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clip(RoundedCornerShape(20.dp)),
//                    contentScale = ContentScale.Crop
//                )
//            }
//
//
//            // Description Text
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .align(Alignment.BottomCenter)
//                    .padding(horizontal = 12.dp, vertical = 60.dp),
////                    .padding(bottom = 30.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            )
//            {
//                Text(
//                    text = "Connect with fellow\ncreators and influencers\nto collaborate, share\nideas, and grow together",
//                    textAlign = TextAlign.Center,
//                    fontSize = 24.sp,
//                    lineHeight = 28.sp,
//                    color = Color.White,
//                    fontFamily = FontFamily(
//                        Font(R.font.font_lexend)
//                    ),
//                    modifier = Modifier
//                        .fillMaxWidth()
////                        .padding(horizontal = 24.dp)
//                        .padding(bottom = 24.dp)
//                )
//
//                Spacer(modifier = Modifier.height(24.dp))
//
//                // Indicator Dots
//                Row(
//                    horizontalArrangement = Arrangement.Center,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
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
//        // Next → Button
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
