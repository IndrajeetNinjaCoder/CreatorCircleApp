package com.cc.creatorcircle

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.cc.creatorcircle.ui.theme.CreatorCircleTheme

import androidx.navigation.compose.rememberNavController
import com.cc.creatorcircle.data.api.ApiService
import com.cc.creatorcircle.data.api.RetrofitInstance
import com.cc.creatorcircle.ui.navigation.NavigationHost
import com.cc.creatorcircle.ui.screens.onboarding.FirstScreen
import com.cc.creatorcircle.ui.screens.onboarding.FourthScreen
import com.cc.creatorcircle.ui.screens.onboarding.SecondScreen
import com.cc.creatorcircle.ui.screens.onboarding.ThirdScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContent {
            CreatorCircleTheme(darkTheme = false) {
                val context = LocalContext.current
                val navController = rememberNavController()


                // Check if onboarding has been completed
                val sharedPrefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                val isOnboardingCompleted = remember {
                    mutableStateOf(sharedPrefs.getBoolean("onboarding_completed", false))
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        if (isOnboardingCompleted.value) {
                            // Show main navigation with login as start destination
                            NavigationHost(navController = navController)
                        } else {
                            // Show onboarding screens
                            OnboardingPager(
                                onFinish = {
                                    // Mark onboarding as completed
                                    sharedPrefs.edit()
                                        .putBoolean("onboarding_completed", true)
                                        .apply()
                                    isOnboardingCompleted.value = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingPager(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val coroutineScope = rememberCoroutineScope()

//    LaunchedEffect(pagerState.currentPage) {
//        if (pagerState.currentPage == 3 && pagerState.currentPageOffsetFraction == 0f) {
//            delay(300)
//            onFinish()
//        }
//    }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> FirstScreen(pagerState)
            1 -> SecondScreen(pagerState)
//            2 -> ThirdScreen(pagerState)
            2 -> FourthScreen(onFinish = onFinish)
        }
    }
}