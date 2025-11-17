package pl.rafapp.marko.appendixCreator

import pl.rafapp.marko.appendixCreator.config.DatabaseConfig

fun main() {
    println("=== Test połączenia z bazą danych ===")

    try {
        // Próba utworzenia SessionFactory
        val sessionFactory = DatabaseConfig.sessionFactory

        // Test zapytania
        val session = sessionFactory.openSession()
        val count = session.createNativeQuery(
            "SELECT COUNT(*) FROM budynki",
            Long::class.java
        ).singleResult

        println("✅ Połączenie udane!")
        println("📊 Liczba budynków w bazie: $count")

        session.close()
        DatabaseConfig.shutdown()

    } catch (e: Exception) {
        println("❌ Błąd połączenia: ${e.message}")
        e.printStackTrace()
    }
}