object Versions {
    // Misc and plugins
    const val GRADLE = "7.2"
    const val KOTLIN = "1.5.31"
    const val BEN_MANES_VERSIONS_PLUGIN = "0.39.0"
    const val ANDROID_GRADLE_PLUGIN = "7.0.3"

    // App dependencies
    const val ANDROIDX_FRAGMENT = "1.3.6"
    const val ANDROIDX_ANNOTATION = "1.2.0"
    const val ANDROIDX_PERCENT_LAYOUT = "1.0.0"
    const val ANDROIDX_PALETTE = "1.0.0"
    const val ANDROIDX_PREFERENCE = "1.1.1"
    const val ANDROIDX_WEAR = "1.2.0"
    const val ANDROIDX_WEAR_TILES = "1.0.0-alpha09"
    const val ANDROIDX_LIFECYCLE = "2.4.0"
    const val ANDROIDX_ROOM = "2.3.0"
    const val ANDROIDX_WORK_MANAGER = "2.7.0"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "2.1.1"
    const val ANDROIDX_CONCURRENT = "1.1.0"
    const val RX_JAVA = "2.2.21"
    const val RX_ANDROID = "2.1.1"
    const val COROUTINES = "1.5.1"
    const val DAGGER = "2.40"
    const val ANDROID_WEARABLE = "2.8.1"
    const val KPREFS = "1.5.0"
    const val OK_HTTP = "4.9.2"
    const val GLIDE = "4.12.0"
    const val JSR_250 = "1.0"
    const val APOLLO = "3.0.0-rc04-SNAPSHOT"

    // Testing dependencies
    const val ESPRESSO = "3.4.0"
    const val JUNIT = "4.13.2"
    const val ROBOLECTRIC = "4.6.1"
    const val FEST = "2.0M10"
}

object AppConfig {
    const val APPLICATION_ID = "org.jraf.android.cinetoday"
    const val COMPILE_SDK = 31
    const val TARGET_SDK = 31
    const val MIN_SDK = 26

    var buildNumber: Int = 0
    val buildProperties = mutableMapOf<String, String>()
}
