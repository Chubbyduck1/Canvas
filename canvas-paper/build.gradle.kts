plugins {
    `java-library`
    id("net.ltgt.errorprone")
    `maven-publish`
    id("com.gradleup.shadow")
}

// Shaded into the standalone plugin jar only, never exposed to plugins that depend on canvas-paper.
val bundled: Configuration by configurations.creating
configurations.compileOnly {
    extendsFrom(bundled)
}

dependencies {
    constraints {
        "compileOnly"("org.apache.logging.log4j:log4j-api:2.25.5") {
            because("Fix CVE-2026-49844 in Paper's transitive API dependency")
        }
        "compileOnly"("org.codehaus.plexus:plexus-utils:3.6.1") {
            because("Fix CVE-2025-67030 in Paper's transitive API dependency")
        }
        "compileOnly"("org.apache.commons:commons-lang3:3.18.0") {
            because("Fix CVE-2025-48924 in Paper's transitive API dependency")
        }
    }
    "api"(project(":canvas-pack"))
    "api"(project(":canvas-elements"))
    "compileOnly"("io.papermc.paper:paper-api:${property("paperVersion")}")
    bundled("org.bstats:bstats-bukkit:${property("bstatsVersion")}")
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveBaseName.set("Canvas")
    archiveClassifier.set("")
    configurations.set(listOf(project.configurations.runtimeClasspath.get(), bundled))
    relocate("org.bstats", "me.chubbyduck.canvas.libs.bstats")
    // Paper already provides Adventure and Gson.
    dependencies {
        exclude(dependency("net.kyori:.*"))
        exclude(dependency("com.google.code.gson:.*"))
        exclude(dependency("org.jetbrains:annotations"))
        exclude(dependency("org.jspecify:jspecify"))
    }
    from("src/plugin") {
        include("plugin.yml", "config.yml")
        expand("version" to project.version)
    }
}

tasks.named("build") {
    dependsOn("shadowJar")
}

shadow {
    addShadowVariantIntoJavaComponent.set(false)
}
