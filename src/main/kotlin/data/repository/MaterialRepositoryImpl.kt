package pl.rafapp.marko.appendixCreator.data.repository

import org.hibernate.Session
import pl.rafapp.marko.appendixCreator.config.DatabaseConfig
import pl.rafapp.marko.appendixCreator.data.entity.MaterialEntity
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.repository.MaterialRepository

/**
 * Implementacja repozytorium materiałów
 * Single Responsibility: operacje CRUD na materiałach
 */
class MaterialRepositoryImpl : MaterialRepository {

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

    override fun dodaj(material: Material): Material = useSession { session ->
        val entity = MaterialEntity.fromDomain(material)
        session.persist(entity)
        session.flush()
        entity.toDomain()
    }
    override fun aktualizuj(material: Material): Material = useSession { session ->
        val entity = session.get(MaterialEntity::class.java, material.id)
            ?: throw IllegalArgumentException("Materiał o ID ${material.id} nie istnieje")

        val updated = entity.copy(
            nazwa = material.nazwa,
            jednostka = material.jednostka,
            cenaZaJednostke = material.cenaZaJednostke
        )
        session.merge(updated)
        session.flush()
        updated.toDomain()
    }

    override fun pobierzWszystkie(): List<Material> = useSession { session ->
        session.createQuery("FROM MaterialEntity ORDER BY nazwa", MaterialEntity::class.java)
            .resultList
            .map { it.toDomain() }
    }

    override fun pobierzPoId(id: Long): Material? = useSession { session ->
        session.get(MaterialEntity::class.java, id)?.toDomain()
    }

    override fun usun(id: Long) = useSession { session ->
        val entity = session.get(MaterialEntity::class.java, id)
            ?: throw IllegalArgumentException("Materiał o ID $id nie istnieje")
        session.remove(entity)
    }
}