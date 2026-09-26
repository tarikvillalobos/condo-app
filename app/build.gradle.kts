import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}
val environment = providers.gradleProperty("appEnvironment").orElse("demo").get()
val brandName = providers.gradleProperty("brand").orElse("condo").get()
require(environment in listOf("demo", "staging", "production"))
require(brandName in listOf("condo", "viva"))
val generatedSource = layout.buildDirectory.dir("generated/config")
val generateConfig by tasks.registering {
    inputs.property("environment", environment)
    inputs.property("brand", brandName)
    outputs.dir(generatedSource)
    doLast {
        val file = generatedSource.get().file("app/condo/BuildConfig.kt").asFile
        file.parentFile.mkdirs()
        val factory = if (environment == "demo") {
            "app.condo.demo.DemoRepository(storage, clock)"
        } else {
            "app.condo.data.UnavailableRepository()"
        }
        file.writeText("""
            package app.condo
            import app.condo.domain.*
            const val APP_ENVIRONMENT = "$environment"
            const val BRAND_ID = "$brandName"
            fun createRepository(storage: LocalStore, clock: AppClock): CondoRepository = $factory
        """.trimIndent())
    }
}
kotlin {
    jvmToolchain(21)
    android {
        namespace = "app.condo.shared"
        compileSdk = 36
