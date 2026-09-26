package app.condo.design

import app.condo.presentation.AppController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun FormField(
    controller: AppController,
    key: String,
    label: String,
    initial: String = "",
    secret: Boolean = false,
    multiline: Boolean = false,
) {
    val state by controller.state.collectAsState()
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = state.forms[key] ?: initial,
        onValueChange = { controller.field(key, it) },
        label = { Text(label) },
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        modifier = Modifier.fillMaxWidth(),
        shape = Tokens.controlCorner,
        visualTransformation = if (secret && !visible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = if (secret) {
            { IconButton({ visible = !visible }) {
                AppIcon(Glyph.EYE, if (visible) "Ocultar senha" else "Mostrar senha")
            } }
        } else null,
    )
}
