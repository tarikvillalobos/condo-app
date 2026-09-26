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
)
object Brands {
