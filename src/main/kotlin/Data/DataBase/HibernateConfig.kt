package pl.rafapp.appendixCreator.Data.database

import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import pl.rafapp.appendixCreator.Data.*
import java.util.Properties

object HibernateConfig {

    val sessionFactory: SessionFactory by lazy {
        try {
            val configuration = Configuration()

            configuration.properties = createProperties()

            // Wszystkie encje
            configuration.addAnnotatedClass(BudynekEntity::class.java)
            configuration.addAnnotatedClass(MaterialEntity::class.java)
            configuration.addAnnotatedClass(PracaEntity::class.java)
            configuration.addAnnotatedClass(PracaMaterialEntity::class.java)

            configuration.buildSessionFactory()
        } catch (ex: Throwable) {
            throw ExceptionInInitializerError("Błąd inicjalizacji Hibernate: ${ex.message}")
        }
    }

    private fun createProperties(): Properties {
        return Properties().apply {
            // Database connection
            setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
            setProperty("hibernate.connection.url",
                System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres")
            setProperty("hibernate.connection.username",
                System.getenv("DB_USER") ?: "postgres")
            setProperty("hibernate.connection.password",
                System.getenv("DB_PASSWORD") ?: "")

            // Hibernate settings
            setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            setProperty("hibernate.hbm2ddl.auto", "validate")
            setProperty("hibernate.show_sql", "true")
            setProperty("hibernate.format_sql", "true")

            // Connection pool
            setProperty("hibernate.connection.provider_class",
                "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
            setProperty("hibernate.hikari.minimumIdle", "5")
            setProperty("hibernate.hikari.maximumPoolSize", "20")
            setProperty("hibernate.hikari.idleTimeout", "300000")
        }
    }

    fun shutdown() {
        sessionFactory.close()
    }
}