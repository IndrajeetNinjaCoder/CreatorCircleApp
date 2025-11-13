package com.cc.creatorcircle.ui.components

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import com.cc.creatorcircle.R
import com.cc.creatorcircle.ui.navigation.Screen
import com.cc.creatorcircle.viewModel.PostsViewModel
import com.cc.creatorcircle.viewModel.PostsViewModelFactory

@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Get context and create viewModel to fetch user profile
    val context = LocalContext.current
    val viewModel: PostsViewModel = viewModel(
        factory = PostsViewModelFactory(context)
    )

    // User profile states
    val userProfile by viewModel.userProfile.collectAsState()

    // Fetch user profile when BottomNavBar is first created
    LaunchedEffect(Unit) {
        viewModel.fetchUserProfile()
    }

    // Get current route to determine active tab
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 8.dp), // Reduced from vertical = 2.dp
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                iconRes = R.drawable.ic_home,
                label = "Home",
                isActive = currentRoute == Screen.Home.route,
                activeTint = Color(0xFFB726FF),
                inactiveTint = Color(0xFF6B7280),
                onClick = {
                    if (currentRoute != Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )

//            BottomNavItem(
//                iconRes = R.drawable.ic_connections,
//                label = "Connections",
//                isActive = currentRoute == Screen.Connections.route,
//                activeTint = Color(0xFFB726FF),
//                inactiveTint = Color(0xFF6B7280),
//                onClick = {
//                    if (currentRoute != Screen.Connections.route) {
//                        navController.navigate(Screen.Connections.route) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
//                }
//            )


            BottomNavItem(
                iconRes = R.drawable.ic_brand_collab,
                label = "Brand Collab",
                isActive = currentRoute == Screen.BrandCollab.route,
                activeTint = Color(0xFFB726FF),
                inactiveTint = Color(0xFF6B7280),
                onClick = {
                    Log.d("BottomNav", "Brand Collab clicked")
                    if (currentRoute != Screen.BrandCollab.route) {
                        navController.navigate(Screen.BrandCollab.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )




            BottomNavItem(
                iconRes = R.drawable.ic_sabo_ai,
                label = "SABO AI",
                isActive = currentRoute == Screen.SaboAI.route,
                activeTint = Color(0xFFFF6600),
                inactiveTint = Color(0xFFFF6600),
                onClick = {
                    if (currentRoute != Screen.SaboAI.route) {
                        navController.navigate(Screen.SaboAI.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )

            BottomNavItem(
                iconRes = R.drawable.ic_video,
                label = "Mentor Circle",
                isActive = currentRoute == Screen.LiveSession.route,
                activeTint = Color(0xFFB726FF),
                inactiveTint = Color(0xFF6B7280),
                onClick = {
                    if (currentRoute != Screen.LiveSession.route) {
                        navController.navigate(Screen.LiveSession.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )

            // For profile icon in circle with user's actual profile picture
            BottomNavProfileItem(
                iconRes = R.drawable.ic_profile1,
                label = "You",
                isActive = currentRoute == Screen.ProfileWeb.route,
                activeTint = Color(0xFFB726FF),
                inactiveTint = Color(0xFF6B7280),
                userProfilePic = userProfile?.profile_pic,
                onClick = {
                    if (currentRoute != Screen.ProfileWeb.route) {
//                        navController.navigate(Screen.ProfileWeb.route) {
                        navController.navigate(Screen.ProfileScreen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    iconRes: Int,
    label: String,
    isActive: Boolean,
    activeTint: Color,
    inactiveTint: Color,
    onClick: () -> Unit = {}
) {
    // Animated colors and scale
    val animatedTintColor by animateColorAsState(
        targetValue = if (isActive) activeTint else inactiveTint,
        animationSpec = tween(durationMillis = 100),
        label = "tint_animation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "scale_animation"
    )

    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
    val backgroundColor = if (isActive) activeTint.copy(alpha = 0.1f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    radius = 32.dp,
                    color = activeTint
                )
            ) { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp) // Reduced from vertical = 4.dp
            .scale(animatedScale)
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = label,
            modifier = Modifier.size(if (isActive) 24.dp else 22.dp),
            colorFilter = ColorFilter.tint(animatedTintColor)
        )

        Spacer(modifier = Modifier.height(2.dp)) // Reduced from 4.dp

        Text(
            text = label,
            color = animatedTintColor,
            fontSize = if (isActive) 11.sp else 10.sp,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Composable
fun BottomNavProfileItem(
    iconRes: Int,
    label: String,
    isActive: Boolean,
    activeTint: Color,
    inactiveTint: Color,
    userProfilePic: String? = null,
    onClick: () -> Unit = {}
) {
    // Animated colors and scale
    val animatedTintColor by animateColorAsState(
        targetValue = if (isActive) activeTint else inactiveTint,
        animationSpec = tween(durationMillis = 300),
        label = "profile_tint_animation"
    )

    val animatedScale by animateFloatAsState(
        targetValue = if (isActive) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "profile_scale_animation"
    )

    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
    val backgroundColor = if (isActive) activeTint.copy(alpha = 0.1f) else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    bounded = true,
                    radius = 32.dp,
                    color = activeTint
                )
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 2.dp) // Reduced from vertical = 8.dp
            .scale(animatedScale)
    ) {
        ProfileIcon(
            iconRes = iconRes,
            borderColor = animatedTintColor,
            isActive = isActive,
            userProfilePic = userProfilePic
        )

        Spacer(modifier = Modifier.height(2.dp)) // Reduced from 4.dp

        Text(
            text = label,
            color = animatedTintColor,
            fontSize = if (isActive) 12.sp else 11.sp,
            fontWeight = fontWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }
}

@Composable
fun ProfileIcon(
    iconRes: Int,
    borderColor: Color,
    isActive: Boolean,
    userProfilePic: String? = null
) {
//    val borderWidth = if (isActive) 3.dp else 2.dp
    val iconSize = if (isActive) 32.dp else 30.dp

    // Animated border color
    val animatedBorderColor by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(durationMillis = 100),
        label = "border_color_animation"
    )

    // Show user's actual profile picture if available, otherwise show default icon
    if (userProfilePic?.isNotEmpty() == true) {
        AsyncImage(
            model = userProfilePic,
            contentDescription = "Profile",
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
//                .border(borderWidth, animatedBorderColor, CircleShape)
                .background(Color.White, CircleShape),
            placeholder = painterResource(id = iconRes),
            error = painterResource(id = iconRes),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .size(iconSize)
                .clip(CircleShape)
//                .border(borderWidth, animatedBorderColor, CircleShape)
                .background(Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Profile",
                modifier = Modifier.size(20.dp),
                colorFilter = ColorFilter.tint(animatedBorderColor)
            )
        }
    }
}












//package com.cc.creatorcircle.ui.components
//
//import androidx.compose.animation.animateColorAsState
//import androidx.compose.animation.core.animateFloatAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material3.ripple
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.ColorFilter
//import androidx.compose.ui.layout.ContentScale
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.res.painterResource
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import androidx.navigation.NavController
//import androidx.navigation.compose.currentBackStackEntryAsState
//import coil.compose.AsyncImage
//import com.cc.creatorcircle.R
//import com.cc.creatorcircle.ui.navigation.Screen
//import com.cc.creatorcircle.viewModel.PostsViewModel
//import com.cc.creatorcircle.viewModel.PostsViewModelFactory
//
//@Composable
//fun BottomNavBar(
//    navController: NavController,
//    modifier: Modifier = Modifier
//) {
//    // Get context and create viewModel to fetch user profile
//    val context = LocalContext.current
//    val viewModel: PostsViewModel = viewModel(
//        factory = PostsViewModelFactory(context)
//    )
//
//    // User profile states
//    val userProfile by viewModel.userProfile.collectAsState()
//
//    // Fetch user profile when BottomNavBar is first created
//    LaunchedEffect(Unit) {
//        viewModel.fetchUserProfile()
//    }
//
//    // Get current route to determine active tab
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentRoute = navBackStackEntry?.destination?.route
//
//    Surface(
//        modifier = modifier.fillMaxWidth(),
//        color = Color.White,
//        shadowElevation = 8.dp
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 2.dp, horizontal = 8.dp),
//            horizontalArrangement = Arrangement.SpaceEvenly,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            BottomNavItem(
//                iconRes = R.drawable.ic_home,
//                label = "Home",
//                isActive = currentRoute == Screen.Home.route,
//                activeTint = Color(0xFFB726FF),
//                inactiveTint = Color(0xFF6B7280),
//                onClick = {
//                    if (currentRoute != Screen.Home.route) {
//                        navController.navigate(Screen.Home.route) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
//                }
//            )
//
//            BottomNavItem(
//                iconRes = R.drawable.ic_connections,
//                label = "Connections",
//                isActive = currentRoute == Screen.Connections.route,
//                activeTint = Color(0xFFB726FF),
//                inactiveTint = Color(0xFF6B7280),
//                onClick = {
//                    if (currentRoute != Screen.Connections.route) {
//                        navController.navigate(Screen.Connections.route) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
//                }
//            )
//
//            BottomNavItem(
//                iconRes = R.drawable.ic_sabo_ai,
//                label = "Sabo AI",
//                isActive = currentRoute == Screen.SaboAI.route,
//                activeTint = Color(0xFFFF6600),
//                inactiveTint = Color(0xFFFF6600),
//                onClick = {
//                    if (currentRoute != Screen.SaboAI.route) {
//                        navController.navigate(Screen.SaboAI.route) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
//                }
//            )
//
//            BottomNavItem(
//                iconRes = R.drawable.ic_video,
//                label = "Live Session",
//                isActive = currentRoute == Screen.LiveSession.route,
//                activeTint = Color(0xFFB726FF),
//                inactiveTint = Color(0xFF6B7280),
//                onClick = {
//                    if (currentRoute != Screen.LiveSession.route) {
//                        navController.navigate(Screen.LiveSession.route) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
//                }
//            )
//
//            // For profile icon in circle with user's actual profile picture
//            BottomNavProfileItem(
//                iconRes = R.drawable.ic_profile,
//                label = "You",
//                isActive = currentRoute == Screen.ProfileWeb.route,
//                activeTint = Color(0xFFB726FF),
//                inactiveTint = Color(0xFF6B7280),
//                userProfilePic = userProfile?.profile_pic,
//                onClick = {
//                    if (currentRoute != Screen.ProfileWeb.route) {
//                        navController.navigate(Screen.ProfileWeb.route) {
//                            popUpTo(navController.graph.startDestinationId)
//                            launchSingleTop = true
//                        }
//                    }
//                }
//            )
//        }
//    }
//}
//
//@Composable
//fun BottomNavItem(
//    iconRes: Int,
//    label: String,
//    isActive: Boolean,
//    activeTint: Color,
//    inactiveTint: Color,
//    onClick: () -> Unit = {}
//) {
//    // Animated colors and scale
//    val animatedTintColor by animateColorAsState(
//        targetValue = if (isActive) activeTint else inactiveTint,
//        animationSpec = tween(durationMillis = 100),
//        label = "tint_animation"
//    )
//
//    val animatedScale by animateFloatAsState(
//        targetValue = if (isActive) 1.1f else 1f,
//        animationSpec = tween(durationMillis = 100),
//        label = "scale_animation"
//    )
//
//    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
//    val backgroundColor = if (isActive) activeTint.copy(alpha = 0.1f) else Color.Transparent
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .clip(RoundedCornerShape(12.dp))
//            .background(backgroundColor)
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = ripple(
//                    bounded = true,
//                    radius = 32.dp,
//                    color = activeTint
//                )
//            ) { onClick() }
//            .padding(horizontal = 6.dp, vertical = 4.dp)
//            .scale(animatedScale)
//    ) {
//        Image(
//            painter = painterResource(id = iconRes),
//            contentDescription = label,
//            modifier = Modifier.size(if (isActive) 24.dp else 22.dp),
//            colorFilter = ColorFilter.tint(animatedTintColor)
//        )
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = label,
//            color = animatedTintColor,
//            fontSize = if (isActive) 11.sp else 10.sp,
//            fontWeight = fontWeight,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//
//    }
//}
//
//@Composable
//fun BottomNavProfileItem(
//    iconRes: Int,
//    label: String,
//    isActive: Boolean,
//    activeTint: Color,
//    inactiveTint: Color,
//    userProfilePic: String? = null,
//    onClick: () -> Unit = {}
//) {
//    // Animated colors and scale
//    val animatedTintColor by animateColorAsState(
//        targetValue = if (isActive) activeTint else inactiveTint,
//        animationSpec = tween(durationMillis = 300),
//        label = "profile_tint_animation"
//    )
//
//    val animatedScale by animateFloatAsState(
//        targetValue = if (isActive) 1.1f else 1f,
//        animationSpec = tween(durationMillis = 200),
//        label = "profile_scale_animation"
//    )
//
//    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
//    val backgroundColor = if (isActive) activeTint.copy(alpha = 0.1f) else Color.Transparent
//
//    Column(
//        horizontalAlignment = Alignment.CenterHorizontally,
//        modifier = Modifier
//            .clip(RoundedCornerShape(12.dp))
//            .background(backgroundColor)
//            .clickable(
//                interactionSource = remember { MutableInteractionSource() },
//                indication = ripple(
//                    bounded = true,
//                    radius = 32.dp,
//                    color = activeTint
//                )
//            ) { onClick() }
//            .padding(horizontal = 12.dp, vertical = 8.dp)
//            .scale(animatedScale)
//    ) {
//        ProfileIcon(
//            iconRes = iconRes,
//            borderColor = animatedTintColor,
//            isActive = isActive,
//            userProfilePic = userProfilePic
//        )
//
//        Spacer(modifier = Modifier.height(4.dp))
//
//        Text(
//            text = label,
//            color = animatedTintColor,
//            fontSize = if (isActive) 12.sp else 11.sp,
//            fontWeight = fontWeight,
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis
//        )
//
//    }
//}
//
//@Composable
//fun ProfileIcon(
//    iconRes: Int,
//    borderColor: Color,
//    isActive: Boolean,
//    userProfilePic: String? = null
//) {
//    val borderWidth = if (isActive) 3.dp else 2.dp
//    val iconSize = if (isActive) 34.dp else 32.dp
//
//    // Animated border color
//    val animatedBorderColor by animateColorAsState(
//        targetValue = borderColor,
//        animationSpec = tween(durationMillis = 100),
//        label = "border_color_animation"
//    )
//
//    // Show user's actual profile picture if available, otherwise show default icon
//    if (userProfilePic?.isNotEmpty() == true) {
//        AsyncImage(
//            model = userProfilePic,
//            contentDescription = "Profile",
//            modifier = Modifier
//                .size(iconSize)
//                .clip(CircleShape)
//                .border(borderWidth, animatedBorderColor, CircleShape)
//                .background(Color.White, CircleShape),
//            placeholder = painterResource(id = iconRes),
//            error = painterResource(id = iconRes),
//            contentScale = ContentScale.Crop
//        )
//    } else {
//        Box(
//            modifier = Modifier
//                .size(iconSize)
//                .clip(CircleShape)
//                .border(borderWidth, animatedBorderColor, CircleShape)
//                .background(Color.White, CircleShape),
//            contentAlignment = Alignment.Center
//        ) {
//            Image(
//                painter = painterResource(id = iconRes),
//                contentDescription = "Profile",
//                modifier = Modifier.size(20.dp),
//                colorFilter = ColorFilter.tint(animatedBorderColor)
//            )
//        }
//    }
//}
//






// Active indicator dot
//        if (isActive) {
//            Spacer(modifier = Modifier.height(2.dp))
//            Box(
//                modifier = Modifier
//                    .size(4.dp)
//                    .background(
//                        brush = Brush.radialGradient(
//                            colors = listOf(activeTint, activeTint.copy(alpha = 0.7f))
//                        ),
//                        shape = CircleShape
//                    )
//            )
//        }