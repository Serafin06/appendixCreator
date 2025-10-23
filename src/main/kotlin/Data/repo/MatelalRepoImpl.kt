package pl.rafapp.appendixCreator.Data.repo

import org.hibernate.Session
import pl.rafapp.appendixCreator.Data.DataBase.HibernateConfig
import pl.rafapp.appendixCreator.Data.MaterialEntity
import pl.rafapp.appendixCreator.dataBase.repo.MaterialRepo
import pl.rafapp.appendixCreator.domena.Material

class MatelalRepoImpl : MaterialRepo {

    private inline fun <T> useSession(block: (Session) -> T): T {
        val session = HibernateConfig.sessionFactory.openSession()
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

    override fun dodaj(material: Material): Material = useSession { session ->
        val entity = MaterialEntity.fromDomain(material)
        session.persist(entity)
        session.flush()
        entity.toDomain()
    }

    override fun pobierzWszystkie(): List<Material> = useSession { session ->
        session.createQuery("FROM MaterialEntity", MaterialEntity::class.java)
            .resultList
            .map { it.toDomain() }
    }

    override fun pobierzPoId(id: Long): Material? = useSession { session ->
        session.get(MaterialEntity::class.java, id)?.toDomain()
    }

    override fun usun(id: Long) = useSession { session ->
        val entity = session.get(MaterialEntity::class.java, id)
        if (entity != null) {
            session.remove(entity)
        }
    }
}