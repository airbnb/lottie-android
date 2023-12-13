import com.vanniktech.maven.publish.SonatypeHost

plugins {
  id("com.android.library")
  id("org.jetbrains.kotlin.multiplatform")
  id("com.vanniktech.maven.publish")
  id("androidx.baselineprofile")
  id("org.jetbrains.compose")
}

kotlin {
    androidTarget {}

    jvm("desktop")

    iosArm64()
    iosX64()
    iosSimulatorArm64()

    js(IR) {
        browser()
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(compose.foundation)
                implementation(compose.ui)
            }
        }
        val androidMain by getting {
            dependencies {
                api(project(":lottie"))
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.robolectric)
                implementation(libs.androidx.collection.ktx)
                implementation(libs.kotlinx.coroutines.test)
                implementation(libs.junit4)
                implementation(libs.androidx.test.junit)
                implementation(libs.androidx.test.espresso)
            }
        }

        val desktopMain by getting
        val jsMain by getting

        val iosArm64Main by getting
        val iosX64Main by getting
        val iosSimulatorArm64Main by getting

        val iosMain by creating {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosX64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }

        val skikoMain by creating {
            dependsOn(commonMain)
            desktopMain.dependsOn(this)
            jsMain.dependsOn(this)
            iosMain.dependsOn(this)
        }
    }
}

dependencies {
    baselineProfile(project(":baselineprofile"))
}

android {
  namespace = "com.airbnb.lottie.compose"
  compileSdk = 34
  defaultConfig {
    minSdk = 21
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }
  buildTypes {
    release {
      isMinifyEnabled = false
    }
  }
}

mavenPublishing {
  publishToMavenCentral(SonatypeHost.DEFAULT)
  signAllPublications()
}

baselineProfile {
  filter {
    include("com.airbnb.lottie.compose.**")
  }
}
