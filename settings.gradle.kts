pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "canvas"

include(
    "canvas-api",
    "canvas-core",
    "canvas-pack",
    "canvas-elements",
    "canvas-paper",
    "canvas-kitchensink",
)
