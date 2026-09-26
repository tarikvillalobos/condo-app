package app.condo.android

import android.content.Context
import android.graphics.Bitmap
import android.view.inputmethod.InputMethodManager
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.AnnotatedString
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.condo.APP_ENVIRONMENT
import app.condo.BRAND_ID
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExternalResource
import org.junit.rules.RuleChain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.junit.runner.RunWith
import java.io.File

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
    private val captureFailure = object : TestWatcher() {
        override fun failed(error: Throwable, description: Description) {
            runCatching {
                val instrumentation = InstrumentationRegistry.getInstrumentation()
                val directory = requireNotNull(instrumentation.targetContext.getExternalFilesDir("qa"))
                check(directory.isDirectory || directory.mkdirs())
                val screenshot = instrumentation.uiAutomation.takeScreenshot() ?: return
                try {
                    File(directory, "${description.methodName}.png").outputStream().use {
                        check(screenshot.compress(Bitmap.CompressFormat.PNG, 100, it))
                    }
                } finally { screenshot.recycle() }
            }
        }
    }
    @get:Rule val rules: RuleChain = RuleChain.outerRule(resetDemo).around(compose).around(captureFailure)

    @Test fun credentialsOpenHomeAndCollectionRemainsAwaitingLocker() {
        enter("CPF ou e-mail", "alex@condo.demo")
        enter("Senha", "Demo1234!")
        field("CPF ou e-mail").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.InputText, AnnotatedString("alex@condo.demo")))
        field("Senha").assert(SemanticsMatcher.expectValue(
            SemanticsProperties.InputText, AnnotatedString("Demo1234!")))
        compose.onNodeWithText("Entrar").performScrollTo().assertIsDisplayed().performClick()
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
    private fun closeSoftKeyboard() {
        compose.runOnIdle {
            val activity = compose.activity
            val view = activity.currentFocus ?: activity.window.decorView
            val keyboard = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.hideSoftInputFromWindow(view.windowToken, 0)
            view.clearFocus()
        }
        compose.waitUntil(5_000) {
            compose.runOnIdle {
                val insets = ViewCompat.getRootWindowInsets(compose.activity.window.decorView)
                insets != null && !insets.isVisible(WindowInsetsCompat.Type.ime())
            }
        }
        compose.waitForIdle()
    }
    private fun enter(label: String, value: String) {
        field(label).performScrollTo().performClick().performTextReplacement(value)
    }
    private fun waitForText(value: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(value).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
