package app.condo.design

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*

@Composable
fun Panel(modifier: Modifier = Modifier, color: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Surface(modifier.fillMaxWidth(), shape = Tokens.corner, color = color, border = BorderStroke(1.dp, Tokens.border)) {
        Column(Modifier.padding(Tokens.lg), verticalArrangement = Arrangement.spacedBy(Tokens.md), content = content)
    }
}
@Composable
