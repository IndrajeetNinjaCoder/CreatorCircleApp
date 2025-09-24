package com.cc.creatorcircle.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.cc.creatorcircle.R

// Lexend Font Family
val LexendFontFamily = FontFamily(
    Font(R.font.font_lexend)
)

// Extension function to apply Lexend to all typography styles
private fun Typography.withLexendFont(): Typography {
    return this.copy(
        displayLarge = displayLarge.copy(fontFamily = LexendFontFamily),
        displayMedium = displayMedium.copy(fontFamily = LexendFontFamily),
        displaySmall = displaySmall.copy(fontFamily = LexendFontFamily),
        headlineLarge = headlineLarge.copy(fontFamily = LexendFontFamily),
        headlineMedium = headlineMedium.copy(fontFamily = LexendFontFamily),
        headlineSmall = headlineSmall.copy(fontFamily = LexendFontFamily),
        titleLarge = titleLarge.copy(fontFamily = LexendFontFamily),
        titleMedium = titleMedium.copy(fontFamily = LexendFontFamily),
        titleSmall = titleSmall.copy(fontFamily = LexendFontFamily),
        bodyLarge = bodyLarge.copy(fontFamily = LexendFontFamily),
        bodyMedium = bodyMedium.copy(fontFamily = LexendFontFamily),
        bodySmall = bodySmall.copy(fontFamily = LexendFontFamily),
        labelLarge = labelLarge.copy(fontFamily = LexendFontFamily),
        labelMedium = labelMedium.copy(fontFamily = LexendFontFamily),
        labelSmall = labelSmall.copy(fontFamily = LexendFontFamily),
    )
}

// Typography with Lexend font applied
val LexendTypography = Typography().withLexendFont()

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun CreatorCircleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LexendTypography, // Using Lexend typography instead of default
//        typography = AppTypography, // Using Lexend typography instead of default
        content = content
    )
}






val Neuton = FontFamily(
    Font(R.font.neuton_extralight, FontWeight.ExtraLight),
    Font(R.font.neuton_light, FontWeight.Light),
    Font(R.font.neuton_regular, FontWeight.Normal),
    Font(R.font.neuton_italic, FontWeight.Normal), // italic style
    Font(R.font.neuton_bold, FontWeight.Bold),
    Font(R.font.neuton_extrabold, FontWeight.ExtraBold)
)








//package com.cc.creatorcircle.ui.theme
//
//import android.os.Build
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.dynamicDarkColorScheme
//import androidx.compose.material3.dynamicLightColorScheme
//import androidx.compose.material3.lightColorScheme
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.platform.LocalContext
//
//private val DarkColorScheme = darkColorScheme(
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
//)
//
//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)
//
//@Composable
//fun CreatorCircleTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
//    content: @Composable () -> Unit
//) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
//
//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
//}