import com.android.build.gradle.internal.tasks.factory.dependsOn

private val readAndUnderstoodLicense = false

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "io.github.domi04151309.alwayson"
    compileSdk = 34

    defaultConfig {
        applicationId = "io.github.domi04151309.alwayson"
        minSdk = 24
        //noinspection EditedTargetSdkVersion
        targetSdk = 34
        versionCode = 391
        versionName = "3.9.1"
    }

    buildTypes {
        debug {
            isMinifyEnabled = true
            isShrinkResources = true
            applicationIdSuffix = ".debug"
            versionNameSuffix = " debug"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    buildFeatures {
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    project.tasks.preBuild.dependsOn("license")
}

tasks.register("license") {
    doFirst {
        val data =
            file("./src/main/res/xml/pref_about.xml")
                .readText()
                .contains("app:key=\"license\"")
        if (!data) {
            throw Exception(
                "Please note that removing the license from the about page is not allowed if you " +
                    "plan to publish your modified version of this app. " +
                    "Please read the project's LICENSE.",
            )
        }
        if (!(
                android.defaultConfig.applicationId?.contains("domi04151309") == true ||
                    readAndUnderstoodLicense
            )
        ) {
            throw Exception(
                "Please make sure you have read and understood the LICENSE!",
            )
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.jaredrummler:colorpicker:1.1.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.android.volley:volley:1.2.1")
}
