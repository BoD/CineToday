import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask

plugins {
    id("com.github.ben-manes.versions") version Versions.BEN_MANES_VERSIONS_PLUGIN
}

buildscript {
    repositories {
        mavenLocal()
        google()
        jcenter()
    }

    dependencies {
        classpath("com.android.tools.build", "gradle", Versions.ANDROID_GRADLE_PLUGIN)
        classpath(kotlin("gradle-plugin", Versions.KOTLIN))
        classpath("com.github.ben-manes", "gradle-versions-plugin", Versions.BEN_MANES_VERSIONS_PLUGIN)
    }
}

allprojects {
    repositories {
        mavenLocal()
        google()
        jcenter()
        maven("https://jitpack.io")
    }

    // Show a report in the log when running tests
    tasks.withType<Test> {
        testLogging {
            events("passed", "skipped", "failed", "standardOut", "standardError")
        }
    }
}

tasks {
    register<Delete>("clean") {
        delete(rootProject.buildDir)
    }

    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = Versions.GRADLE
    }

    // Configuration for gradle-versions-plugin
    // Run `./gradlew dependencyUpdates` to see latest versions of dependencies
    withType<DependencyUpdatesTask> {
        resolutionStrategy {
            componentSelection {
                all {
                    if (setOf("alpha", "beta", "rc", "preview", "eap", "m1").any { candidate.version.contains(it, true) }) {
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
val buildNumberPropertiesFile = getOrCreateFile("build.number")
val buildNumberProperties = mutableMapOf<String, String>().apply {
    loadFromFile(buildNumberPropertiesFile)
}
AppConfig.buildNumber = buildNumberProperties["buildNumber"]!!.toInt()

// Add an 'incrementBuildNumber' task that increments the build number
val incrementBuildNumberTask = tasks.register("incrementBuildNumber") {
    doFirst {
        buildNumberProperties["buildNumber"] = (AppConfig.buildNumber + 1).toString()
        buildNumberProperties.storeToFile(buildNumberPropertiesFile)
    }
}

// Make the 'assembleRelease' task depend on the 'incrementBuildNumber' task of every subproject
subprojects {
    tasks.whenTaskAdded {
        if (name == "assembleRelease") {
            dependsOn(incrementBuildNumberTask)
        }
    }
}


// Splash screen
printSplashScreen()