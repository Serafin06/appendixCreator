package pl.rafapp.appendixCreator.dataBase

import org.hibernate.Session

object DatabaseDao {

    private inline fun <T> useSession(block: (Session) -> T): T {
        val session = HibernateUtil.sessionFactory.openSession()
        return try {
            session.beginTransaction()
            val result = block(session)
            session.transaction.commit()
            result
        } catch (e: Exception) {
            session.transaction?.rollback()
            throw e
        } finally {
            session.close()
        }
    }

    fun dodajBudynek(budynek: Budynek) = useSession { session ->
        session.persist(budynek)
    }

    fun dodajMaterial(material: Material) = useSession { session ->
        session.persist(material)
    }

    fun dodajPrace(praca: Praca) = useSession { session ->
        session.persist(praca)
    }

    fun pobierzBudynki(): List<Budynek> = useSession { session ->
        session.createQuery("FROM Budynek", Budynek::class.java).resultList
    }

    fun pobierzMaterialy(): List<Material> = useSession { session ->
        session.createQuery("FROM Material", Material::class.java).resultList
    }

    fun pobierzPrace(): List<Praca> = useSession { session ->
        session.createQuery(
            "FROM Praca p JOIN FETCH p.budynek",
            Praca::class.java
        ).resultList
    }
}