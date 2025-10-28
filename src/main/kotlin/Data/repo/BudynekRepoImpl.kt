package pl.rafapp.appendixCreator.Data.repo

import org.hibernate.Session
import pl.rafapp.appendixCreator.Data.BudynekEntity
import pl.rafapp.appendixCreator.Data.database.HibernateConfig
import pl.rafapp.appendixCreator.domena.Budynek
import pl.rafapp.appendixCreator.domena.repo.BudynekRepo

class BudynekRepoImpl : BudynekRepo {

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

    override fun dodaj(budynek: Budynek): Budynek = useSession { session ->
        val entity = BudynekEntity.fromDomain(budynek)
        session.persist(entity)
        session.flush()
        entity.toDomain()
    }

    override fun aktualizuj(budynek: Budynek): Budynek = useSession { session ->
        val entity = session.get(BudynekEntity::class.java, budynek.id)
            ?: throw IllegalArgumentException("Budynek o ID ${budynek.id} nie istnieje")

        val updated = entity.copy(adres = budynek.adres)
        session.merge(updated)
        session.flush()
        updated.toDomain()
    }

    override fun pobierzWszystkie(): List<Budynek> = useSession { session ->
        session.createQuery("FROM BudynekEntity", BudynekEntity::class.java)
            .resultList
            .map { it.toDomain() }
    }

    override fun pobierzPoId(id: Long): Budynek? = useSession { session ->
        session.get(BudynekEntity::class.java, id)?.toDomain()
    }

    override fun usun(id: Long) = useSession { session ->
        val entity = session.get(BudynekEntity::class.java, id)
            ?: throw IllegalArgumentException("Budynek o ID $id nie istnieje")
        session.remove(entity)
    }
}