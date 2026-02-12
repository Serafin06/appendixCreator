plugins {
    kotlin("jvm") version "2.2.20"
}

group = "pl.rafapp.marko.tools"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Excel
    implementation("org.apache.poi:poi:5.3.0")
    implementation("org.apache.poi:poi-ooxml:5.3.0")

    // PostgreSQL
    implementation("org.postgresql:postgresql:42.7.4")

    // Dotenv - żeby używać tego samego .env co główna aplikacja
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")
}

kotlin {
    jvmToolchain(21)
}

tasks.register<JavaExec>("importExcel") {
    group = "tools"
    description = "Import materiałów z pliku Excel do bazy danych"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("ImportExcelKt")

    // Przekaż ścieżkę do pliku jako argument: ./gradlew :tools:importExcel --args="sciezka/do/pliku.xlsx"
    args = project.findProperty("excelFile")?.toString()?.let { listOf(it) } ?: emptyList()
}