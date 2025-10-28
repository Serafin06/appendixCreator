package pl.rafapp.appendixCreator.domena.repo

import pl.rafapp.appendixCreator.domena.Praca


interface PracaRepo {
    fun dodaj(praca: Praca): Praca
    fun aktualizuj(praca: Praca): Praca
    fun pobierzWszystkie(): List<Praca>
    fun pobierzPoId(id: Long): Praca?
    fun pobierzDlaBudynku(budynekId: Long): List<Praca>
    fun usun(id: Long)
}