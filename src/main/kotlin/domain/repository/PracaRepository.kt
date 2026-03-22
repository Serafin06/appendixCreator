package pl.rafapp.marko.appendixCreator.domain.repository

import pl.rafapp.marko.appendixCreator.domain.model.Praca

interface PracaRepository {
    fun dodaj(praca: Praca): Praca
    fun pobierzWszystkie(): List<Praca>
    fun pobierzPoId(id: Long): Praca?
    fun pobierzDlaBudynku(budynekId: Long): List<Praca>
    fun aktualizuj(praca: Praca): Praca
    fun usun(id: Long)
}