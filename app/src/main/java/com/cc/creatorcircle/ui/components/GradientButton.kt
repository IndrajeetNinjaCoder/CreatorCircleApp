package com.cc.creatorcircle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GradientButton(
    text: String,
    modifier: Modifier = Modifier,
//    gradientColors: List<Color> = listOf(Color(0xFF9C27B0), Color(0xFFE91E63)), // purple → pink
    gradientColors: List<Color> = listOf(Color(0xFF893BCF), Color(0xFFEA3BA1)), // purple → pink
    cornerRadius: Int = 8,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius.dp)

    // Define colors based on enabled state
    val backgroundColors = if (enabled) {
        gradientColors
    } else {
        listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f))
    }

    val textColor = if (enabled) Color.White else Color.White.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .height(40.dp)
//            .widthIn(min = 120.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(backgroundColors),
                shape = shape
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}



@Composable
fun GradientIconButton(
    text: String,
    icon: Int,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(Color(0xFF893BCF), Color(0xFFEA3BA1)), // purple → pink
    cornerRadius: Int = 8,
    enabled: Boolean = true,
    iconTint: Color = Color.White,
    spacing: Int = 8,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius.dp)

    // Define colors based on enabled state
    val backgroundColors = if (enabled) {
        gradientColors
    } else {
        listOf(Color.Gray.copy(alpha = 0.5f), Color.Gray.copy(alpha = 0.5f))
    }

    val textColor = if (enabled) Color.White else Color.White.copy(alpha = 0.6f)
    val finalIconTint = if (enabled) iconTint else iconTint.copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .background(
                brush = Brush.horizontalGradient(backgroundColors),
                shape = shape
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = Color.White.copy(alpha = 0.3f)),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = finalIconTint,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(spacing.dp))
            Text(
                text = text,
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}



@Composable
fun CustomOutlinedButton(
    text: String,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFFB787F5),
    textColor: Color = Color(0xFFB787F5),
    cornerRadius: Int = 8,
    borderWidth: Dp = 1.dp,
    fontSize: TextUnit = 12.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val shape = RoundedCornerShape(cornerRadius.dp)

    // Define colors based on enabled state
    val finalBorderColor = if (enabled) borderColor else borderColor.copy(alpha = 0.5f)
    val finalTextColor = if (enabled) textColor else textColor.copy(alpha = 0.5f)

    Box(
        modifier = modifier
            .height(40.dp)
            .clip(shape)
            .border(
                width = borderWidth,
                color = finalBorderColor,
                shape = shape
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = ripple(color = borderColor.copy(alpha = 0.1f)),
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = finalTextColor,
            fontSize = fontSize,
            fontWeight = fontWeight
        )
    }
}





















//package com.cc.creatorcircle.ui.components
//
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.widthIn
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.ripple.rememberRipple
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//@Composable
//fun GradientButton(
//    text: String,
//    modifier: Modifier = Modifier,
//    gradientColors: List<Color> = listOf(Color(0xFF9C27B0), Color(0xFFE91E63)), // purple → pink
//    cornerRadius: Int = 8,
//    onClick: () -> Unit
//) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val shape = RoundedCornerShape(cornerRadius.dp)
//
//    Box(
//        modifier = modifier
//            .height(40.dp)
//            .widthIn(min = 120.dp)
//            .clip(shape)
//            .background(
//                brush = Brush.horizontalGradient(gradientColors),
//                shape = shape
//            )
//            .clickable(
//                interactionSource = interactionSource,
////                indication = rememberRipple(color = Color.White.copy(alpha = 0.3f)),
//                onClick = onClick
//            )
//            .padding(horizontal = 16.dp, vertical = 8.dp),
//        contentAlignment = Alignment.Center
//    ) {
//        Text(
//            text = text,
//            color = Color.White,
//            fontSize = 14.sp,
//            fontWeight = FontWeight.Medium
//        )
//    }
//}
//
