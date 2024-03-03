import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("dagger.hilt.android.plugin")
    id("kotlin-kapt")
}

android {
    namespace = "com.navirotation"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.navirotation"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        fun key(pKey:String): String = gradleLocalProperties(rootDir).getProperty(pKey) ?: ""
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", key("KAKAO_NATIVE_APP_KEY"))
        buildConfigField("String", "KAKAO_REST_API_KEY", key("KAKAO_REST_API_KEY"))
        buildConfigField("String", "SK_APP_KEY", key("SK_APP_KEY"))
        buildConfigField("String", "USER_KEY", key("USER_KEY"))
        buildConfigField("String", "SK_BASE_URL", key("SK_BASE_URL"))
        buildConfigField("String", "KAKAO_BASE_URL", key("KAKAO_BASE_URL"))
        buildConfigField("String", "NAVER_CLIENT_ID", key("NAVER_CLIENT_ID"))

        manifestPlaceholders["NAVER_CLIENT_ID"] = key("NAVER_CLIENT_ID")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        buildConfig = true
    }
}

val hiltVersion = 2.44


dependencies {

    implementation("androidx.core:core-ktx:1.10.1")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // kakao navi
    implementation("com.kakaomobility.knsdk:knsdk_ui:1.9.4")

    // Naver Map
    implementation("com.naver.maps:map-sdk:3.17.0")

    // 위치 정보, 등록을 하면 Navigation이랑 충돌이 나면서 크래시 발생
//    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Hilt
    implementation ("com.google.dagger:hilt-android:$hiltVersion")
    kapt ("com.google.dagger:hilt-android-compiler:$hiltVersion")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}