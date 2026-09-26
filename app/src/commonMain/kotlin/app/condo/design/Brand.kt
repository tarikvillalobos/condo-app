package app.condo.design

import app.condo.BRAND_ID
import app.condo.domain.Module
import androidx.compose.ui.graphics.Color

data class Brand(
    val id: String,
    val name: String,
    val primary: Color,
    val dark: Color,
    val monogram: String,
    val modules: Set<Module>,
    val supportEmail: String,
    val institution: String,
    val privacy: String,
    val terms: String,
    val apiUrls: Map<String, String>,
    val logo: Glyph = Glyph.BUILDING,
)
object Brands {
    val condo = Brand(
        "condo", "Condo App", Color(0xFF007A5E), Color(0xFF083933), "C",
        Module.entries.toSet(), "suporte@example.invalid",
        "Conectando você ao seu condomínio.",
        "Demonstração com dados fictícios armazenados neste dispositivo. Política de produção pendente do cliente.",
        "Ambiente de demonstração, sem operações reais de acesso ou cobrança.", emptyMap(),
    )
    val viva = Brand(
        "viva", "Viva Morar", Color(0xFF375CB3), Color(0xFF183466), "V",
        Module.entries.toSet() - Module.CAMERAS, "ajuda@example.invalid",
        "Mais vida em comunidade.",
    )
    val current: Brand get() = if (BRAND_ID == "viva") viva else condo
}
