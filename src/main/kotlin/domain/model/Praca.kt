package pl.rafapp.marko.appendixCreator.domain.model

import java.time.LocalDate

/**
 * Domain model - reprezentuje pracę wykonaną na budynku
 */
data class Praca(
    val id: Long = 0,
    val data: LocalDate,
    val opis: String,
    val roboczogodziny: Int,
    val kosztDojazdu: Double,
    val vat: Int,
    val budynekId: Long,
    val materialy: List<PracaMaterial> = emptyList()
)