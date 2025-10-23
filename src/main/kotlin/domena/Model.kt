package pl.rafapp.appendixCreator.domena

import java.time.LocalDate

data class Budynek(
    val id: Long = 0,
    val adres: String
)

data class Material(
    val id: Long = 0,
    val nazwa: String,
    val jednostka: String,
    val cenaZaJednostke: Double
)


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


data class PracaMaterial(
    val id: Long = 0,
    val materialId: Long,
    val ilosc: Double
)
