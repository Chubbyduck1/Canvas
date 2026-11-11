plugins {
    `java-library`
    id("net.ltgt.errorprone")
    id("xyz.jpenilla.run-paper")
}

evaluationDependsOn(":canvas-paper")

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
    // The Canvas plugin provides this at runtime (plugin.yml lists it under depend).
    "compileOnly"(project(":canvas-paper"))
    "compileOnly"("io.papermc.paper:paper-api:${property("paperVersion")}")
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}

tasks.named<xyz.jpenilla.runpaper.task.RunServer>("runServer") {
    minecraftVersion("26.3")
    pluginJars.from(project(":canvas-paper").tasks.named("shadowJar"))
    jvmArgs("-Dcom.mojang.eula.agree=true")
}
