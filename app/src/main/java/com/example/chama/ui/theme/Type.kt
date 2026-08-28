package com.example.chama.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.chama.R

// 1. Declara a FontFamily apontando para o seu arquivo em res/font/
val GermaniaOne = FontFamily(
    Font(R.font.germaniaone_regular, FontWeight.Normal)
)

// 2. Configura a tipografia global do Material 3
val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = GermaniaOne,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        letterSpacing = 1.sp
    ),
    titleLarge = TextStyle(
        fontFamily = GermaniaOne,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)