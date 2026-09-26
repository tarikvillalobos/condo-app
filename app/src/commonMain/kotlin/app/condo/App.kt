package app.condo

import app.condo.design.*
import app.condo.domain.*
import app.condo.platform.PlatformServices
import app.condo.presentation.*
import app.condo.presentation.screens.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CondoApp(controller: AppController) {
    val state by controller.state.collectAsState()
    CondoTheme {
        Surface(Modifier.fillMaxSize(), color = Tokens.background) {
