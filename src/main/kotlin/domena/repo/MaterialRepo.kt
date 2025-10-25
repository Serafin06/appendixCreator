package pl.rafapp.appendixCreator.dataBase.repo

import pl.rafapp.appendixCreator.domena.Material


interface MaterialRepo {
    fun dodaj(material: Material): Material
    fun aktualizuj(material: Material): Material
    fun pobierzWszystkie(): List<Material>
    fun pobierzPoId(id: Long): Material?
    fun usun(id: Long)
}