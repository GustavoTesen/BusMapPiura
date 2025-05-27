plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") version "4.4.2"
}

android {
    namespace = "com.example.busmappiura"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.busmappiura"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions { jvmTarget = "1.8" }

    buildFeatures { compose = true }
    composeOptions {
        // Compatible con Kotlin 1.9.10
        kotlinCompilerExtensionVersion = "1.5.3"
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    // Firebase BOM que arrastra Auth 22.3.1
    implementation(platform("com.google.firebase:firebase-bom:32.4.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation ("com.google.firebase:firebase-firestore-ktx:25.1.4")
    //google maps//
    implementation ("com.google.android.gms:play-services-maps:19.2.0")

    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation ("com.google.android.material:material:1.12.0")
        // ... otras dependencias




    // AndroidX
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.0")
    implementation("androidx.activity:activity-compose:1.10.1")

    // Jetpack Compose BOM + UI
    implementation(platform("androidx.compose:compose-bom:2023.08.00"))
    implementation("androidx.compose.ui:ui")                   // 1.5.x
    implementation("androidx.compose.ui:ui-graphics")          // 1.5.x
    implementation("androidx.compose.ui:ui-tooling-preview")   // 1.5.x
    implementation("androidx.compose.material3:material3")     // 1.0.x

    // Soporte legacy UI
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Compose UI tests
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.08.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
