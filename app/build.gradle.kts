plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}


android {
    namespace = "com.live.azurah"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.live.azurah"
        minSdk = 24
        targetSdk = 36
        versionCode = 12
        versionName = "1.1.1"
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

    buildFeatures{
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

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.activity:activity-ktx:1.10.1")
    implementation("androidx.compose.ui:ui-geometry-android:1.9.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
    implementation("com.intuit.ssp:ssp-android:1.1.0")
    implementation("com.intuit.sdp:sdp-android:1.1.0")
//    implementation("com.github.mukeshsolanki.android-otpview-pinview:otpview:3.1.0")
    implementation("com.github.aabhasr1:OtpView:v1.1.1-ktx")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")
//    implementation("com.github.smarteist:autoimageslider:1.4.0")
    implementation("com.github.Hassaan-Javed:gowtham-video-trimmer-fork:1.2.0")
//    implementation("com.github.a914-gowtham:android-video-trimmer:1.7.19")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.github.yalantis:ucrop:2.2.11")
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    implementation("com.github.pgreze:android-reactions:1.6")
    implementation("com.hbb20:ccp:2.7.3")
    implementation("com.github.sheetalkumar105:ZoomImageView-android:1.02")
    implementation("com.jsibbold:zoomage:1.3.1")
//    implementation("com.theartofdev.edmodo:android-image-cropper:2.8.0")
 /*   implementation("com.hbb20:android-country-picker:0.0.7")
    implementation("com.hbb20:android-country-picker-flagpack1:0.0.7")*/
    implementation("com.tbuonomo:dotsindicator:5.1.0")

    implementation("com.google.dagger:hilt-android:2.48.1")
    kapt("com.google.dagger:hilt-compiler:2.48.1")

    implementation("androidx.media3:media3-exoplayer:1.0.1")
    implementation("androidx.media3:media3-ui:1.0.1")
    implementation("androidx.media3:media3-common:1.0.1")
    implementation("androidx.media3:media3-session:1.0.1")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    //viewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation("android.arch.lifecycle:extensions:1.1.1")
    //activity-ktx
    implementation("androidx.activity:activity-ktx:1.9.1")
    implementation("androidx.fragment:fragment-ktx:1.8.2")

    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    implementation("io.socket:socket.io-client:2.1.0") {
        exclude(group = "org.json", module = "json")
    }
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation("com.github.skydoves:balloon:1.6.4")
    implementation("com.google.firebase:firebase-messaging-ktx:24.1.0")
    implementation("com.android.billingclient:billing:7.1.1") // or your chosen version

//    implementation("com.google.firebase:firebase-analytics")
}