plugins {
    `java-library`
    id("net.ltgt.errorprone")
    `maven-publish`
}

dependencies {
    "api"(project(":canvas-core"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
}

tasks.test {
    useJUnitPlatform()
}
