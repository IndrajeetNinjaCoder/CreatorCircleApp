package com.cc.creatorcircle.ui.screens.mentor_circle


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
import androidx.navigation.NavController
import com.cc.creatorcircle.ui.components.BottomNavBar
import com.cc.creatorcircle.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSession(navController: NavController) {
    val context = LocalContext.current

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

                    val hideNavbarJs = """
                        (function() {
                            try {
                                // Create and inject CSS to hide navbar but preserve hamburger menu
                                const style = document.createElement('style');
                                style.textContent = `
                                    /* Hide the main website navbar */
                                    .main-navbar, .site-header, .top-navigation,
                                    .fixed-header, .sticky-header, .app-header,
                                    nav.navbar, header.navbar, .navbar.navbar-fixed-top,
                                    .navbar.fixed-top, .navbar-default,
                                    .MuiAppBar-root.MuiAppBar-positionFixed,
                                    .MuiToolbar-root:first-child,
                                    header:first-child, nav:first-child,
                                    .cc-main-navbar, .creator-circle-header {
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
                                    
                                    /* CRITICAL: Ensure hamburger menu and ALL its functionality stays visible */
                                    .hamburger, .hamburger-menu, .menu-hamburger,
                                    [class*="hamburger"], .menu-toggle, .navbar-toggler,
                                    .mobile-menu-toggle, .sidebar-toggle,
                                    
                                    /* Dropdown menus and menu items */
                                    .dropdown-menu, .menu-dropdown, .dropdown-content,
                                    .mobile-menu, .nav-dropdown, .menu-items,
                                    .dropdown, .menu-list, .sidebar-menu,
                                    
                                    /* Individual menu items */
                                    .menu-item, .nav-item, .dropdown-item,
                                    .sidebar-item, .menu-link, .nav-link,
                                    
                                    /* Role-based selectors */
                                    [role="menu"], [role="menubar"], [role="menuitem"],
                                    [role="button"][aria-expanded], [role="button"][aria-controls],
                                    
                                    /* Any element that's part of hamburger functionality */
                                    [class*="menu"]:not([class*="navbar"]),
                                    [class*="dropdown"]:not([class*="navbar"]),
                                    [aria-controls*="menu"], [aria-controls*="dropdown"] {
                                        display: block !important;
                                        visibility: visible !important;
                                        height: auto !important;
                                        max-height: none !important;
                                        overflow: visible !important;
                                        opacity: 1 !important;
                                        position: relative !important;
                                        z-index: 9999 !important;
                                        pointer-events: auto !important;
                                    }
                                    
                                    /* Special handling for dropdown positioning */
                                    .dropdown-menu, .menu-dropdown, .mobile-menu, .sidebar-menu {
                                        position: absolute !important;
                                        z-index: 10000 !important;
                                    }
                                    
                                    /* Ensure hamburger icon lines/bars are visible */
                                    .hamburger span, .hamburger-menu span, .menu-toggle span,
                                    .hamburger::before, .hamburger::after,
                                    .hamburger-menu::before, .hamburger-menu::after {
                                        display: block !important;
                                        visibility: visible !important;
                                    }
                                    
                                    /* Make sure containers don't hide overflow */
                                    .main-content, .page-content, .dashboard,
                                    .container, .content-wrapper {
                                        overflow: visible !important;
                                    }
                                `;
                                
                                document.head.appendChild(style);
                                console.log('Navbar hiding CSS with hamburger preservation injected');
                                
                                // Wait for content to load, then do targeted navbar removal
                                setTimeout(() => {
                                    console.log('Starting targeted navbar removal...');
                                    
                                    // Find and remove ONLY the main navbar, not hamburger elements
                                    const potentialNavbars = document.querySelectorAll('header, nav, .navbar');
                                    
                                    potentialNavbars.forEach((element, index) => {
                                        try {
                                            const rect = element.getBoundingClientRect();
                                            const hasHamburger = element.querySelector('[class*="hamburger"], .menu-toggle, [class*="menu-toggle"]');
                                            const isAtTop = rect.top <= 150; // Within 150px of top
                                            const isWide = rect.width > window.innerWidth * 0.7; // Spans most of width
                                            const isNavbar = element.tagName === 'NAV' || 
                                                           element.classList.contains('navbar') ||
                                                           element.tagName === 'HEADER';
                                            
                                            // Only remove if it's clearly a navbar but DOESN'T contain hamburger menu
                                            if (isNavbar && isAtTop && isWide && !hasHamburger) {
                                                console.log('Removing navbar element:', element.className || element.tagName);
                                                element.remove();
                                            } else if (hasHamburger) {
                                                console.log('Preserving element with hamburger:', element.className || element.tagName);
                                            }
                                        } catch (e) {
                                            console.log('Error processing navbar element:', e);
                                        }
                                    });
                                    
                                    // Ensure all hamburger-related elements are visible and functional
                                    const hamburgerSelectors = [
                                        '[class*="hamburger"]',
                                        '.menu-toggle',
                                        '.navbar-toggler', 
                                        '.mobile-menu-toggle',
                                        '[aria-controls*="menu"]',
                                        '[aria-controls*="dropdown"]'
                                    ];
                                    
                                    hamburgerSelectors.forEach(selector => {
                                        try {
                                            const elements = document.querySelectorAll(selector);
                                            elements.forEach(el => {
                                                el.style.display = 'block';
                                                el.style.visibility = 'visible';
                                                el.style.opacity = '1';
                                                el.style.pointerEvents = 'auto';
                                                el.style.zIndex = '9999';
                                            });
                                         
                                        } catch (e) {
                                            console.log('Error ensuring hamburger visibility:', selector, e);
                                        }
                                    });
                                    
                                    // Force visibility of menu items (they might be hidden by default)
                                    setTimeout(() => {
                                        const menuItemSelectors = [
                                            '.menu-item', '.nav-item', '.dropdown-item',
                                            '.sidebar-item', '[role="menuitem"]',
                                            '.dropdown-menu > *', '.mobile-menu > *',
                                            '[class*="menu"] > li', '[class*="menu"] > a'
                                        ];
                                        
                                        menuItemSelectors.forEach(selector => {
                                            try {
                                                const items = document.querySelectorAll(selector);
                                                items.forEach(item => {
                                                    item.style.display = 'block';
                                                    item.style.visibility = 'visible';
                                                    item.style.opacity = '1';
                                                });
                                   
                                            } catch (e) {
                                                console.log('Error making menu items visible:', selector, e);
                                            }
                                        });
                                    }, 1000);
                                    
                                }, 1500); // Wait for initial load
                                
                            } catch (e) {
                                console.error('Failed to execute navbar hiding script:', e);
                            }
                        })();
                    """.trimIndent()

                    view?.evaluateJavascript(hideNavbarJs) { result ->
                        Log.d("WebView", "Navbar hiding script result: $result")
                    }

                    // Debug script to monitor hamburger menu functionality
                    val debugJs = """
                        setTimeout(() => {
                            console.log('=== HAMBURGER MENU DEBUG ===');
                            const hamburgers = document.querySelectorAll('[class*="hamburger"], .menu-toggle');
                            console.log('Found hamburger elements:', hamburgers.length);
                            
                            const menuItems = document.querySelectorAll('.menu-item, .nav-item, .dropdown-item');
                            console.log('Found menu items:', menuItems.length);
                            
                            menuItems.forEach((item, i) => {
                                const styles = window.getComputedStyle(item);
                            });
                        }, 3000);
                    """.trimIndent()

                    view?.evaluateJavascript(debugJs) { result ->
                        Log.d("WebView", "Debug script result: $result")
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
                loadUrl("https://creatorcircle.in/dashboard/live-session")
            } catch (e: Exception) {
                Log.e("WebView", "Failed to load URL", e)
            }
        }
    }

    Scaffold(
        topBar = { TopBar(title = "Mentor Circle", navController) },
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
