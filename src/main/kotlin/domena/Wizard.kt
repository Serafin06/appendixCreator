package pl.rafapp.appendixCreator.domena

import java.time.LocalDate



// State Wizarda tworzenia pracy - zgodnie z wzorcem State Pattern

data class PracaWizardState(
    // Krok 1
    val budynekId: Long? = null,
    val nowyAdres: String = "",
    val data: LocalDate = LocalDate.now(),

    // Krok 2
    val opis: String = "",
    val roboczogodziny: Int = 8,
    val vat: Int = 23,
    val kosztDojazdu: Double = 0.0,

    // Krok 3
    val wybraneMaterialy: List<WybranyMaterial> = emptyList(),

    // Meta
    val aktualnyKrok: Int = 1,
    val bledy: Map<String, String> = emptyMap()
) {
    fun isKrok1Valid(): Boolean {
        return (budynekId != null || nowyAdres.isNotBlank())
    }

    fun isKrok2Valid(): Boolean {
        return opis.isNotBlank() &&
                roboczogodziny in 1..30 &&
                (vat == 8 || vat == 23) &&
                kosztDojazdu >= 0
    }

    fun isKrok3Valid(): Boolean {
        return true // materiały opcjonalne
    }

    fun toPraca(ostateczneBudynekId: Long): Praca {
        return Praca(
            data = data,
            opis = opis,
            roboczogodziny = roboczogodziny,
            kosztDojazdu = kosztDojazdu,
            vat = vat,
            budynekId = ostateczneBudynekId,
            materialy = wybraneMaterialy.map {
                PracaMaterial(
                    materialId = it.materialId,
                    ilosc = it.ilosc
                )
            }
        )
    }
}

data class WybranyMaterial(
    val materialId: Long,
    val nazwa: String,
    val jednostka: String,
    val cenaZaJednostke: Double,
    val ilosc: Double
) {
    val suma: Double get() = cenaZaJednostke * ilosc
}