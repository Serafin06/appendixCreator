package pl.marko.bazaDanych

import org.hibernate.Session


object DaoTabele {

    fun dodajBudynek(budynek: Budynek) {
        val session: Session = HibernateUtil.sessionFactory.openSession()
        session.beginTransaction()
        session.persist(budynek)
        session.transaction.commit()
        session.close()
    }

    fun dodajMaterial(material: Material) {
        val session: Session = HibernateUtil.sessionFactory.openSession()
        session.beginTransaction()
        session.persist(material)
        session.transaction.commit()
        session.close()
    }

    fun dodajPraca(praca: Praca) {
        val session: Session = HibernateUtil.sessionFactory.openSession()
        session.beginTransaction()
        session.persist(praca)
        session.transaction.commit()
        session.close()
    }

    fun pobierzBudynki(): List<Budynek> {
        val session: Session = HibernateUtil.sessionFactory.openSession()
        val result = session.createQuery("FROM Budynek", Budynek::class.java).resultList
        session.close()
        return result
    }

    fun pobierzMaterialy(): List<Material> {
        val session: Session = HibernateUtil.sessionFactory.openSession()
        val result = session.createQuery("FROM Material", Material::class.java).resultList
        session.close()
        return result
    }

    fun pobierzPrace(): List<Praca> {
        val session: Session = HibernateUtil.sessionFactory.openSession()
        val result = session.createQuery("FROM Praca", Praca::class.java).resultList
        session.close()
        return result
    }
}
