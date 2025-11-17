package pl.rafapp.marko.appendixCreator.data.repository

import org.hibernate.Session
import pl.rafapp.marko.appendixCreator.config.DatabaseConfig
import pl.rafapp.marko.appendixCreator.data.entity.BudynekEntity
import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository


/**
 * Implementacja repozytorium budynków
 * Single Responsibility: operacje CRUD na budynkach
 */
class BudynekRepositoryImpl : BudynekRepository {

    private inline fun <T> useSession(block: (Session) -> T): T {
        val session = DatabaseConfig.sessionFactory.openSession()
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

    override fun pobierzWszystkie(): List<Budynek> = useSession { session ->
        session.createQuery("FROM BudynekEntity ORDER BY adres", BudynekEntity::class.java)
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