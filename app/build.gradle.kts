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
        compileSdk = 37
        minSdk = 26
        androidResources.enable = true
    }
    jvm("desktop")
    listOf(iosArm64(), iosSimulatorArm64()).forEach {
        it.binaries.framework {
            baseName = "CondoApp"
            isStatic = true
        }
    }
    sourceSets {
        commonMain { kotlin.srcDir(generatedSource) }
        commonMain.dependencies {
            api(project(":domain"))
            if (environment == "demo") implementation(project(":demo"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.ktor.core)
            implementation(libs.serialization.json)
            implementation(libs.qrcode)
        }
        androidMain.dependencies {
            implementation(libs.ktor.okhttp)
            implementation(libs.coroutines.android)
        }
        iosMain.dependencies { implementation(libs.ktor.darwin) }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.coroutines.swing)
                implementation(libs.ktor.cio)
            }
        }
        if (environment != "demo") {
            commonTest { kotlin.exclude("**/presentation/ControllerTest.kt") }
            getByName("desktopTest").kotlin.exclude("**/ResponsiveRenderTest.kt", "**/ResidentFlowTest.kt")
        }
        commonTest.dependencies {
            if (environment == "demo") implementation(project(":demo"))
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
            implementation(libs.ktor.mock)
        }
        val desktopTest by getting {
            dependencies {
                implementation(compose.desktop.uiTestJUnit4)
                implementation(libs.zxing)
            }
        }
    }
}
tasks.matching { it.name.startsWith("compileKotlin") || it.name.startsWith("compileAndroidMain") }
    .configureEach { dependsOn(generateConfig) }
compose.desktop {
    application {
        mainClass = "app.condo.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = if (brandName == "viva") "Viva Morar" else "Condo App"
            copyright = "Private and proprietary"
        }
    }
}

compose.resources { packageOfResClass = "app.condo.resources" }
