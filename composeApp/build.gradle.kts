import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21"
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(JvmTarget.JVM_11)
            }
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm {
        mainRun {
            mainClass.set("com.zioanacleto.feedtracker.DesktopMainKt")
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.core)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.ui.backhandler)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.icons)
            implementation(libs.compose.navigation)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.koin.core)
            implementation(libs.koin.core.viewmodel)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(projects.shared)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
            implementation(libs.turbine)
        }
        androidUnitTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.junit)
            implementation(libs.androidx.testExt.junit)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        jvmTest.dependencies {
            implementation(libs.mockk)
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest.assertions.core)
        }
    }
}

val feedtrackerVersionCode =
    (findProperty("feedtracker.versionCode") as String?)?.toIntOrNull() ?: 1
val feedtrackerVersionName =
    (findProperty("feedtracker.versionName") as String?) ?: "1.0.0"

val androidKeystoreFile = providers.environmentVariable("ANDROID_KEYSTORE_FILE")
val androidKeystorePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD")
val androidKeyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS")
val androidKeyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD")
val androidKeystorePath = androidKeystoreFile.orNull
val canSignAndroidRelease =
    !androidKeystorePath.isNullOrBlank() &&
        androidKeystorePassword.orNull?.isNotBlank() == true &&
        androidKeyAlias.orNull?.isNotBlank() == true &&
        androidKeyPassword.orNull?.isNotBlank() == true &&
        file(androidKeystorePath).exists()

android {
    namespace = "com.zioanacleto.feedtracker"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.zioanacleto.feedtracker"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = feedtrackerVersionCode
        versionName = feedtrackerVersionName
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    if (canSignAndroidRelease) {
        signingConfigs {
            create("release") {
                storeFile = file(androidKeystorePath!!)
                storePassword = androidKeystorePassword.get()
                keyAlias = androidKeyAlias.get()
                keyPassword = androidKeyPassword.get()
            }
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            if (canSignAndroidRelease) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.zioanacleto.feedtracker.DesktopMainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.zioanacleto.feedtracker"
            packageVersion = feedtrackerVersionName
        }

        // packageRelease* enables ProGuard by default, which drops Ktor's
        // JSON ServiceLoader provider and crashes the packaged app on launch.
        buildTypes.release.proguard {
            isEnabled.set(false)
        }
    }
}
