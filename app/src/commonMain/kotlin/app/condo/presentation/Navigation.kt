package app.condo.presentation

import app.condo.design.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val mainDestinations = listOf(Route.HOME, Route.PARCELS, Route.VISITS, Route.CAMERAS, Route.PROFILE)
fun Route.glyph(): Glyph = when (this) {
    Route.HOME -> Glyph.HOME
    Route.PARCELS, Route.PARCEL_DETAIL -> Glyph.BOX
    Route.CAMERAS, Route.CAMERA_DETAIL -> Glyph.CAMERA
    Route.VISITS, Route.VISIT_FORM -> Glyph.PEOPLE
    Route.PETS, Route.PET_DETAIL, Route.PET_FORM -> Glyph.PAW
    Route.BOOKINGS, Route.EVENTS -> Glyph.CALENDAR
    Route.NOTICES, Route.BULLETIN -> Glyph.NOTICE
