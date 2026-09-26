package app.condo.android

import android.content.Context
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
