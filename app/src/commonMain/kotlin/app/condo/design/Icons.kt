package app.condo.design

import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

enum class Glyph(val path: String) {
    HOME("M3 10 L12 3 L21 10 M5 9 L5 21 L10 21 L10 14 L14 14 L14 21 L19 21 L19 9"),
    BOX("M3 7 L12 2 L21 7 L21 17 L12 22 L3 17 Z M3 7 L12 12 L21 7 M12 12 L12 22 M7 5 L16 10"),
    PEOPLE("M4 21 C4 12 16 12 16 21 M7 6 A4 4 0 1 0 15 6 A4 4 0 1 0 7 6 M18 4 C23 5 22 11 18 12 M19 15 C22 16 23 18 23 21"),
    CAMERA("M3 6 L15 6 L15 18 L3 18 Z M15 10 L22 6 L22 18 L15 14"),
    PAW("M7 16 C10 11 14 11 17 16 C20 21 14 23 12 20 C8 23 4 21 7 16 M3 9 A2 3 0 1 0 7 9 A2 3 0 1 0 3 9 M8 5 A2 3 0 1 0 12 5 A2 3 0 1 0 8 5 M15 5 A2 3 0 1 0 19 5 A2 3 0 1 0 15 5 M19 11 A2 3 0 1 0 23 11 A2 3 0 1 0 19 11"),
    CALENDAR("M4 5 L20 5 L20 21 L4 21 Z M4 10 L20 10 M8 2 L8 7 M16 2 L16 7"),
