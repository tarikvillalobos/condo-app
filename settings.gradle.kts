pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Condo App"
include(":domain", ":app", ":androidApp")
if (providers.gradleProperty("appEnvironment").orElse("demo").get() == "demo") {
    include(":demo")
}
