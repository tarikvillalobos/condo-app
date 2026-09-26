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
    NOTICE("M3 9 L8 9 L19 4 L19 20 L8 15 L3 15 Z M8 15 L10 22 M22 9 L22 15"),
    WARNING("M12 3 L23 21 L1 21 Z M12 9 L12 14 M12 17 L12 18"),
    SHIELD("M12 2 L21 6 L20 15 Q18 21 12 23 Q6 21 4 15 L3 6 Z M8 12 L11 15 L16 9"),
    USER("M8 6 A4 4 0 1 0 16 6 A4 4 0 1 0 8 6 M3 22 C3 11 21 11 21 22"),
    BELL("M5 16 L5 9 C5 1 19 1 19 9 L19 16 L22 19 L2 19 Z M9 22 L15 22"),
    BUILDING("M3 22 L3 7 L11 7 L11 2 L20 2 L20 22 Z M7 11 L7 13 M7 16 L7 18 M15 6 L17 6 M15 10 L17 10 M15 14 L17 14 M11 22 L11 18 L15 18 L15 22"),
    QR("M2 2 L9 2 L9 9 L2 9 Z M15 2 L22 2 L22 9 L15 9 Z M2 15 L9 15 L9 22 L2 22 Z M15 15 L18 15 L18 18 L22 18 L22 22 L15 22 Z"),
    BACK("M15 5 L8 12 L15 19"),
    NEXT("M9 5 L16 12 L9 19"),
    DOWN("M5 9 L12 16 L19 9"),
    PLUS("M12 4 L12 20 M4 12 L20 12"),
    CHECK("M4 12 L9 17 L20 6"),
    CLOCK("M2 12 A10 10 0 1 0 22 12 A10 10 0 1 0 2 12 M12 6 L12 12 L16 14"),
    EYE("M1 12 Q12 0 23 12 Q12 24 1 12 M8 12 A4 4 0 1 0 16 12 A4 4 0 1 0 8 12"),
    LOCK("M5 10 L19 10 L19 22 L5 22 Z M8 10 L8 5 C8 0 16 0 16 5 L16 10 M12 15 L12 18"),
    PHONE("M4 2 L9 2 L10 8 L7 10 Q10 17 15 17 L17 14 L23 16 L22 22 Q5 25 2 6 Z"),
    CAR("M3 9 L6 3 L18 3 L21 9 L21 19 L3 19 Z M3 10 L21 10 M6 14 L8 14 M16 14 L18 14 M5 19 L5 22 M19 19 L19 22"),
    EDIT("M3 17 L17 3 L21 7 L7 21 L3 21 Z M14 6 L18 10"),
}
@Composable
