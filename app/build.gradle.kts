plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdkVersion(AppConfig.COMPILE_SDK)

    defaultConfig {
        applicationId = AppConfig.APPLICATION_ID
        minSdkVersion(AppConfig.MIN_SDK)
        targetSdkVersion(AppConfig.TARGET_SDK)
        versionCode = AppConfig.buildNumber
        versionName = AppConfig.buildProperties["versionName"]

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // For now we enable strict mode for all the configs
        buildConfigField("boolean", "STRICT_MODE", "true")
        // For now we enable debug logs all the configs
        buildConfigField("boolean", "DEBUG_LOGS", "true")

        resConfigs("en", "fr")

        // Useful for api keys in the manifest (Maps, Crashlytics, ...)
        manifestPlaceholders = AppConfig.buildProperties as Map<String, Any>

        // Setting this to true disables the png generation at buildtime
        // (see http://android-developers.blogspot.fr/2016/02/android-support-library-232.html)
        vectorDrawables.useSupportLibrary = true
    }

    signingConfigs {
        create("release") {
            storeFile = file(AppConfig.buildProperties["signingStoreFile"]!!)
            storePassword = AppConfig.buildProperties["signingStorePassword"]
            keyAlias = AppConfig.buildProperties["signingKeyAlias"]
            keyPassword = AppConfig.buildProperties["signingKeyPassword"]
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "GIT_SHA1", "\"dev\"")
            buildConfigField("String", "BUILD_DATE", "\"dev\"")
        }

        getByName("release") {
            buildConfigField("String", "GIT_SHA1", "\"${getGitSha1()}\"")
            buildConfigField("String", "BUILD_DATE", "\"${buildDate}\"")

            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        dataBinding = true
    }

    lintOptions {
        isAbortOnError = true
        textReport = true
        isIgnoreWarnings = true
    }

    compileOptions {
        incremental = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
    }
}

//android.applicationVariants.all { variant ->
//    // Create new copy tasks, for release builds
//    if (variant.buildType.name == 'release') {
//        variant.outputs.each { output ->
//            def outputFile = file("build/outputs/apk/${variant.flavorName}/release/$output.outputFileName")
//            def apkName = "${globalProjectName}-${project.name}-${android.defaultConfig.versionCode}-${variant.flavorName}-signed.apk"
//
//            // Copy the apk to the 'etc' folder
//            def copyApkToEtc = tasks.create("copy${variant.name.capitalize()}ApkToEtc", Copy)
//            copyApkToEtc.from(outputFile)
//            copyApkToEtc.into('../etc/apk')
//            copyApkToEtc.rename output.outputFileName, apkName
//
//            // Copy the apk to the deploy folder
//            def deployFolder = "${buildProperties.deployFolder}/$globalProjectName/x"
//            def copyApkToDeploy = tasks.create("copy${variant.name.capitalize()}ApkToDeploy", Copy)
//            copyApkToDeploy.from(outputFile)
//            copyApkToDeploy.into(deployFolder)
//            copyApkToDeploy.rename output.outputFileName, apkName
//
//            // Make the copy tasks run after the assemble tasks of the variant
//            variant.assembleProvider.get().finalizedBy(copyApkToEtc, copyApkToDeploy)
//        }
//    }
//}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib", Versions.KOTLIN))

    // AndroidX
    implementation("androidx.fragment:fragment-ktx:${Versions.ANDROIDX_FRAGMENT}")
    implementation("androidx.annotation:annotation:${Versions.ANDROIDX_ANNOTATION}")
    implementation("androidx.percentlayout:percentlayout:${Versions.ANDROIDX_PERCENT_LAYOUT}")
    implementation("androidx.palette:palette:${Versions.ANDROIDX_PALETTE}")
    implementation("androidx.preference:preference-ktx:${Versions.ANDROIDX_PREFERENCE}")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:${Versions.ANDROIDX_LIFECYCLE}")
    implementation("androidx.room:room-runtime:${Versions.ANDROIDX_ROOM}")
    kapt("androidx.room:room-compiler:${Versions.ANDROIDX_ROOM}")
    implementation("androidx.room:room-ktx:${Versions.ANDROIDX_ROOM}")
    implementation("androidx.constraintlayout:constraintlayout:${Versions.ANDROIDX_CONSTRAINT_LAYOUT}")
    implementation("androidx.work:work-runtime-ktx:${Versions.ANDROIDX_WORK_MANAGER}")

    //  Wear
    implementation("com.google.android.support:wearable:${Versions.ANDROID_WEARABLE}")
    implementation("com.google.android.wearable:wearable:${Versions.ANDROID_WEARABLE}")
    implementation("androidx.wear:wear:${Versions.ANDROIDX_WEAR}")

    // JRAF
    implementation("org.jraf:kprefs:${Versions.KPREFS}")
    implementation("com.github.BoD:jraf-android-util:-SNAPSHOT")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:${Versions.OK_HTTP}")
    implementation("com.squareup.okhttp3:logging-interceptor:${Versions.OK_HTTP}")

    // Glide
    implementation("com.github.bumptech.glide:glide:${Versions.GLIDE}")
    kapt("com.github.bumptech.glide:compiler:${Versions.GLIDE}")
    implementation("com.github.bumptech.glide:okhttp3-integration:${Versions.GLIDE}")

    // Dagger
    implementation("com.google.dagger:dagger:${Versions.DAGGER}")
    kapt("com.google.dagger:dagger-compiler:${Versions.DAGGER}")
    compileOnly("javax.annotation:jsr250-api:${Versions.JSR_250}")

    // Rx
    implementation("io.reactivex.rxjava2:rxandroid:${Versions.RX_ANDROID}")
    implementation("io.reactivex.rxjava2:rxjava:${Versions.RX_JAVA}")


    // Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:${Versions.ESPRESSO}") {
        exclude(group = "com.android.support", module = "support-annotations")
    }
    testImplementation("junit:junit:${Versions.JUNIT}")
    testImplementation("org.robolectric:robolectric:${Versions.ROBOLECTRIC}")
    testImplementation("org.easytesting:fest-assert-core:${Versions.FEST}")
}
