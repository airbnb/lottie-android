
plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}


kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":sample-compose-multiplatform:shared"))

                implementation(compose.desktop.currentOs)
                implementation(compose.runtime)
                implementation(compose.ui)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "Main_desktopKt"
    }
}
