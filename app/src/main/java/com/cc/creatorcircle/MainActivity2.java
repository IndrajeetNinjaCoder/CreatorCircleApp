package com.cc.creatorcircle;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

public class MainActivity2 extends AppCompatActivity {

    private WebView webView;
    private String accessToken = "";
//    private String accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpbmRyYTNAZ21haWwuY29tIiwiZXhwIjoxNzYzMjk0ODExfQ.tHU_ZGVpnkYzLsKMuQxFNe4C6ox-Va0TgRb2IDXWxSw";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Get token from Intent
//        accessToken = getIntent().getStringExtra("token");

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
        String cookieValue = "access_token=" + accessToken; // Change key-name if your backend expects a different cookie name
        cookieManager.setCookie("https://creatorcircle.in", cookieValue);
        cookieManager.flush();

        // 2) Inject into LocalStorage (for frontend JS)
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                // Insert access_token into localStorage
                String js = "localStorage.setItem('access_token', '" + accessToken + "');";
                view.evaluateJavascript(js, null);

                super.onPageFinished(view, url);
            }
        });

        // 3) Load your dashboard URL
        webView.loadUrl("https://creatorcircle.in/dashboard");
    }
}





/*

public class MainActivity2 extends AppCompatActivity {
    private WebView webView;
    private SharedPreferences sharedPrefs;
    private boolean authInjected = false; // Flag to prevent repeated auth injections

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        webView = findViewById(R.id.webview);
        sharedPrefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);

        // Enable JavaScript and other settings
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("utf-8");

        // Enable cookies
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // Add JavaScript interface for authentication
        webView.addJavascriptInterface(new WebAppInterface(), "AndroidAuth");

        // Debug: Print stored authentication data
        debugStoredAuthData();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                System.out.println("MainActivity2: Page finished loading: " + url);

                // Check if user is logged in natively
                boolean isLoggedIn = sharedPrefs.getBoolean("is_logged_in", false);
                System.out.println("MainActivity2: Is user logged in natively: " + isLoggedIn);

                // Only inject auth data if user is logged in and we haven't already injected
                if (isLoggedIn && !authInjected) {
                    System.out.println("MainActivity2: First time auth injection needed");
                    authInjected = true; // Set flag to prevent repeated injections

                    // Wait a bit for the page to fully load, then inject auth data
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("MainActivity2: Attempting to inject auth data...");
                            injectAuthDataWithDelay();
                        }
                    }, 2000); // Wait 2 seconds for page to fully load
                } else if (isLoggedIn && authInjected) {
                    System.out.println("MainActivity2: Auth already injected, just triggering state change");
                    // Just trigger auth state change without reloading
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            triggerAuthStateChange();
                        }
                    }, 1000);
                } else {
                    System.out.println("MainActivity2: User not logged in natively, loading normal page");
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                System.out.println("MainActivity2: Page started loading: " + url);
            }
        });

        // Load the website
        webView.loadUrl("https://creatorcircle.in/dashboard");

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView != null && webView.canGoBack()) {
                    webView.goBack();
                } else {
                    // Allow default system behavior
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void injectAuthDataWithDelay() {
        // Get authentication data from SharedPreferences
        String accessToken = "";
        String userInfo = "";

        // First try to get from login_response
        String loginResponse = sharedPrefs.getString("login_response", "");
        if (!loginResponse.isEmpty()) {
            try {
                org.json.JSONObject responseObj = new org.json.JSONObject(loginResponse);
                accessToken = extractAccessToken(responseObj);
                userInfo = extractUserInfo(responseObj);
                System.out.println("MainActivity2: Extracted from login_response - Token: " +
                        (accessToken.isEmpty() ? "NOT_FOUND" : "FOUND") + ", User: " +
                        (userInfo.isEmpty() ? "NOT_FOUND" : "FOUND"));
            } catch (Exception e) {
                System.out.println("MainActivity2: Error parsing login response: " + e.getMessage());
            }
        }

        // Fallback to direct stored values
        if (accessToken.isEmpty()) {
            accessToken = sharedPrefs.getString("access_token", "");
            if (accessToken.isEmpty()) {
                accessToken = sharedPrefs.getString("auth_token", "");
            }
        }

        // If still no token, try form filling fallback
        if (accessToken.isEmpty()) {
            System.out.println("MainActivity2: ❌ No valid auth token found! Trying form filling fallback...");
            fallbackFormFilling();
            return;
        }

        System.out.println("MainActivity2: ✅ Using access token: " +
                accessToken.substring(0, Math.min(20, accessToken.length())) + "...");

        // Create user info if not available
        if (userInfo.isEmpty()) {
            try {
                String email = sharedPrefs.getString("user_email", "");
                String username = sharedPrefs.getString("username", "NativeUser");
                String fullName = sharedPrefs.getString("full_name", "");
                int userId = sharedPrefs.getInt("user_id", 0);

                org.json.JSONObject userObj = new org.json.JSONObject();
                userObj.put("id", userId);
                userObj.put("email", email);
                userObj.put("username", username);
                userObj.put("full_name", fullName);
                userObj.put("is_active", sharedPrefs.getBoolean("is_active", true));

                userInfo = userObj.toString();
                System.out.println("MainActivity2: Created user info from stored data");
            } catch (Exception e) {
                System.out.println("MainActivity2: Error creating user info: " + e.getMessage());
                userInfo = "{\"email\":\"" + sharedPrefs.getString("user_email", "") + "\",\"username\":\"NativeUser\"}";
            }
        }

        // Escape the user info for JavaScript
        String escapedUserInfo = userInfo.replace("\"", "\\\"").replace("'", "\\'")
                .replace("\n", "\\n").replace("\r", "");

        String jsCode =
                "(function() {" +
                        "  try {" +
                        "    console.log('🔄 Starting native auth injection with real token...');" +

                        // Clear any existing auth data first
                        "    console.log('🧹 Clearing existing auth data...');" +
                        "    localStorage.removeItem('access_token');" +
                        "    localStorage.removeItem('user_info');" +
                        "    localStorage.removeItem('auth_token');" +
                        "    sessionStorage.clear();" +

                        // Set the real authentication data
                        "    console.log('🔐 Setting real auth data in localStorage');" +
                        "    localStorage.setItem('access_token', '" + accessToken + "');" +
                        "    localStorage.setItem('user_info', '" + escapedUserInfo + "');" +
                        "    localStorage.setItem('auth_token', '" + accessToken + "');" +
                        "    localStorage.setItem('token_type', 'Bearer');" +

                        // Set additional data that the website might need
                        "    localStorage.setItem('is_authenticated', 'true');" +
                        "    localStorage.setItem('login_timestamp', Date.now().toString());" +

                        "    console.log('✅ Real auth data set in localStorage');" +
                        "    console.log('🔑 Token preview:', localStorage.getItem('access_token').substring(0, 20) + '...');" +
                        "    console.log('👤 User info set:', !!localStorage.getItem('user_info'));" +

                        // Dispatch custom events that the React app might listen for
                        "    window.dispatchEvent(new CustomEvent('nativeAuthInjected', {" +
                        "      detail: { " +
                        "        source: 'native', " +
                        "        authenticated: true, " +
                        "        timestamp: Date.now() " +
                        "      }" +
                        "    }));" +

                        // Trigger any auth state change functions if they exist
                        "    if (typeof window.updateAuthState === 'function') {" +
                        "      console.log('📢 Calling window.updateAuthState()');" +
                        "      window.updateAuthState(true);" +
                        "    }" +

                        // Try to trigger React state updates
                        "    if (typeof window.refreshAuthState === 'function') {" +
                        "      console.log('🔄 Calling window.refreshAuthState()');" +
                        "      window.refreshAuthState();" +
                        "    }" +

                        // Force page refresh if on login page
                        "    if (window.location.pathname.includes('login') || window.location.pathname.includes('signin')) {" +
                        "      console.log('📍 On login page, redirecting to dashboard...');" +
                        "      setTimeout(() => {" +
                        "        window.location.href = '/dashboard';" +
                        "      }, 500);" +
                        "    } else {" +
                        "      console.log('✅ Already on correct page, triggering state refresh...');" +
                        "      // Force a re-render by updating a dummy localStorage value" +
                        "      localStorage.setItem('auth_refresh_trigger', Date.now().toString());" +
                        "    }" +

                        "    return 'success';" +
                        "  } catch (error) {" +
                        "    console.error('❌ Error in auth injection:', error);" +
                        "    return 'error: ' + error.message;" +
                        "  }" +
                        "})();";

        System.out.println("MainActivity2: Executing auth injection JavaScript...");

        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String result) {
                System.out.println("MainActivity2: JavaScript result: " + result);

                // If localStorage injection worked, we're done
                if (result != null && result.contains("success")) {
                    System.out.println("MainActivity2: ✅ Auth injection successful");

                    // Also try to trigger any additional authentication state changes
                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            triggerAuthStateChange();
                        }
                    }, 1000);
                } else {
                    System.out.println("MainActivity2: ❌ Auth injection failed, trying fallback");
                    // Fallback to form filling if localStorage injection failed
                    fallbackFormFilling();
                }
            }
        });
    }

    private void triggerAuthStateChange() {
        // Try to trigger any authentication state changes in the React app
        String jsCode =
                "(function() {" +
                        "  try {" +
                        "    console.log('🔄 Triggering additional auth state changes...');" +

                        // Dispatch multiple custom events that the React app might listen for
                        "    const authEvents = ['authStateChanged', 'userLoggedIn', 'tokenUpdated', 'nativeAuthComplete'];" +
                        "    authEvents.forEach(eventName => {" +
                        "      window.dispatchEvent(new CustomEvent(eventName, {" +
                        "        detail: { source: 'native', authenticated: true, timestamp: Date.now() }" +
                        "      }));" +
                        "    });" +

                        // Try to call any global auth functions if they exist
                        "    const authFunctions = ['updateAuthState', 'refreshUser', 'checkAuthStatus', 'onAuthChange'];" +
                        "    authFunctions.forEach(funcName => {" +
                        "      if (typeof window[funcName] === 'function') {" +
                        "        console.log('📢 Calling window.' + funcName + '()');" +
                        "        try { window[funcName](true); } catch(e) { console.log('Error calling ' + funcName + ':', e); }" +
                        "      }" +
                        "    });" +

                        // Force re-render by changing a dummy localStorage item
                        "    localStorage.setItem('auth_trigger', Date.now().toString());" +
                        "    localStorage.setItem('user_session', 'active');" +

                        // Try to force React hooks to re-run by dispatching storage events
                        "    window.dispatchEvent(new StorageEvent('storage', {" +
                        "      key: 'access_token'," +
                        "      newValue: localStorage.getItem('access_token')," +
                        "      storageArea: localStorage" +
                        "    }));" +

                        "    console.log('✅ Auth state change triggered successfully');" +
                        "    return 'triggered';" +
                        "  } catch (error) {" +
                        "    console.error('❌ Error triggering auth state:', error);" +
                        "    return 'error: ' + error.message;" +
                        "  }" +
                        "})();";

        webView.evaluateJavascript(jsCode, null);
    }

    private void debugStoredAuthData() {
        System.out.println("MainActivity2: === DEBUGGING STORED AUTH DATA ===");
        System.out.println("MainActivity2: is_logged_in: " + sharedPrefs.getBoolean("is_logged_in", false));
        System.out.println("MainActivity2: user_email: " + sharedPrefs.getString("user_email", "NOT_SET"));
        System.out.println("MainActivity2: access_token: " + (sharedPrefs.getString("access_token", "").isEmpty() ? "NOT_SET" : "SET"));
        System.out.println("MainActivity2: auth_token: " + (sharedPrefs.getString("auth_token", "").isEmpty() ? "NOT_SET" : "SET"));

        String loginResponse = sharedPrefs.getString("login_response", "");
        if (!loginResponse.isEmpty()) {
            System.out.println("MainActivity2: login_response: " + loginResponse.substring(0, Math.min(200, loginResponse.length())) + "...");
            try {
                org.json.JSONObject responseObj = new org.json.JSONObject(loginResponse);
                String extractedToken = extractAccessToken(responseObj);
                String extractedUser = extractUserInfo(responseObj);
                System.out.println("MainActivity2: extracted access_token: " + (extractedToken.isEmpty() ? "NOT_FOUND" : "FOUND"));
                System.out.println("MainActivity2: extracted user_info: " + (extractedUser.isEmpty() ? "NOT_FOUND" : "FOUND"));
            } catch (Exception e) {
                System.out.println("MainActivity2: Error parsing login response: " + e.getMessage());
            }
        } else {
            System.out.println("MainActivity2: login_response: NOT_SET");
        }
        System.out.println("MainActivity2: ================================");
    }

    private void fallbackFormFilling() {
        System.out.println("MainActivity2: Attempting fallback form filling...");

        String email = sharedPrefs.getString("user_email", "");
        String password = sharedPrefs.getString("user_password", "");

        if (email.isEmpty() || password.isEmpty()) {
            System.out.println("MainActivity2: No credentials available for form filling");
            return;
        }

        // More robust form filling approach
        String jsCode =
                "(function() {" +
                        "  try {" +
                        "    console.log('🔄 Attempting form filling fallback...');" +

                        "    // Wait for form to be ready" +
                        "    var attempts = 0;" +
                        "    var maxAttempts = 10;" +

                        "    function fillForm() {" +
                        "      attempts++;" +
                        "      console.log('🔍 Form fill attempt:', attempts);" +

                        "      var emailField = document.querySelector(" +
                        "        'input[type=\"email\"], input[name=\"email\"], input[id*=\"email\"], " +
                        "        input[placeholder*=\"email\" i], input[placeholder*=\"Email\"]'" +
                        "      );" +

                        "      var passwordField = document.querySelector(" +
                        "        'input[type=\"password\"], input[name=\"password\"], input[id*=\"password\"], " +
                        "        input[placeholder*=\"password\" i], input[placeholder*=\"Password\"]'" +
                        "      );" +

                        "      var submitButton = document.querySelector(" +
                        "        'button[type=\"submit\"], input[type=\"submit\"], " +
                        "        button:contains(\"Login\"), button:contains(\"Sign in\"), button:contains(\"Log in\"), " +
                        "        .login-btn, #login-btn, .btn-login, [class*=\"login\"][role=\"button\"]'" +
                        "      );" +

                        "      console.log('📋 Found fields:', {" +
                        "        email: !!emailField," +
                        "        password: !!passwordField," +
                        "        submit: !!submitButton" +
                        "      });" +

                        "      if (emailField && passwordField) {" +
                        "        emailField.value = '" + email + "';" +
                        "        passwordField.value = '" + password + "';" +

                        "        // Trigger all possible events" +
                        "        ['input', 'change', 'keyup', 'blur'].forEach(eventType => {" +
                        "          emailField.dispatchEvent(new Event(eventType, {bubbles: true, cancelable: true}));" +
                        "          passwordField.dispatchEvent(new Event(eventType, {bubbles: true, cancelable: true}));" +
                        "        });" +

                        "        console.log('✅ Form fields filled');" +

                        "        if (submitButton) {" +
                        "          setTimeout(() => {" +
                        "            submitButton.click();" +
                        "            console.log('🚀 Submit button clicked');" +
                        "          }, 500);" +
                        "        } else {" +
                        "          // Try to find and trigger form submission" +
                        "          var form = emailField.closest('form');" +
                        "          if (form) {" +
                        "            setTimeout(() => {" +
                        "              form.submit();" +
                        "              console.log('📤 Form submitted directly');" +
                        "            }, 500);" +
                        "          }" +
                        "        }" +

                        "        return 'filled';" +
                        "      } else if (attempts < maxAttempts) {" +
                        "        setTimeout(fillForm, 1000);" +
                        "        return 'retrying';" +
                        "      } else {" +
                        "        console.log('❌ Could not find login form after', maxAttempts, 'attempts');" +
                        "        return 'failed';" +
                        "      }" +
                        "    }" +

                        "    return fillForm();" +

                        "  } catch (error) {" +
                        "    console.error('❌ Form filling error:', error);" +
                        "    return 'error: ' + error.message;" +
                        "  }" +
                        "})();";

        webView.evaluateJavascript(jsCode, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String result) {
                System.out.println("MainActivity2: Form filling result: " + result);
            }
        });
    }

    // Helper method to extract access token from JSON response
    private String extractAccessToken(org.json.JSONObject responseObj) {
        try {
            // Try different possible keys for access token
            if (responseObj.has("access_token")) {
                return responseObj.getString("access_token");
            } else if (responseObj.has("accessToken")) {
                return responseObj.getString("accessToken");
            } else if (responseObj.has("token")) {
                return responseObj.getString("token");
            } else if (responseObj.has("data")) {
                org.json.JSONObject dataObj = responseObj.getJSONObject("data");
                if (dataObj.has("access_token")) {
                    return dataObj.getString("access_token");
                } else if (dataObj.has("accessToken")) {
                    return dataObj.getString("accessToken");
                } else if (dataObj.has("token")) {
                    return dataObj.getString("token");
                }
            }
            return "";
        } catch (Exception e) {
            System.out.println("Error extracting access token: " + e.getMessage());
            return "";
        }
    }

    // Helper method to extract user info from JSON response
    private String extractUserInfo(org.json.JSONObject responseObj) {
        try {
            // Try different possible keys for user info
            if (responseObj.has("user")) {
                return responseObj.getJSONObject("user").toString();
            } else if (responseObj.has("user_info")) {
                return responseObj.getJSONObject("user_info").toString();
            } else if (responseObj.has("userInfo")) {
                return responseObj.getJSONObject("userInfo").toString();
            } else if (responseObj.has("data")) {
                org.json.JSONObject dataObj = responseObj.getJSONObject("data");
                if (dataObj.has("user")) {
                    return dataObj.getJSONObject("user").toString();
                } else if (dataObj.has("user_info")) {
                    return dataObj.getJSONObject("user_info").toString();
                } else if (dataObj.has("userInfo")) {
                    return dataObj.getJSONObject("userInfo").toString();
                }
            }
            return "";
        } catch (Exception e) {
            System.out.println("Error extracting user info: " + e.getMessage());
            return "";
        }
    }

    public class WebAppInterface {
        @JavascriptInterface
        public String getAuthToken() {
            return sharedPrefs.getString("auth_token", "");
        }

        @JavascriptInterface
        public String getAccessToken() {
            // Try to extract access_token from stored response
            String loginResponse = sharedPrefs.getString("login_response", "");
            if (!loginResponse.isEmpty()) {
                try {
                    org.json.JSONObject responseObj = new org.json.JSONObject(loginResponse);
                    String token = extractAccessToken(responseObj);
                    if (!token.isEmpty()) {
                        return token;
                    }
                } catch (Exception e) {
                    System.out.println("WebAppInterface: Error extracting access token: " + e.getMessage());
                }
            }
            // Fallback to direct stored access token
            return sharedPrefs.getString("access_token", "");
        }

        @JavascriptInterface
        public String getUserInfo() {
            // Try to extract user_info from stored response
            String loginResponse = sharedPrefs.getString("login_response", "");
            if (!loginResponse.isEmpty()) {
                try {
                    org.json.JSONObject responseObj = new org.json.JSONObject(loginResponse);
                    String userInfo = extractUserInfo(responseObj);
                    if (!userInfo.isEmpty()) {
                        return userInfo;
                    }
                } catch (Exception e) {
                    System.out.println("WebAppInterface: Error extracting user info: " + e.getMessage());
                }
            }
            // Fallback to create user info from stored data
            try {
                org.json.JSONObject userObj = new org.json.JSONObject();
                userObj.put("id", sharedPrefs.getInt("user_id", 0));
                userObj.put("email", sharedPrefs.getString("user_email", ""));
                userObj.put("username", sharedPrefs.getString("username", ""));
                userObj.put("full_name", sharedPrefs.getString("full_name", ""));
                userObj.put("is_active", sharedPrefs.getBoolean("is_active", true));
                return userObj.toString();
            } catch (Exception e) {
                return "{}";
            }
        }

        @JavascriptInterface
        public String getUserEmail() {
            return sharedPrefs.getString("user_email", "");
        }

        @JavascriptInterface
        public boolean isLoggedIn() {
            return sharedPrefs.getBoolean("is_logged_in", false);
        }

        @JavascriptInterface
        public String getAuthType() {
            return sharedPrefs.getString("auth_type", "email");
        }

        @JavascriptInterface
        public void showToast(String message) {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity2.this, message, Toast.LENGTH_SHORT).show();
            });
        }

        @JavascriptInterface
        public String getLoginResponse() {
            return sharedPrefs.getString("login_response", "");
        }

        @JavascriptInterface
        public void clearAuthData() {
            sharedPrefs.edit().clear().apply();
            authInjected = false;
            System.out.println("WebAppInterface: Auth data cleared");
        }

        @JavascriptInterface
        public void refreshWebView() {
            runOnUiThread(() -> {
                webView.reload();
            });
        }

        @JavascriptInterface
        public String getStoredData(String key) {
            return sharedPrefs.getString(key, "");
        }
    }
}


 */