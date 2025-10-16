pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "sgc"

include("backend")
include("frontend")

// enableFeaturePreview("STABLE_CONFIGURATION_CACHE")