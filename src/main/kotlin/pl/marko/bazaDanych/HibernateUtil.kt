package pl.marko.bazaDanych

import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration

object HibernateUtil {
    val sessionFactory: SessionFactory = Configuration()
        .configure("resources/hibernate.cfg.xml")
        .buildSessionFactory()
}
