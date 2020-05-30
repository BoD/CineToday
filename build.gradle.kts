import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions") version Versions.BEN_MANES_VERSIONS_PLUGIN
}

buildscript {
    repositories {
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Versions.ANDROID_GRADLE_PLUGIN}")
        classpath(kotlin("gradle-plugin", Versions.KOTLIN))
        classpath("com.github.ben-manes:gradle-versions-plugin:${Versions.BEN_MANES_VERSIONS_PLUGIN}")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven("https://jitpack.io")
    }

    // Show a report in the log when running tests
    tasks.withType(Test::class) {
        // TODO
//        testLogging {
//            events "passed", "skipped", "failed", "standardOut", "standardError"
//        }
    }
}

// Run `./gradlew dependencyUpdates` to see latest versions of dependencies
tasks {
    register("clean", Delete::class) {
        delete(rootProject.buildDir)
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = Versions.GRADLE
    }

    // Configuration for gradle-versions-plugin
    withType<DependencyUpdatesTask> {
        resolutionStrategy {
            componentSelection {
                all {
                    if (setOf(
                            "alpha",
                            "beta",
                            "rc",
                            "preview",
                            "eap",
                            "m1"
                        ).any { candidate.version.contains(it, true) }
                    ) {
                        reject("Non stable")
                    }
                }
            }
        }
    }
}

// Build properties
AppConfig.buildProperties.loadFromFile(getOrCreateFile("build.properties"))

// Build number
val buildNumberProperties = mutableMapOf<String, String>().apply {
    loadFromFile(getOrCreateFile("build.number"))
}
AppConfig.buildNumber = buildNumberProperties["buildNumber"]!!.toInt()

// Splash screen
printSplashScreen()