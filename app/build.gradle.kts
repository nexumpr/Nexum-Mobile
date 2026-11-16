
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "mx.nexum.app"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    val runNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
    val computedVersionCode = if (System.getenv("GITHUB_RUN_NUMBER") != null) {
        100 + runNumber
    } else {
        2
    }

    defaultConfig {
        applicationId = "com.nexum.nexum"
        minSdk = 26
        targetSdk = 36
        versionCode = computedVersionCode
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Configuración de firma para Google Play
    val keystoreFile = System.getenv("KEYSTORE_FILE")

    if (keystoreFile != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystoreFile)

                storePassword = requireNotNull(
                    System.getenv("KEYSTORE_PASSWORD")
                ) {
                    "Falta la variable KEYSTORE_PASSWORD"
                }

                keyAlias = requireNotNull(
                    System.getenv("KEY_ALIAS")
                ) {
                    "Falta la variable KEY_ALIAS"
                }

                keyPassword = requireNotNull(
                    System.getenv("KEY_PASSWORD")
                ) {
                    "Falta la variable KEY_PASSWORD"
                }
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }

            if (keystoreFile != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
