package pl.rafapp.marko.appendixCreator.domain.repository

import pl.rafapp.marko.appendixCreator.domain.model.Material

interface MaterialRepository {
    fun dodaj(material: Material): Material
    fun aktualizuj(material: Material): Material
    fun pobierzWszystkie(): List<Material>
    fun pobierzPoId(id: Long): Material?
    fun usun(id: Long)
}