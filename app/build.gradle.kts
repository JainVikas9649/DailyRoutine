plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
     alias(libs.plugins.google.gms.google.services)
 }



android {

    signingConfigs {
        create("release") {
            storeFile = file("/Users/sunilsharma/Desktop/DailyRoutineJKS/dailyroutine.jks")
            storePassword = "dailyroutine.123"
            keyAlias = "dailyRoutine"
            keyPassword = "dailyroutine.123"
        }
    }
    namespace = "app.dailyroutine"
    compileSdk = 35
    defaultConfig {
        applicationId = "app.dailyroutine"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = false

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
    buildFeatures {
        viewBinding = true
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

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.auth)
     testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.gson)
    implementation (libs.cardview)
    implementation  (libs.lottie)
    implementation(libs.mpandroidchart)
    implementation (libs.itextg)
    implementation (libs.work.runtime)
    implementation(libs.places)
     implementation (libs.prolificinteractive.material.calendarview)
    implementation (libs.biometric)
    implementation (libs.glide)
    annotationProcessor (libs.compiler)
    implementation (libs.play.services.auth)
    implementation(platform(libs.firebase.bom))
    implementation (libs.play.services.location)
    implementation(libs.firebase.analytics)
    implementation (libs.work.runtime.v290)
    implementation (libs.firebase.auth.v2231)
            implementation (libs.play.services.ads)
    implementation (libs.material.v1100)
    // Location services
    implementation (libs.play.services.location.v2101)
    implementation (libs.play.services.maps)
    implementation("com.google.android.libraries.places:places:3.4.0")
    // Image loading (optional)
    implementation (libs.glide.v4151)
    annotationProcessor (libs.compiler.v4151)
   }