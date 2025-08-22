//package com.cc.creatorcircle;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.webkit.CookieManager;
//import android.webkit.WebSettings;
//import android.webkit.WebView;
//import android.webkit.WebViewClient;
//
//import androidx.activity.EdgeToEdge;
//import androidx.activity.OnBackPressedCallback;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//
//public class MainActivity2 extends AppCompatActivity {
//
//    private WebView webView;
//    private String accessToken = "";
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Enable edge-to-edge layout (let us control insets ourselves)
//        EdgeToEdge.enable(this);
//
//        setContentView(R.layout.activity_main2);
//
//        // Apply safe-area padding dynamically
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
//            return insets;
//        });
//
//        // Get token from SharedPreferences
//        SharedPreferences prefs = getSharedPreferences("CCPrefs", MODE_PRIVATE);
//        accessToken = prefs.getString("access_token", "");
//
//        webView = findViewById(R.id.webview);
//
//        // Enable JavaScript & LocalStorage
//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setDomStorageEnabled(true);
//
//        // 1) Set Cookie (for backend)
//        CookieManager cookieManager = CookieManager.getInstance();
//        cookieManager.setAcceptCookie(true);
//        String cookieValue = "access_token=" + accessToken;
//        cookieManager.setCookie("https://creatorcircle.in", cookieValue);
//        cookieManager.flush();
//
//        // 2) Inject into LocalStorage (for frontend JS)
//        webView.setWebViewClient(new WebViewClient() {
//            @Override
//            public void onPageFinished(WebView view, String url) {
//                String js = "localStorage.setItem('access_token', '" + accessToken + "');";
//                view.evaluateJavascript(js, null);
//                super.onPageFinished(view, url);
//            }
//        });
//
//        // 3) Load your dashboard URL
//        webView.loadUrl("https://creatorcircle.in/dashboard");
//    }
//}











package com.cc.creatorcircle;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity2 extends AppCompatActivity {

    private WebView webView;
    private String accessToken = "";
    private static final String HOME_URL = "https://creatorcircle.in/dashboard";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge layout (let us control insets ourselves)
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main2);

        // Apply safe-area padding dynamically
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(0, systemBars.top, 0, systemBars.bottom);
            return insets;
        });

        // Get token from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("CCPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", "");

        webView = findViewById(R.id.webview);

        // Enable JavaScript & LocalStorage
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // 1) Set Cookie (for backend)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        String cookieValue = "access_token=" + accessToken;
        cookieManager.setCookie("https://creatorcircle.in", cookieValue);
        cookieManager.flush();

        // 2) Inject into LocalStorage (for frontend JS)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                String js = "localStorage.setItem('access_token', '" + accessToken + "');";
                view.evaluateJavascript(js, null);
                super.onPageFinished(view, url);
            }
        });

        // 3) Load your dashboard URL (home page)
        webView.loadUrl(HOME_URL);

        // Handle back button press with modern approach
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Check if WebView can go back in its own history
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // If no more pages in WebView history, close the app
                    finishAffinity();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}

