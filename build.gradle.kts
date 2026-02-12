plugins {
    kotlin("jvm") version "2.2.20"
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

group = "pl.rafapp.marko.appendixCreator"
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

    // Hibernate
    implementation("org.hibernate:hibernate-core:6.6.4.Final")
    implementation("org.hibernate.orm:hibernate-hikaricp:6.6.4.Final")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.4")

    // HikariCP (connection pool)
    implementation("com.zaxxer:HikariCP:6.2.1")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.16")

    // Dotenv - do ładowania .env
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // import to excel i pdf
    implementation("org.apache.poi:poi:5.3.0")
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // Testy
    testImplementation(kotlin("test"))
}

compose.desktop {
    application {
        mainClass = "pl.rafapp.marko.appendixCreator.MainKt"
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}