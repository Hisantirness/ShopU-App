package com.univalle.shopu.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.ui.text.TextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Red = Color(0xFFD63734)
private val Black = Color(0xFF060000)
private val White = Color(0xFFF9FAFA)

private val LightColors = lightColorScheme(
    primary = Red,
    onPrimary = White,
    secondary = Red,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = Color.White,
    onSurface = Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Red,
    onPrimary = White,
    secondary = Red,
    onSecondary = White,
    background = Color(0xFF111111),
    onBackground = White,
    surface = Color(0xFF1A1A1A),
    onSurface = White,
    error = Color(0xFFCF6679),
    onError = Color.Black
)

@Composable
fun ShopUTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors: ColorScheme = if (darkTheme) DarkColors else LightColors
    val shapes = Shapes(
        small = RoundedCornerShape(8.dp),
        medium = RoundedCornerShape(12.dp),
        large = RoundedCornerShape(16.dp),
        extraLarge = RoundedCornerShape(24.dp)
    )
    // Google Fonts provider for Poppins
    val provider = GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = com.univalle.shopu.R.array.com_google_android_gms_fonts_certs
    )
    val poppins = GoogleFont("Poppins")
    val poppinsFamily = FontFamily(
        Font(googleFont = poppins, fontProvider = provider, weight = FontWeight.Normal),
        Font(googleFont = poppins, fontProvider = provider, weight = FontWeight.Medium),
        Font(googleFont = poppins, fontProvider = provider, weight = FontWeight.SemiBold),
        Font(googleFont = poppins, fontProvider = provider, weight = FontWeight.Bold)
    )
    // Typography mapping guided by Figma using base M3 styles with increased sizes
    val base = Typography()
    val typography = Typography(
        displayLarge = base.displayLarge.copy(fontFamily = poppinsFamily),
        displayMedium = base.displayMedium.copy(fontFamily = poppinsFamily),
        displaySmall = base.displaySmall.copy(fontFamily = poppinsFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = poppinsFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = poppinsFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
        titleMedium = base.titleMedium.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
        titleSmall = base.titleSmall.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp),
        bodyLarge = base.bodyLarge.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.Normal, fontSize = 18.sp),
        bodyMedium = base.bodyMedium.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp),
        bodySmall = base.bodySmall.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp),
        labelLarge = base.labelLarge.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        labelMedium = base.labelMedium.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp),
        labelSmall = base.labelSmall.copy(fontFamily = poppinsFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
    )

    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
