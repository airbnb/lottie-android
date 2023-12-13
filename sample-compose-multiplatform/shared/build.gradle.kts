plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
}

kotlin {

    androidTarget {}
    jvm("desktop")

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":lottie-compose"))
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
            }
        }
    }
}

android {
    namespace = "com.airbnb.lottie.sample.compose"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
}
