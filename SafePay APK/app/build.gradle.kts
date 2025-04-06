plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id ("kotlin-kapt")
    id ("kotlin-parcelize")
}



android {
    namespace = "aarambh.apps.safepay_apk"
    compileSdk = 35

    defaultConfig {
        applicationId = "aarambh.apps.safepay_apk"
        minSdk = 28
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    //text dimension
    implementation("com.intuit.sdp:sdp-android:1.1.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")

    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    //lifecycle
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth-ktx")
    // implementation("com.google.firebase:firebase-auth")
    implementation("com.google.android.gms:play-services-auth-api-phone:18.0.1")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.firebase:firebase-storage:20.2.1")

    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.0")

    implementation ("com.github.bumptech.glide:glide:4.16.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0") // If you're using Kotlin

    implementation ("androidx.camera:camera-core:1.3.1")
    implementation ("androidx.camera:camera-camera2:1.3.1")
    implementation ("androidx.camera:camera-lifecycle:1.3.1")
    implementation ("androidx.camera:camera-view:1.3.1")
    implementation ("androidx.camera:camera-video:1.3.0")

}