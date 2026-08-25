rootProject.name = "FeedTracker"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val serverOnlyBuild =
    (providers.gradleProperty("feedtracker.serverOnly").orNull
        ?: providers.environmentVariable("FEEDTRACKER_SERVER_ONLY").orNull)
        ?.equals("true", ignoreCase = true) == true

include(":server")
include(":shared")
if (!serverOnlyBuild) {
    include(":composeApp")
}