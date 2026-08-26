import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.ktlint) apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21" apply false
}

val ktlintPluginId = libs.plugins.ktlint.get().pluginId

subprojects {
    pluginManager.apply(ktlintPluginId)

    extensions.configure<KtlintExtension> {
        version.set("1.7.1")
        android.set(false)
        ignoreFailures.set(false)
        filter {
            exclude { element ->
                element.file.path.contains("/build/") ||
                    element.file.path.contains("/generated/")
            }
        }
    }
}