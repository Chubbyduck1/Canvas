plugins {
    `java-library`
    id("net.ltgt.errorprone")
    `maven-publish`
}

dependencies {
    "api"("org.jspecify:jspecify:1.0.0")
    "api"("net.kyori:adventure-api:${property("adventureVersion")}")
}
