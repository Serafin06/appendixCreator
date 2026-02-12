package pl.rafapp.marko.appendixCreator.data.repository

import org.hibernate.Session
import pl.rafapp.marko.appendixCreator.config.DatabaseConfig
import pl.rafapp.marko.appendixCreator.data.entity.UstawieniaEntity
import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import pl.rafapp.marko.appendixCreator.domain.repository.UstawieniaRepository

/**
 * Repozytorium ustawień aplikacji
 * Zawsze jeden wiersz w bazie (id=1)
 */

class UstawieniaRepositoryImpl : UstawieniaRepository {

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

    override fun pobierz(): Ustawienia = useSession { session ->
        session.get(UstawieniaEntity::class.java, 1)?.toDomain()
            ?: Ustawienia() // domyślne jeśli brak
    }

    override fun zapisz(ustawienia: Ustawienia): Ustawienia = useSession { session ->
        val entity = UstawieniaEntity.fromDomain(ustawienia)
        session.merge(entity)
        session.flush()
        entity.toDomain()
    }
}