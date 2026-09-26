package app.condo.presentation.screens

import app.condo.design.*
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import kotlinx.datetime.toLocalDateTime

@Composable
fun VisitsScreen(controller: AppController, state: AppState) {
    if (state.destination.route == Route.VISIT_FORM) {
        VisitForm(controller, state)
        return
    }
    val snapshot = state.snapshot!!
    PrimaryButton("+ Nova visita", !state.submitting) { controller.beginVisit() }
    Panel(color = Tokens.tint) {
