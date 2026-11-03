package com.canadulce.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography =
    Typography(
        headlineLarge =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                lineHeight = 39.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                lineHeight = 34.sp,
            ),
        titleLarge =
            TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp, lineHeight = 29.sp),
        titleMedium =
            TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 25.sp),
        bodyLarge = TextStyle(fontSize = 18.sp, lineHeight = 28.sp),
        bodyMedium = TextStyle(fontSize = 15.sp, lineHeight = 23.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 20.sp),
    )
