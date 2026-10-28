package pl.rafapp.marko.appendixCreator.application.usecase

import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import pl.rafapp.marko.appendixCreator.domain.repository.BudynekRepository
import pl.rafapp.marko.appendixCreator.domain.repository.MaterialRepository
import pl.rafapp.marko.appendixCreator.domain.repository.PracaRepository
import pl.rafapp.marko.appendixCreator.domain.repository.UstawieniaRepository
import java.time.LocalDate

class FakeBudynekRepository(
    val buildings: MutableList<Budynek> = mutableListOf()
) : BudynekRepository {
    var failure: Exception? = null
    var deletedId: Long? = null

    override fun dodaj(budynek: Budynek): Budynek {
        throwIfFailed()
        buildings += budynek
        return budynek
    }

    override fun pobierzWszystkie(): List<Budynek> {
        throwIfFailed()
        return buildings.toList()
    }

    override fun pobierzPoId(id: Long): Budynek? {
        throwIfFailed()
        return buildings.firstOrNull { it.id == id }
    }

    override fun usun(id: Long) {
        throwIfFailed()
        deletedId = id
        buildings.removeAll { it.id == id }
    }

    private fun throwIfFailed() {
        failure?.let { throw it }
    }
}

class FakeMaterialRepository(
    val materials: MutableList<Material> = mutableListOf()
) : MaterialRepository {
    var failure: Exception? = null
    var updatedMaterial: Material? = null
    var deletedId: Long? = null

    override fun dodaj(material: Material): Material {
        throwIfFailed()
        materials += material
        return material
    }

    override fun aktualizuj(material: Material): Material {
        throwIfFailed()
        updatedMaterial = material
        return material
    }

    override fun pobierzWszystkie(): List<Material> {
        throwIfFailed()
        return materials.toList()
    }

    override fun pobierzPoId(id: Long): Material? {
        throwIfFailed()
        return materials.firstOrNull { it.id == id }
    }

    override fun usun(id: Long) {
        throwIfFailed()
        deletedId = id
        materials.removeAll { it.id == id }
    }

    private fun throwIfFailed() {
        failure?.let { throw it }
    }
}

class FakePracaRepository(
    val works: MutableList<Praca> = mutableListOf()
) : PracaRepository {
    var failure: Exception? = null
    var updatedWork: Praca? = null
    var deletedId: Long? = null
    var assignIds = false
    private var nextId = 1L

    override fun dodaj(praca: Praca): Praca {
        throwIfFailed()
        val saved = if (assignIds) praca.copy(id = nextId++) else praca
        works += saved
        return saved
    }

    override fun pobierzWszystkie(): List<Praca> {
        throwIfFailed()
        return works.toList()
    }

    override fun pobierzPoId(id: Long): Praca? {
        throwIfFailed()
        return works.firstOrNull { it.id == id }
    }

    override fun pobierzDlaBudynku(budynekId: Long): List<Praca> {
        throwIfFailed()
        return works.filter { it.budynekId == budynekId }
    }

    override fun pobierzZOkresu(od: LocalDate, koniec: LocalDate): List<Praca> =
        pobierzWszystkie().filter { !it.data.isBefore(od) && it.data.isBefore(koniec) }

    override fun aktualizuj(praca: Praca): Praca {
        throwIfFailed()
        updatedWork = praca
        return praca
    }

    override fun usun(id: Long) {
        throwIfFailed()
        deletedId = id
        works.removeAll { it.id == id }
    }

    private fun throwIfFailed() {
        failure?.let { throw it }
    }
}

class FakeUstawieniaRepository(
    var settings: Ustawienia = Ustawienia()
) : UstawieniaRepository {
    var failure: Exception? = null

    override fun pobierz(): Ustawienia {
        throwIfFailed()
        return settings
    }

    override fun zapisz(ustawienia: Ustawienia): Ustawienia {
        throwIfFailed()
        settings = ustawienia
        return ustawienia
    }

    private fun throwIfFailed() {
        failure?.let { throw it }
    }
}
