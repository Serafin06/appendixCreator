package pl.rafapp.appendixCreator.Data.repo

import org.hibernate.Session
import pl.rafapp.appendixCreator.Data.PracaEntity
import pl.rafapp.appendixCreator.Data.database.HibernateConfig
import pl.rafapp.appendixCreator.dataBase.repo.PracaRepo
import pl.rafapp.appendixCreator.domena.Praca

class PracaRepoImpl : PracaRepo {

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

    override fun dodaj(praca: Praca): Praca = useSession { session ->
        val entity = PracaEntity.fromDomain(praca)
        session.persist(entity)
        session.flush()
        entity.toDomain()
    }

    override fun aktualizuj(praca: Praca): Praca = useSession { session ->
        // Pobierz istniejącą pracę
        val existingEntity = session.get(PracaEntity::class.java, praca.id)
            ?: throw IllegalArgumentException("Praca o ID ${praca.id} nie istnieje")

        // Usuń stare materiały
        existingEntity.materialy.clear()
        session.flush()

        // Stwórz nową encję z nowymi danymi
        val updatedEntity = PracaEntity.fromDomain(praca)

        // Merge zmian
        val merged = session.merge(updatedEntity)
        session.flush()
        merged.toDomain()
    }

    override fun pobierzWszystkie(): List<Praca> = useSession { session ->
        session.createQuery(
            "SELECT DISTINCT p FROM PracaEntity p LEFT JOIN FETCH p.materialy",
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
            "SELECT DISTINCT p FROM PracaEntity p LEFT JOIN FETCH p.materialy WHERE p.budynekId = :budynekId",
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