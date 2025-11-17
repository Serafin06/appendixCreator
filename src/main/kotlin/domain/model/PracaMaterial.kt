package pl.rafapp.marko.appendixCreator.domain.model

/**
 * Domain model - reprezentuje materiał użyty w pracy
 */
data class PracaMaterial(
    val id: Long = 0,
    val materialId: Long,
    val ilosc: Double
)