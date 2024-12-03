import java.util.Properties
plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("realm-android")
}
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

val appId: String = localProperties.getProperty("appId")
val mapsApiKey: String = localProperties.getProperty("MAPS_API_KEY") ?: ""

apply(plugin = "realm-android")
android {
    namespace = "com.example.hackathon"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.hackathon"
        minSdk = 24
        //noinspection OldTargetApi
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "appId", "\"$appId\"")
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
    }

    buildFeatures{
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
realm {
    isSyncEnabled = true
}
dependencies {
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("androidx.webkit:webkit:1.8.0")
    implementation ("com.google.android.gms:play-services-location:21.2.0")
    implementation (libs.volley)
    implementation (libs.cardview)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}