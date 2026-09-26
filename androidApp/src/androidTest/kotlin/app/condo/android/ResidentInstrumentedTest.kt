package app.condo.android

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.condo.APP_ENVIRONMENT
import app.condo.BRAND_ID
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResidentInstrumentedTest {
    private val compose = createAndroidComposeRule<MainActivity>()
    private val resetDemo = object : ExternalResource() {
        override fun before() {
            assumeTrue("Os fluxos instrumentais exigem o ambiente demonstrativo", APP_ENVIRONMENT == "demo")
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            check(context.getSharedPreferences("$BRAND_ID.$APP_ENVIRONMENT", Context.MODE_PRIVATE)
                .edit().clear().commit())
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(resetDemo).around(compose)

    @Test fun credentialsOpenHomeAndCollectionRemainsAwaitingLocker() {
        enter("CPF ou e-mail", "alex@condo.demo")
        enter("Senha", "Demo1234!")
        closeSoftKeyboard()
        compose.onNodeWithText("Entrar").performScrollTo().performClick()
        waitForText("Olá, Alex")
        compose.onNodeWithText("Ver QR Code de retirada").performScrollTo().performClick()
        compose.onNode(hasText("Mercado Livre") and hasClickAction()).performScrollTo().performClick()
        compose.onNodeWithText("Já retirei a encomenda").performScrollTo().performClick()
        waitForText("Entendi")
        compose.onNodeWithText("Entendi").performClick()
        waitForText("Retirada informada · aguardando locker")
        compose.onAllNodesWithText("Retirada informada · aguardando locker")[0].assertExists()
    }

    @Test fun visitDraftSurvivesActivityRecreationAndCanBeSubmitted() {
        compose.onNodeWithText("Entrar na demonstração").performScrollTo().performClick()
        waitForText("Olá, Alex")
        compose.onNode(hasText("Visitas") and hasClickAction()).performClick()
        compose.onNodeWithText("+ Nova visita").performScrollTo().performClick()
        enter("Nome completo", "Nina Instrumentação")
        enter("Motivo ou serviço", "Entrega autorizada")
        compose.activityRule.scenario.recreate()
        field("Nome completo").assertTextContains("Nina Instrumentação")
        field("Motivo ou serviço").assertTextContains("Entrega autorizada")
        closeSoftKeyboard()
        compose.onNodeWithText("Salvar visita").performScrollTo().performClick()
        waitForText("Visita salva na demonstração.")
        compose.onNodeWithText("Entendi").performClick()
        compose.onNodeWithText("Agendadas").performScrollTo().performClick()
        waitForText("Nina Instrumentação")
        compose.onNodeWithText("Nina Instrumentação").performScrollTo().assertIsDisplayed()
    }

    private fun field(label: String) = compose.onNode(hasSetTextAction() and hasText(label))
    private fun enter(label: String, value: String) {
        field(label).performScrollTo().performClick().performTextReplacement(value)
    }
    private fun waitForText(value: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
