package pl.rafapp.appendixCreator.dataBase

import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

object HibernateUtil {
    val sessionFactory: SessionFactory by lazy {
        try {
            Configuration()
                .configure("hibernate.cfg.xml")
                .buildSessionFactory()
        } catch (ex: Throwable) {
            throw ExceptionInInitializerError("Błąd inicjalizacji SessionFactory: ${ex.message}")
        }
    }

    fun shutdown() {
        sessionFactory.close()
    }
}