import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("net.ltgt.errorprone") version "4.3.0" apply false
    id("xyz.jpenilla.run-paper") version "3.1.0" apply false
}

// -Pcanvas.strict=true turns every lint warning (javac, Error Prone, javadoc) into a build failure.
// Without it, they are only reported.
val strict = (findProperty("canvas.strict") as String?)?.toBoolean() ?: false

subprojects {
    group = "me.chubbyduck"
    // Release builds pass -PcanvasVersion=<tag without the leading v>.
    version = (findProperty("canvasVersion") as String?) ?: "0.1.0-SNAPSHOT"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    // Library modules publish one Maven artifact each (JitPack builds them with publishToMavenLocal).
    plugins.withId("maven-publish") {
        extensions.configure<PublishingExtension> {
            publications.create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    plugins.withId("net.ltgt.errorprone") {
        extensions.configure<JavaPluginExtension> {
            withSourcesJar()
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(25)
            options.compilerArgs.addAll(listOf("-Xlint:all,-processing,-options"))
            if (strict) {
                options.compilerArgs.add("-Werror")
            }
            options.errorprone {
                // Error Prone's default checks report as warnings; NullAway (JSpecify: @NullMarked
                // classes, @Nullable types) is always an error on main sources.
                allErrorsAsWarnings.set(true)
                check("NullAway", CheckSeverity.ERROR)
                option("NullAway:OnlyNullMarked", "true")
            }
        }

        tasks.withType<Javadoc>().configureEach {
            (options as StandardJavadocDocletOptions).apply {
                encoding = "UTF-8"
                addStringOption("Xdoclint:all,-missing", "-quiet")
                if (strict) {
                    addBooleanOption("Werror", true)
                }
            }
        }

        dependencies {
            "errorprone"("com.google.errorprone:error_prone_core:2.42.0")
            "errorprone"("com.uber.nullaway:nullaway:0.12.10")
        }
    }
}
