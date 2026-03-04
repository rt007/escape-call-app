plugins {
    id("com.android.application")
}

android {
    namespace = "com.escapecall"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.escapecall"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
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


}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // OkHttp for network requests (lightweight, no Retrofit overhead for 1 endpoint)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
