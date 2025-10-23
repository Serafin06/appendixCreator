import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

group = "pl.rafapp.appendixCreator"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    // Compose Desktop
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)

    // Hibernate
    implementation("org.hibernate:hibernate-core:6.6.4.Final")

    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.7.4")

    // Connection Pool
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // Testy
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "pl.rafapp.appendixCreator.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "AppendixCreator"
            packageVersion = "1.0.0"

            description = "System zarządzania budynkami i pracami"
            copyright = "© 2025 RafApp"
            vendor = "RafApp"
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(24)
}