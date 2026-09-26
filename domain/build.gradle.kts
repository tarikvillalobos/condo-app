plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
}
kotlin {
    jvmToolchain(21)
    android {
        namespace = "app.condo.domain"
        compileSdk = 36
        minSdk = 26
    }
    jvm("desktop")
    iosArm64()
    iosSimulatorArm64()
    sourceSets {
        commonMain.dependencies {
            api(libs.coroutines.core)
            api(libs.datetime)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.coroutines.test)
        }
    }
}
