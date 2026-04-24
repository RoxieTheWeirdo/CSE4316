import java.util.Properties
import java.io.FileInputStream

val camerax_version = "1.3.4"

val properties = Properties()
// NOTE: Do NOT hardcode secrets here.
// Keep for sprint demo, but move to local.properties / env later.
val fatsecretConsumerKey = "XXXXXXXXXXXXX"
val fatsecretConsumerSecret = "XXXXXXX"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fitbite"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.fitbite"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "FATSECRET_CONSUMER_KEY", "\"${fatsecretConsumerKey.trim()}\"")
        buildConfigField("String", "FATSECRET_CONSUMER_SECRET", "\"${fatsecretConsumerSecret.trim()}\"")
        buildConfigField("String", "CLARIFAI_API_KEY", "\"\"")
        buildConfigField("String", "CLARIFAI_WORKFLOW_ID", "\"FoodImageModel\"")
        buildConfigField("String", "CLARIFAI_WORKFLOW_VERSION", "\"\"")
        buildConfigField("String", "CLARIFAI_API_KEY", "\"XXXXXXX\"")
        buildConfigField("String", "CLARIFAI_WORKFLOW_VERSION", "\"XXXXXXXX\"")
    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    kotlin {
        jvmToolchain(21)
    }


}

dependencies {
    // Compose BOM
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core UI deps
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.core.ktx)

    // Compose
    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation("com.google.android.material:material:1.11.0")

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth:23.0.0")
    implementation("com.google.firebase:firebase-analytics:22.0.2")

    // Google Sign-in / Credentials
    implementation("com.google.android.gms:play-services-auth:21.1.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // CameraX
    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-video:$camerax_version")
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    // Navigation / Layout
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Networking
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Misc (from main)
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.17")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // keep ONLY ONE material dependency (you already have libs.material above)
    implementation("com.google.android.material:material:1.12.0")

    implementation("nl.dionsegijn:konfetti-xml:2.0.4")
    implementation("nl.dionsegijn:konfetti-compose:2.0.4")
    implementation("nl.dionsegijn:konfetti-core:2.0.4")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // New dependencies from main
    implementation("com.github.haifengl:smile-core:2.6.0")
    implementation("com.google.android.gms:play-services-fitness:21.2.0")
    implementation("androidx.work:work-runtime:2.9.0")
    implementation("com.google.firebase:firebase-messaging:23.4.1")
}