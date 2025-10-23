package pl.rafapp.appendixCreator.dataBase.repo

import pl.rafapp.appendixCreator.domena.Budynek

interface BudynekRepo {
    fun dodaj(budynek: Budynek): Budynek
    fun pobierzWszystkie(): List<Budynek>
    fun pobierzPoId(id: Long): Budynek?
    fun usun(id: Long)
}