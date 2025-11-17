package pl.rafapp.marko.appendixCreator.domain.repository

import pl.rafapp.marko.appendixCreator.domain.model.Budynek

/**
 * Repository interface - kontrakt dla operacji na budynkach
 * Zgodnie z Dependency Inversion Principle (SOLID)
 */

interface BudynekRepository {
    fun dodaj(budynek: Budynek): Budynek
    fun pobierzWszystkie(): List<Budynek>
    fun pobierzPoId(id: Long): Budynek?
    fun usun(id: Long)
}