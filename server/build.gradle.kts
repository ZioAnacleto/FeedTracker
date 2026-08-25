plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.21"
    application
}

group = "com.zioanacleto.feedtracker"
version = "1.0.0"

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("com.zioanacleto.feedtracker.ApplicationKt")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=false")
}

ktor {
    development.set(false)
    fatJar {
        archiveFileName.set("feedtracker-server.jar")
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.kotlin.logging)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.serverContentNegotiation)
    implementation(libs.ktor.serializationKotlinxJson)
    implementation(libs.ktor.serverStatusPages)
    implementation(libs.ktor.serverDefaultHeaders)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.postgresql)
    implementation(libs.hikaricp)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)
    implementation(libs.kotlinx.coroutines)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.h2)
    testImplementation(libs.exposed.jdbc)
}

tasks.test {
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    systemProperty("io.ktor.development", "false")
    val sharedJvmClasses = project(":shared").layout.buildDirectory.dir("classes/kotlin/jvm/main")
    classpath = files(sharedJvmClasses) + classpath
}
