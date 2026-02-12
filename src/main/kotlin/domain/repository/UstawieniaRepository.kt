package pl.rafapp.marko.appendixCreator.domain.repository

import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia

interface UstawieniaRepository {
    fun pobierz(): Ustawienia
    fun zapisz(ustawienia: Ustawienia): Ustawienia
}