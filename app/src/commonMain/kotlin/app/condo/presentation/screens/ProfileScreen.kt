package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

@Composable
fun ProfileScreen(controller: AppController, state: AppState) {
    val session = state.session!!
    val snapshot = state.snapshot!!
    when (state.destination.route) {
        Route.PROFILE -> {
            Panel {
                AppIcon(Glyph.USER)
                Heading(session.account.name)
                Muted(session.account.email)
