plugins {
    `java-library`
    id("net.ltgt.errorprone")
    `maven-publish`
}

dependencies {
    "api"(project(":canvas-core"))
}
