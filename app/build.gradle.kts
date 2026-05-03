import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use {
        localProperties.load(it)
    }
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val isReleaseTask = project.gradle.startParameter.taskNames.any { it.contains("release", ignoreCase = true) }

android {

    namespace = "vn.io.litever.remind.app"

    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "vn.io.litever.remind"
        minSdk = 26
        targetSdk = 36
        versionCode = libs.versions.app.versionCode.get().toInt()
        versionName = libs.versions.app.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        register("release") {
            keyAlias = localProperties.getProperty("LITEVER_KEYSTORE_ALIAS")
            keyPassword = localProperties.getProperty("LITEVER_KEYSTORE_ALIAS_PASSWORD")
            storeFile = file(localProperties.getProperty("LITEVER_KEYSTORE_PATH"))
            storePassword = localProperties.getProperty("LITEVER_KEYSTORE_PASSWORD")
        }
    }

    buildTypes {
        debug {
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "false"
        }
        release {
            isMinifyEnabled = true
            manifestPlaceholders["crashlyticsCollectionEnabled"] = "true"
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        viewBinding = false
    }
    buildToolsVersion = "36.0.0"
}

dependencies {
    implementation(project(":features:alarms"))
    implementation(project(":features:settings"))
    implementation(project(":features:mission"))
    implementation(project(":features:remind"))
    implementation(project(":core:network"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(project(":core:model"))
    implementation(project(":core:alarm"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.google.play.services.ads)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}



