package app.condo.design

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape
import org.jetbrains.compose.resources.Font
import app.condo.resources.*

object Tokens {
    val text = Color(0xFF10201C)
    val secondary = Color(0xFF5A6A65)
    val background = Color(0xFFF5F8F7)
    val border = Color(0xFFE2EAE7)
    val tint = Color(0xFFE7F2EE)
    val warning = Color(0xFFFFE9C2)
    val onWarning = Color(0xFF7A4900)
    val danger = Color(0xFFB42318)
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val page = 20.dp
    val xl = 24.dp
    val touch = 48.dp
    val icon = 24.dp
    val corner = RoundedCornerShape(16.dp)
    val largeCorner = RoundedCornerShape(20.dp)
    val controlCorner = RoundedCornerShape(14.dp)
    val compact = 600.dp
    val expanded = 840.dp
    val maxContent = 1180.dp
    val maxForm = 520.dp
}
val LocalBrand = staticCompositionLocalOf { Brands.condo }
@Composable
fun CondoTheme(brand: Brand = Brands.current, content: @Composable () -> Unit) {
    val body = FontFamily(Font(Res.font.manrope))
    val heading = FontFamily(Font(Res.font.sora))
    val typography = Typography(
        headlineLarge = TextStyle(fontFamily = heading, fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 39.sp),
        headlineMedium = TextStyle(fontFamily = heading, fontWeight = FontWeight.SemiBold, fontSize = 26.sp, lineHeight = 34.sp),
        titleLarge = TextStyle(fontFamily = heading, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontFamily = heading, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 25.sp),
        titleSmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 14.sp, lineHeight = 21.sp),
        bodyLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 21.sp),
        bodySmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
        labelLarge = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 22.sp),
        labelMedium = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 18.sp),
        labelSmall = TextStyle(fontFamily = body, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 16.sp),
    )
    CompositionLocalProvider(LocalBrand provides brand) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = brand.primary, onPrimary = Color.White,
                primaryContainer = Tokens.tint, onPrimaryContainer = brand.dark,
                background = Tokens.background, onBackground = Tokens.text,
                surface = Color.White, onSurface = Tokens.text,
                surfaceVariant = Tokens.background, onSurfaceVariant = Tokens.secondary,
                outline = Tokens.border, error = Tokens.danger,
            ),
            typography = typography,
            shapes = Shapes(medium = Tokens.corner, large = Tokens.largeCorner),
            content = content,
        )
    }
}
