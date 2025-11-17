package pl.rafapp.marko.appendixCreator.config

import io.github.cdimascio.dotenv.dotenv
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import pl.rafapp.marko.appendixCreator.data.entity.*
import java.util.Properties

object DatabaseConfig {

    private val dotenv = dotenv {
        ignoreIfMissing = false
        systemProperties = true
    }

    val sessionFactory: SessionFactory by lazy {
        try {
            Configuration()
                .apply {
                    properties = createHibernateProperties()

                    // Mapowanie encji
                    addAnnotatedClass(BudynekEntity::class.java)
                    addAnnotatedClass(MaterialEntity::class.java)
                    addAnnotatedClass(PracaEntity::class.java)
                    addAnnotatedClass(PracaMaterialEntity::class.java)
                }
                .buildSessionFactory()
        } catch (ex: Throwable) {
            System.err.println("❌ Błąd inicjalizacji bazy danych: ${ex.message}")
            ex.printStackTrace()
            throw ExceptionInInitializerError(ex)
        }
    }

    private fun createHibernateProperties(): Properties {
        val host = dotenv["DB_HOST"]
            ?: System.getenv("DB_HOST")
            ?: throw IllegalStateException("Brak DB_HOST")

        val port = dotenv["DB_PORT"] ?: System.getenv("DB_PORT") ?: "5432"
        val dbName = dotenv["DB_NAME"] ?: System.getenv("DB_NAME") ?: "postgres"
        val user = dotenv["DB_USER"]
            ?: System.getenv("DB_USER")
            ?: throw IllegalStateException("Brak DB_USER")

        val password = dotenv["DB_PASSWORD"]
            ?: System.getenv("DB_PASSWORD")
            ?: throw IllegalStateException("Brak DB_PASSWORD")

        val jdbcUrl = "jdbc:postgresql://$host:$port/$dbName"

        println("🔌 Łączę z bazą: $jdbcUrl")

        return Properties().apply {
            setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
            setProperty("hibernate.connection.url", jdbcUrl)
            setProperty("hibernate.connection.username", user)
            setProperty("hibernate.connection.password", password)

            setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            setProperty("hibernate.hbm2ddl.auto", "validate")
            setProperty("hibernate.show_sql", "true")
            setProperty("hibernate.format_sql", "true")

            // HikariCP
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