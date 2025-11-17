package pl.rafapp.marko.appendixCreator.config

import io.github.cdimascio.dotenv.dotenv
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import java.util.Properties

object DatabaseConfig {

    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val sessionFactory: SessionFactory by lazy {
        try {
            Configuration()
                .apply { properties = createHibernateProperties() }
                .buildSessionFactory()
        } catch (ex: Throwable) {
            System.err.println("❌ Błąd inicjalizacji bazy danych: ${ex.message}")
            ex.printStackTrace()
            throw ExceptionInInitializerError(ex)
        }
    }

    private fun createHibernateProperties(): Properties {
        val host = dotenv["DB_HOST"] ?: throw IllegalStateException("Brak DB_HOST w .env")
        val port = dotenv["DB_PORT"] ?: "5432"
        val dbName = dotenv["DB_NAME"] ?: "postgres"
        val user = dotenv["DB_USER"] ?: throw IllegalStateException("Brak DB_USER w .env")
        val password = dotenv["DB_PASSWORD"] ?: throw IllegalStateException("Brak DB_PASSWORD w .env")

        val jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"

        println("🔌 Łączę z bazą: $jdbcUrl")
        println("👤 Użytkownik: $user")

        return Properties().apply {
            // Connection
            setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
            setProperty("hibernate.connection.url", jdbcUrl)
            setProperty("hibernate.connection.username", user)
            setProperty("hibernate.connection.password", password)

            // Hibernate
            setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            setProperty("hibernate.hbm2ddl.auto", "validate") // tylko walidacja, nie modyfikuj tabel
            setProperty("hibernate.show_sql", "true")
            setProperty("hibernate.format_sql", "true")

            // Connection pool (HikariCP)
            setProperty("hibernate.connection.provider_class",
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
            setProperty("hibernate.hikari.minimumIdle", "2")
            setProperty("hibernate.hikari.maximumPoolSize", "10")
            setProperty("hibernate.hikari.connectionTimeout", "20000")
        }
    }

    fun shutdown() {
        if (sessionFactory.isOpen) {
            sessionFactory.close()
            println("🔌 Połączenie z bazą zamknięte")
        }
    }
}