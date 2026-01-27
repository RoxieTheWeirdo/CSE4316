import java.util.Properties
import java.io.FileInputStream

val camerax_version = "1.3.4"

val properties = Properties()
val fatsecretConsumerKey = "d93a1c71a19841cfbbef59d49370fe3e"
val fatsecretConsumerSecret = "685ad09e5b5942ba9c6764ad750825a7"

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
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
        buildConfigField("String", "CLARIFAI_API_KEY", "\"3254255dfab8489dbb2c2b552481f600\"")
        buildConfigField("String", "CLARIFAI_WORKFLOW_ID", "\"FoodImageModel\"")
        buildConfigField("String", "CLARIFAI_WORKFLOW_VERSION", "\"034d2b10314c4e30892e6ffcbe9af4c0\"")

    }

    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform(libs.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation(libs.activity.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.material3)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.material3)

    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("androidx.camera:camera-core:$camerax_version")
    implementation("androidx.camera:camera-camera2:$camerax_version")
    implementation("androidx.camera:camera-lifecycle:$camerax_version")
    implementation("androidx.camera:camera-view:$camerax_version")
    implementation("androidx.camera:camera-video:$camerax_version")

    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
}
