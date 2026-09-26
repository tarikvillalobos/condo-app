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
import condo_app.app.generated.resources.*

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
