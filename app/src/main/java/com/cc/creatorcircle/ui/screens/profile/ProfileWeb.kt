package com.cc.creatorcircle.ui.screens.profile


import android.content.Context
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceError
import android.widget.FrameLayout
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar
import com.cc.creatorcircle.ui.components.TopBarProfile
import com.cc.creatorcircle.viewModel.UserViewModel
import com.cc.creatorcircle.viewModel.UserViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileWeb(
    navController: NavController
) {
    val context = LocalContext.current

    val userViewModel: UserViewModel = viewModel(
        factory = UserViewModelFactory(context)
    )

    // Create and remember the WebView instance
    val myWebView = remember {
        WebView(context).apply {

            // Get token from SharedPreferences
            val prefs = context.getSharedPreferences("CCPrefs", Context.MODE_PRIVATE)
            val accessToken = prefs.getString("access_token", "") ?: ""

            Log.d("WebView", "Access token: $accessToken")

            // CRITICAL FIX 1: Set background color to prevent white flash
            setBackgroundColor(Color.White.toArgb())

            // CRITICAL FIX 2: Set layer type for better rendering
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)

            // Clear any existing data
            clearCache(true)
            clearFormData()
            clearHistory()

            // Set up Cookie Manager (for backend authentication)
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.setAcceptThirdPartyCookies(
                this,
                true
            ) // FIX 3: Enable third-party cookies

            if (accessToken.isNotEmpty()) {
                val cookieValue = "access_token=$accessToken"
                cookieManager.setCookie("https://creatorcircle.in", cookieValue)
                cookieManager.flush()
                Log.d("WebView", "Cookie set: $cookieValue")
            }

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(
                    view: WebView?,
                    url: String?,
                    favicon: android.graphics.Bitmap?
                ) {
                    super.onPageStarted(view, url, favicon)
                    Log.d("WebView", "Page started: $url")
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d("WebView", "Page finished: $url")

                    // Inject token into localStorage (for frontend JS)
                    if (accessToken.isNotEmpty()) {
                        val tokenJs = """
                                        try {
                                            localStorage.setItem('access_token', '$accessToken');
                                            console.log('Token injected successfully');
                                        } catch (e) {
                                            console.error('Token injection failed:', e);
                                        }
                                    """.trimIndent()
                        view?.evaluateJavascript(tokenJs) { result ->
                            Log.d("WebView", "LocalStorage injection result: $result")
                        }
                    }

                    // NEW: Inject CSS to hide website navbar and any other unwanted elements
                    val hideNavbarJs = """
                        (function() {
                            try {
                                // Create and inject CSS to hide navbar elements
                                const style = document.createElement('style');
                                style.textContent = `
                                    /* Hide main navigation bar */
                                    nav, .navbar, .nav-bar, .navigation, 
                                    header, .header, .site-header, .main-header,
                                    .top-bar, .topbar, .app-bar, .menu-bar,
                                    [role="navigation"], [data-testid*="nav"],
                                    .nav-container, .navigation-container,
                                    
                                    /* Hide specific elements that look like navbars */
                                    .MuiAppBar-root, .MuiToolbar-root,
                                    .ant-layout-header, .ant-menu-horizontal,
                                    .navbar-nav, .navbar-brand, .nav-tabs,
                                    
                                    /* Hide any fixed positioned elements at top */
                                    [style*="position: fixed"][style*="top: 0"],
                                    [style*="position:fixed"][style*="top:0"],
                                    
                                    /* Hide elements with common navbar classes */
                                    .fixed-top, .sticky-top, .navbar-fixed-top,
                                    .header-container, .site-navigation,
                                    
                                    /* Hide breadcrumbs if they exist */
                                    .breadcrumb, .breadcrumbs, .breadcrumb-container,
                                    
                                    /* Custom selectors for your specific site */
                                    .cc-navbar, .creator-circle-nav, .main-nav {
                                        display: none !important;
                                        visibility: hidden !important;
                                        height: 0 !important;
                                        min-height: 0 !important;
                                        max-height: 0 !important;
                                        overflow: hidden !important;
                                        margin: 0 !important;
                                        padding: 0 !important;
                                    }
                                    
                                    /* Adjust body/main content to fill the space */
                                    body {
                                        padding-top: 0 !important;
                                        margin-top: 0 !important;
                                    }
                                    
                                    main, .main-content, .content, #main, #content,
                                    .page-content, .app-content, .container-fluid {
                                        padding-top: 0 !important;
                                        margin-top: 0 !important;
                                    }
                                    
                                    /* Ensure the remaining content uses full viewport */
                                    .dashboard, .dashboard-container, .page-container {
                                        padding-top: 0 !important;
                                        margin-top: 0 !important;
                                    }
                                `;
                                
                                document.head.appendChild(style);
                                console.log('Navbar hiding CSS injected successfully');
                                
                                // Additional JS to remove navbar elements after DOM is ready
                                setTimeout(() => {
                                    // Remove elements by common selectors
                                    const selectorsToRemove = [
                                        'nav', '.navbar', '.nav-bar', '.navigation',
                                        'header', '.header', '.site-header', '.main-header',
                                        '.top-bar', '.topbar', '.app-bar', '.menu-bar',
                                        '[role="navigation"]',
                                        '.MuiAppBar-root', '.MuiToolbar-root',
                                        '.ant-layout-header',
                                        '.fixed-top', '.sticky-top', '.navbar-fixed-top'
                                    ];
                                    
                                    selectorsToRemove.forEach(selector => {
                                        try {
                                            const elements = document.querySelectorAll(selector);
                                            elements.forEach(el => {
                                                // Check if element is actually a navbar (has typical navbar characteristics)
                                                const rect = el.getBoundingClientRect();
                                                const style = window.getComputedStyle(el);
                                                const isLikelyNavbar = 
                                                    rect.top < 100 || // Near top of page
                                                    style.position === 'fixed' || 
                                                    style.position === 'sticky' ||
                                                    el.querySelector('a[href*="home"], a[href*="connect"], a[href*="dashboard"]') ||
                                                    el.textContent.toLowerCase().includes('home') ||
                                                    el.textContent.toLowerCase().includes('connect') ||
                                                    el.textContent.toLowerCase().includes('menu');
                                                
                                                if (isLikelyNavbar) {
                                                    el.remove();
                                                    console.log('Removed navbar element:', selector);
                                                }
                                            });
                                        } catch (e) {
                                            console.log('Could not remove elements for selector:', selector);
                                        }
                                    });
                                }, 1000);
                                
                            } catch (e) {
                                console.error('Failed to hide navbar:', e);
                            }
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(hideNavbarJs) { result ->
                        Log.d("WebView", "Navbar hiding script result: $result")
                    }
                }

                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e("WebView", "Error: ${error?.description} for ${request?.url}")

                    // FIX 4: Handle errors gracefully
                    if (error?.errorCode == ERROR_TIMEOUT ||
                        error?.errorCode == ERROR_HOST_LOOKUP ||
                        error?.errorCode == ERROR_CONNECT
                    ) {
                        // Retry loading after a delay
                        view?.postDelayed({
                            view.reload()
                        }, 2000)
                    }
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    val url = request?.url.toString()
                    Log.d("WebView", "Should override URL: $url")

                    // Allow all URLs from the same domain
                    return if (url.contains("creatorcircle.in")) {
                        view?.loadUrl(url)
                        false // Changed to false to let WebView handle it
                    } else {
                        // For external URLs, you might want to open in browser
                        false
                    }
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                    Log.d("WebView", "Console: ${consoleMessage?.message()}")
                    return super.onConsoleMessage(consoleMessage)
                }
            }

            settings.apply {
                // Basic settings
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true

                // FIX 5: Additional storage settings
//                setAppCacheEnabled(true)
//                setAppCachePath(context.cacheDir.absolutePath)

                // Layout and viewport
                loadWithOverviewMode = true
                useWideViewPort = true
                layoutAlgorithm = android.webkit.WebSettings.LayoutAlgorithm.NORMAL

                // Zoom settings
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false

                // File and content access
                allowFileAccess = true
                allowContentAccess = true
                allowFileAccessFromFileURLs = false
                allowUniversalAccessFromFileURLs = false

                // FIX 6: Security and compatibility improvements
                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                setSupportMultipleWindows(false)
                javaScriptCanOpenWindowsAutomatically = false

                // FIX 7: Better cache strategy
                cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                // Text settings
                textZoom = 100

                // FIX 8: Enable media playback and other features
                mediaPlaybackRequiresUserGesture = false
                setGeolocationEnabled(false)
                setSaveFormData(false)
                setSavePassword(false)

                // FIX 9: User agent (sometimes helps with compatibility)
                userAgentString = userAgentString + " CreatorCircleApp/1.0"
            }

            // FIX 10: Load URL with proper error handling
            try {
                loadUrl("https://creatorcircle.in/dashboard/profile")
            } catch (e: Exception) {
                Log.e("WebView", "Failed to load URL", e)
            }
        }
    }


    Scaffold(
//        topBar = { TopBar(title = "My Profile", navController) },
        topBar = { TopBarProfile(title = "My Profile", navController, userViewModel = userViewModel) },
        bottomBar = { BottomNavBar(navController = navController) },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            AndroidView(
                factory = { context ->
                    FrameLayout(context).apply {
                        // Remove WebView from previous parent if exists
                        if (myWebView.parent != null) {
                            (myWebView.parent as ViewGroup).removeView(myWebView)
                        }
                        addView(myWebView)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    // Cleanup on disposal
    DisposableEffect(Unit) {
        onDispose {
            myWebView.destroy()
        }
    }
}