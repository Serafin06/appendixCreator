package pl.rafapp.marko.appendixCreator.data.repository

import org.hibernate.Session
import pl.rafapp.marko.appendixCreator.config.DatabaseConfig
import pl.rafapp.marko.appendixCreator.data.entity.PracaEntity
import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository

/**
 * Implementacja repozytorium prac
 * Single Responsibility: operacje CRUD na pracach
 */
class PracaRepositoryImpl : PracaRepository {

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

    override fun dodaj(praca: Praca): Praca = useSession { session ->
        val entity = PracaEntity.fromDomain(praca)
        session.persist(entity)
        session.flush()
        entity.toDomain()
    }

    override fun pobierzWszystkie(): List<Praca> = useSession { session ->
        session.createQuery(
            "SELECT DISTINCT p FROM PracaEntity p LEFT JOIN FETCH p.materialy ORDER BY p.data DESC",
            PracaEntity::class.java
        ).resultList.map { it.toDomain() }
    }

    override fun pobierzPoId(id: Long): Praca? = useSession { session ->
        session.createQuery(
            "SELECT p FROM PracaEntity p LEFT JOIN FETCH p.materialy WHERE p.id = :id",
            PracaEntity::class.java
        ).setParameter("id", id)
            .uniqueResult()
            ?.toDomain()
    }

    override fun pobierzDlaBudynku(budynekId: Long): List<Praca> = useSession { session ->
        session.createQuery(
            """
            SELECT DISTINCT p FROM PracaEntity p 
            LEFT JOIN FETCH p.materialy 
            WHERE p.budynekId = :budynekId 
            ORDER BY p.data DESC
            """.trimIndent(),
            PracaEntity::class.java
        ).setParameter("budynekId", budynekId)
            .resultList
            .map { it.toDomain() }
    }

    override fun usun(id: Long) = useSession { session ->
        val entity = session.get(PracaEntity::class.java, id)
            ?: throw IllegalArgumentException("Praca o ID $id nie istnieje")
        session.remove(entity)
    }
}