package app.condo

import app.condo.demo.DemoRepository
import app.condo.domain.*
import app.condo.presentation.*
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

class ResidentFlowTest {
    @get:Rule val compose = createComposeRule()
    @Test fun residentCanTabBetweenCredentialsAndPressEnterToLogin() {
        val platform = TestServices()
        val clock = AppClock { Instant.parse("2026-09-26T12:00:00Z") }
        val controller = AppController(DemoRepository(platform.store, clock, 0), platform, clock,
            storageDispatcher = kotlinx.coroutines.Dispatchers.Main.immediate)
        compose.setContent { CondoApp(controller) }
        try {
            val identity = compose.onNode(hasSetTextAction() and hasText("CPF ou e-mail"))
            val password = compose.onNode(hasSetTextAction() and hasText("Senha"))
            identity.performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            identity.performTextInput("alex@condo.demo")
            compose.onRoot().performKeyInput { pressKey(Key.Tab) }
            password.assertIsFocused().performTextInput("Demo1234!")
            val submit = compose.onNode(hasText("Entrar") and hasClickAction())
            submit.performScrollTo().performSemanticsAction(SemanticsActions.RequestFocus) { it() }
            submit.assertIsFocused()
            compose.onRoot().performKeyInput { pressKey(Key.Enter) }
            compose.waitUntil(5000) { controller.state.value.snapshot != null }
            compose.onNodeWithText("Olá, Alex").assertExists()
        } finally {
    @Test fun residentCanLoginOpenParcelAndReportCollection() {
        val platform = TestServices()
        val clock = AppClock { Instant.parse("2026-09-26T12:00:00Z") }
        val controller = AppController(DemoRepository(platform.store, clock, 0), platform, clock, storageDispatcher = kotlinx.coroutines.Dispatchers.Main.immediate)
        compose.setContent { CondoApp(controller) }
        compose.onNodeWithText("Entrar na demonstração").performScrollTo().performClick()
        compose.waitUntil(5000) { controller.state.value.snapshot != null }
        compose.onNodeWithText("Olá, Alex").assertExists()
        compose.onNodeWithText("Ver QR Code de retirada").performClick()
        compose.onNode(hasText("Mercado Livre") and hasClickAction()).performScrollTo().performClick()
        compose.onNodeWithText("Já retirei a encomenda").performScrollTo().performClick()
        compose.waitUntil(5000) {
            controller.state.value.snapshot?.parcels?.first()?.status == ParcelStatus.MANUAL_REPORT
        }
        compose.onNodeWithText("Entendi").performClick()
        compose.onAllNodesWithText("Retirada informada · aguardando locker").assertCountEquals(2)
        compose.runOnIdle { controller.close() }
    }
}
