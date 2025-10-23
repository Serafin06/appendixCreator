package pl.rafapp.appendixCreator.Data.DataBase

import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import pl.rafapp.appendixCreator.Data.BudynekEntity
import pl.rafapp.appendixCreator.Data.MaterialEntity
import java.util.Properties

object HibernateConfig {

    val sessionFactory: SessionFactory by lazy {
        try {
            val configuration = Configuration()

            // Properties z kodu (nie potrzebujesz hibernate.cfg.xml!)
            configuration.properties = createProperties()

            // Dodaj encje
            configuration.addAnnotatedClass(BudynekEntity::class.java)
            configuration.addAnnotatedClass(MaterialEntity::class.java)
            // TODO: Dodaj PracaEntity i PracaMaterialEntity jak je stworzysz

            configuration.buildSessionFactory()
        } catch (ex: Throwable) {
            throw ExceptionInInitializerError("Błąd inicjalizacji Hibernate: ${ex.message}")
        }
    }

    private fun createProperties(): Properties {
        return Properties().apply {
            // Database connection (ZMIEŃ NA SWOJE DANE SUPABASE!)
            setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
            setProperty("hibernate.connection.url", "jdbc:postgresql://db.TWOJ_PROJECT.supabase.co:5432/postgres")
            setProperty("hibernate.connection.username", "postgres")
            setProperty("hibernate.connection.password", "TWOJE_HASLO")

            // Hibernate settings
            setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
            setProperty("hibernate.hbm2ddl.auto", "validate") // validate = sprawdza czy tabele istnieją
            setProperty("hibernate.show_sql", "true") // pokaż SQL w konsoli
            setProperty("hibernate.format_sql", "true") // formatuj SQL czytelnie

            // Connection pool (HikariCP)
            setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider")
            setProperty("hibernate.hikari.minimumIdle", "5")
            setProperty("hibernate.hikari.maximumPoolSize", "20")
            setProperty("hibernate.hikari.idleTimeout", "300000")
        }
    }

    fun shutdown() {
        sessionFactory.close()
    }
}