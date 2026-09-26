package app.condo.presentation.screens

import app.condo.design.*
import app.condo.presentation.*
import androidx.compose.runtime.Composable

@Composable
fun ScreenRouter(controller: AppController, state: AppState, expanded: Boolean) {
    if (!state.allows(state.destination.route)) {
        EmptyState("Módulo indisponível", "Esta funcionalidade não está habilitada para sua marca ou condomínio.")
        return
    }
    when (state.destination.route) {
        Route.HOME -> HomeScreen(controller, state)
        Route.PARCELS, Route.PARCEL_DETAIL -> ParcelScreen(controller, state, expanded)
        Route.CAMERAS, Route.CAMERA_DETAIL -> CamerasScreen(controller, state)
        Route.VISITS, Route.VISIT_FORM -> VisitsScreen(controller, state)
        Route.PETS, Route.PET_FORM, Route.PET_DETAIL, Route.PET_ALERTS -> PetsScreen(controller, state)
        Route.BOOKINGS -> BookingsScreen(controller, state)
        Route.EVENTS, Route.NOTICES, Route.BULLETIN, Route.NOTIFICATIONS,
        Route.SERVICES, Route.REQUEST_FORM, Route.REQUEST_DETAIL, Route.CONCIERGE -> CommunityScreen(controller, state)
        else -> ProfileScreen(controller, state)
    }
}
