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
    Route.SERVICES, Route.REQUEST_FORM -> Glyph.WARNING
    Route.CONCIERGE -> Glyph.SHIELD
    else -> Glyph.USER
}
fun AppState.allows(route: Route) = route.module == null ||
    route.module in snapshot?.membership?.modules.orEmpty().intersect(Brands.current.modules)
@Composable
fun BottomNavigation(controller: AppController, state: AppState) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(Modifier.fillMaxWidth().padding(vertical = Tokens.sm)) {
            mainDestinations.filter(state::allows).forEach { route ->
                val selected = state.destination.route == route
                Surface({ controller.navigate(route) }, Modifier.weight(1f), color = Color.Transparent) {
                    Column(Modifier.padding(Tokens.xs).heightIn(min = Tokens.touch),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Tokens.xs)) {
                        AppIcon(route.glyph(), tint = if (selected) LocalBrand.current.primary else Tokens.secondary)
                        Text(route.title, style = MaterialTheme.typography.labelSmall,
                            color = if (selected) LocalBrand.current.primary else Tokens.secondary)
                    }
