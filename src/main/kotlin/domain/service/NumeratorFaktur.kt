package pl.rafapp.marko.appendixCreator.domain.service

import pl.rafapp.marko.appendixCreator.domain.model.Praca
import java.time.LocalDate
import java.time.YearMonth

/** Sugeruje numer faktury: ten sam dla budynku w danym miesiącu, w przeciwnym razie kolejny w roku. */
class NumeratorFaktur {
    fun sugeruj(prace: List<Praca>, budynekId: Long?, data: LocalDate): String {
        val okres = YearMonth.from(data)

        if (budynekId != null) {
            prace.firstOrNull {
                it.budynekId == budynekId &&
                        YearMonth.from(it.data) == okres &&
                        !it.nrFaktury.isNullOrBlank()
            }?.nrFaktury?.let { return it }
        }

        val rok = data.year
        val maxNr = prace
            .mapNotNull { it.nrFaktury?.split("/") }
            .filter { it.getOrNull(1)?.trim() == rok.toString() }
            .mapNotNull { it[0].trim().toIntOrNull() }
            .maxOrNull() ?: 0
        return "${maxNr + 1}/$rok"
    }
}