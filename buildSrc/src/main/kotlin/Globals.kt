object Versions {
    // Misc and plugins
    const val GRADLE = "6.4.1"
    const val KOTLIN = "1.3.72"
    const val BEN_MANES_VERSIONS_PLUGIN = "0.28.0"
    const val ANDROID_GRADLE_PLUGIN = "4.0.0"

    // App dependencies
    const val ANDROIDX_FRAGMENT = "1.2.4"
    const val ANDROIDX_ANNOTATION = "1.1.0"
    const val ANDROIDX_PERCENT_LAYOUT = "1.0.0"
    const val ANDROIDX_PALETTE = "1.0.0"
    const val ANDROIDX_PREFERENCE = "1.1.1"
    const val ANDROIDX_WEAR = "1.0.0"
    const val ANDROIDX_LIFECYCLE = "2.2.0"
    const val ANDROIDX_ROOM = "2.2.5"
    const val ANDROIDX_WORK_MANAGER = "2.3.4"
    const val ANDROIDX_CONSTRAINT_LAYOUT = "1.1.3"
    const val RX_JAVA = "2.2.19"
    const val RX_ANDROID = "2.1.1"
    const val DAGGER = "2.28"
    const val ANDROID_WEARABLE = "2.7.0"
    const val KPREFS = "1.3.0"
    const val OK_HTTP = "4.7.2"
    const val GLIDE = "4.11.0"
    const val JSR_250 = "1.0"

    // Testing dependencies
    const val ESPRESSO = "3.2.0"
    const val JUNIT = "4.13"
    const val ROBOLECTRIC = "4.3.1"
    const val FEST = "2.0M10"
}

object AppConfig {
    const val APPLICATION_ID = "org.jraf.android.cinetoday"
    const val COMPILE_SDK = 29
    const val TARGET_SDK = 29
    const val MIN_SDK = 25

    var buildNumber: Int = 0
    val buildProperties = mutableMapOf<String, String>()
}