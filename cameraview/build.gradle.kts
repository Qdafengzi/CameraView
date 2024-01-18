plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
    id("jacoco")
}

android {
    namespace  = "com.picupmedia.cameraview"

    compileSdk = property("compileSdkVersion") as Int
    defaultConfig {
        minSdk = property("minSdkVersion") as Int
        targetSdk = property("targetSdkVersion") as Int
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["filter"] = "" +
                "com.picupmedia.cameraview.tools.SdkExcludeFilter," +
                "com.picupmedia.cameraview.tools.SdkIncludeFilter"
    }
    buildTypes["debug"].isTestCoverageEnabled = true
    buildTypes["release"].isMinifyEnabled = false

    @Suppress("UnstableApiUsage")
    buildFeatures {
        viewBinding  =true
        dataBinding  = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-inline:2.28.2")

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("org.mockito:mockito-android:2.28.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    api("androidx.exifinterface:exifinterface:1.3.7")
    api("androidx.lifecycle:lifecycle-common:2.3.1")
    api("com.google.android.gms:play-services-tasks:18.1.0")
    implementation("androidx.annotation:annotation:1.7.1")
    implementation("com.otaliastudios.opengl:egloo:0.6.1")
}
