plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.mhtour.audio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.mhtour.audio"
        minSdk = 26
        targetSdk = 35
        versionCode = 32
        versionName = "5.1-buttons-fixed"

        val apiUrl = (project.findProperty("MH_TOUR_API_BASE_URL") as String?)?.trim().orEmpty()
        buildConfigField("String", "MH_TOUR_API_BASE_URL", "\"$apiUrl\"")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    // Release signing is supplied only by Gradle properties in CI.
    // Never commit a keystore or passwords to GitHub.
    val keystoreFile = project.findProperty("MH_KEYSTORE_FILE") as String?
    val keystorePassword = project.findProperty("MH_KEYSTORE_PASSWORD") as String?
    val keyAlias = project.findProperty("MH_KEY_ALIAS") as String?
    val keyPassword = project.findProperty("MH_KEY_PASSWORD") as String?

    if (!keystoreFile.isNullOrBlank() && !keystorePassword.isNullOrBlank() &&
        !keyAlias.isNullOrBlank() && !keyPassword.isNullOrBlank()) {
        signingConfigs.create("ciRelease") {
            storeFile = file(keystoreFile)
            storePassword = keystorePassword
            this.keyAlias = keyAlias
            this.keyPassword = keyPassword
        }
    }

    buildTypes {
        release {
            val ciSigning = signingConfigs.findByName("ciRelease")
            if (ciSigning != null) signingConfig = ciSigning
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-ktx:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("io.livekit:livekit-android:2.28.2")
}
