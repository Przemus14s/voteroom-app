plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // już masz, ok
}

android {
    namespace = "com.example.voteroom"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.voteroom"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    // Dodaj jeśli używasz Kotlin (opcjonalnie)
    // kotlinOptions {
    //     jvmTarget = "11"
    // }
}

dependencies {
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.github.PhilJay:MPAndroidChart:3.1.0")

    // Firebase BOM - zarządza wersjami Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.14.0"))

    // Firebase Messaging
    implementation("com.google.firebase:firebase-messaging")

    // Lifecycle process (do ProcessLifecycleOwner)
    implementation("androidx.lifecycle:lifecycle-process:2.6.1")

    // AndroidX i inne biblioteki (zakładam, że libs.* są poprawnie zdefiniowane)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.mpandroidchart)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
