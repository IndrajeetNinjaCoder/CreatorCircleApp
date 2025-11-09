plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
//    id("com.android.application")
//    id("com.google.gms.google-services")

    kotlin("plugin.serialization") version "1.9.0"
}


android {
    namespace = "com.cc.creatorcircle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cc.creatorcircle"
        minSdk = 29
        targetSdk = 36
        versionCode = 19
        versionName = "1.19"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}



dependencies {

//    implementation("com.appsflyer:af-android-sdk:6.12.1")
//    implementation("com.android.installreferrer:installreferrer:2.2")

    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Socket.IO Client
    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation(libs.androidx.appcompat)

    // Lifecycle
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Jetpack Compose
    implementation(libs.androidx.activity.compose)
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
    implementation(libs.androidx.material3)
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.compose.material:material:1.6.0")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended:1.6.0")
    implementation("androidx.compose.foundation:foundation:1.6.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
    implementation("androidx.compose.ui:ui-viewbinding:1.9.0")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Media3 ExoPlayer
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.2.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // WebSocket & Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.3")
    implementation("org.json:json:20230227")

    // Retrofit & Gson
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")

    // Google Services
    implementation("com.google.android.gms:play-services-auth:21.2.0")

    // WebView
    implementation("androidx.webkit:webkit:1.9.0")

    // System UI Controller
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")

    // Pager
    implementation("com.google.accompanist:accompanist-pager:0.34.0")
    implementation("com.google.accompanist:accompanist-pager-indicators:0.34.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.2.2")
    implementation(libs.ui)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-tooling:1.6.0")
    debugImplementation(libs.androidx.ui.test.manifest)
}



//dependencies {
//
////    implementation("com.appsflyer:af-android-sdk:6.12.1")
//// Add install referrer if needed
////    implementation("com.android.installreferrer:installreferrer:2.2")
//
//
//    // Socket.IO Client
//    implementation("io.socket:socket.io-client:2.1.0") {
//        exclude(group = "org.json", module = "json")
//    }
//
//    // Jetpack Compose
//    implementation("androidx.compose.ui:ui:1.5.4")
//    implementation("androidx.compose.material3:material3:1.1.2")
//    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
//    implementation("androidx.activity:activity-compose:1.8.1")
//
//    // Lifecycle
//    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
//
//
//
//    // JSON handling - Note: org.json is included in Android SDK
//
//    // Core Android
//    implementation("androidx.core:core-ktx:1.12.0")
//    implementation(libs.androidx.ui.test)
//
//    debugImplementation("androidx.compose.ui:ui-tooling:1.5.4")
//
//
//
//
//
//
//    // WebSocket support
//    implementation("com.squareup.okhttp3:okhttp:4.12.0")
//    implementation("org.java-websocket:Java-WebSocket:1.5.3")
//
//    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
//
//
//
//
//    implementation("androidx.compose.ui:ui")
//    implementation("androidx.compose.ui:ui-tooling-preview")
//
//    implementation("androidx.compose.material3:material3")
//    implementation("androidx.compose.material:material-icons-core")
//    implementation("androidx.compose.material:material-icons-extended")
//    implementation("androidx.activity:activity-compose:1.8.2")
//    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
//
//    // Media3 ExoPlayer - Latest stable version
//    implementation("androidx.media3:media3-exoplayer:1.2.1")
//    implementation("androidx.media3:media3-ui:1.2.1")
//    implementation("androidx.media3:media3-common:1.2.1")
//
//
//// Coroutines
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
//
//
//
//
//
//
//
//
//    implementation("androidx.compose.runtime:runtime-livedata:1.5.4")
//
//
//
//
//    implementation("androidx.media3:media3-exoplayer:1.2.1")
//    implementation("androidx.media3:media3-ui:1.2.1")
//    implementation("androidx.media3:media3-common:1.2.1")
//
//
//
//
//
//
//
//    implementation("androidx.activity:activity-compose:1.8.2")
//    implementation("androidx.compose.ui:ui-viewbinding:1.9.0")
//
//    implementation("com.squareup.okhttp3:okhttp:4.11.0")
//    implementation("org.json:json:20230227")
//
//
//    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
//    implementation("com.google.firebase:firebase-analytics")
//
//
//    implementation("com.google.firebase:firebase-auth-ktx:22.3.1")
//    implementation("com.google.android.gms:play-services-auth:21.2.0")
//
//
//
//
//
//    // WebView support
//    implementation("androidx.webkit:webkit:1.9.0")
//
//    // Navigation (if you want to add multiple screens later)
//    implementation("androidx.navigation:navigation-compose:2.7.6")
//
//    // System UI Controller for status bar
//    implementation("com.google.accompanist:accompanist-systemuicontroller:0.32.0")
//
//    implementation("androidx.activity:activity-compose:1.8.0")
//    implementation("androidx.compose.ui:ui:1.6.0")
//    implementation("androidx.compose.material:material:1.6.0")
//    implementation("androidx.compose.ui:ui-tooling-preview:1.6.0")
//
//
//    implementation("androidx.compose.foundation:foundation:1.6.0")
//
//    implementation("androidx.datastore:datastore-preferences:1.0.0")
//
//
//
//
//
//
//
//
//
//
//
//
//
//    implementation("com.google.android.gms:play-services-auth:21.2.0") // Google Sign-In
//
//    implementation("androidx.compose.material:material-icons-extended:1.6.0")
//    implementation("androidx.navigation:navigation-compose:2.7.7")
//
//
//
//
//
//
//
//
//    implementation("io.coil-kt:coil-compose:2.2.2") // For image loading
//
//    implementation("androidx.compose.material:material-icons-extended:1.5.0")
//
//    // Retrofit & Gson
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//    implementation("com.google.code.gson:gson:2.10.1")
//    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
//
//    // Jetpack Compose ViewModel integration
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")
//
//
//    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.6.2")
//
//
//    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
//
//    implementation("androidx.navigation:navigation-compose:2.7.7")
//
//    implementation("androidx.media3:media3-exoplayer:1.3.1")
//    implementation("androidx.media3:media3-ui:1.3.1")
//
//    // For pager
//    implementation("com.google.accompanist:accompanist-pager:0.31.5-beta")
//    implementation("com.google.accompanist:accompanist-pager-indicators:0.31.5-beta")
//
//    implementation("com.google.accompanist:accompanist-pager:0.34.0") // Or latest
//    implementation("com.google.accompanist:accompanist-pager-indicators:0.34.0")
//
//
//
//    implementation("com.squareup.retrofit2:retrofit:2.9.0")
//
//
//    implementation("androidx.compose.material3:material3:1.3.0") // or latest stable
//
//
//
//
//
//
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.lifecycle.runtime.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    implementation(libs.androidx.appcompat)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
//    androidTestImplementation(platform(libs.androidx.compose.bom))
//    androidTestImplementation(libs.androidx.ui.test.junit4)
//    debugImplementation(libs.androidx.ui.tooling)
//    debugImplementation(libs.androidx.ui.test.manifest)
//}