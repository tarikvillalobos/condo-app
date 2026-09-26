plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
}
android {
    namespace = "app.condo.android"
    compileSdk = 36
    defaultConfig {
        applicationId = if (providers.gradleProperty("brand").get() == "viva") {
            "app.vivamorar.resident"
        } else "app.condo.resident"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    buildFeatures { compose = true }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
