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
fun Heading(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = LocalBrand.current.dark)
}
@Composable
fun Muted(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier, style = MaterialTheme.typography.bodySmall, color = Tokens.secondary)
}
@Composable
fun PrimaryButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(onClick, Modifier.fillMaxWidth().heightIn(min = Tokens.touch), enabled,
        shape = Tokens.controlCorner, contentPadding = PaddingValues(Tokens.md)) {
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
@Composable
fun SecondaryButton(text: String, onClick: () -> Unit) {
    OutlinedButton(onClick, Modifier.fillMaxWidth().heightIn(min = Tokens.touch), shape = Tokens.controlCorner) { Text(text) }
}
@Composable
fun StatusChip(text: String, warning: Boolean = false) {
    Surface(color = if (warning) Tokens.warning else Tokens.tint, shape = Tokens.controlCorner) {
        Text(text, Modifier.padding(horizontal = Tokens.md, vertical = Tokens.sm),
            color = if (warning) Tokens.onWarning else LocalBrand.current.dark,
            style = MaterialTheme.typography.labelSmall)
    }
}
@Composable
fun EmptyState(title: String, description: String, glyph: Glyph = Glyph.CHECK) {
    Panel {
        AppIcon(glyph, modifier = Modifier.size(36.dp))
        Heading(title)
        Muted(description)
    }
}
@Composable
fun MenuRow(title: String, subtitle: String? = null, glyph: Glyph = Glyph.NEXT, onClick: () -> Unit) {
    Surface(onClick, Modifier.fillMaxWidth(), shape = Tokens.corner, border = BorderStroke(1.dp, Tokens.border)) {
        Row(Modifier.padding(Tokens.lg).heightIn(min = 40.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Tokens.md)) {
            AppIcon(glyph)
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                subtitle?.let { Muted(it) }
            }
            AppIcon(Glyph.NEXT, tint = Tokens.secondary)
        }
    }
}
@Composable
fun FilterChips(options: List<String>, selected: String, onSelect: (String) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(Tokens.sm)) {
        options.forEach { option ->
            FilterChip(selected == option, { onSelect(option) }, { Text(option) }, modifier = Modifier.heightIn(min = Tokens.touch))
        }
    }
}
@Composable
fun <T> AdaptiveGrid(items: List<T>, minimum: Dp = 240.dp, maximumColumns: Int = 4, content: @Composable (T) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val scale = LocalDensity.current.fontScale.coerceAtLeast(1f)
