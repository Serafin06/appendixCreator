package pl.rafapp.marko.appendixCreator.application.usecase

import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.service.NumeratorFaktur
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class NumeratorFakturTest {
    private val numerator = NumeratorFaktur()

    private fun praca(budynekId: Long, data: LocalDate, nr: String?) = Praca(
        data = data, opis = "Praca", roboczogodziny = 1,
        kosztDojazdu = 0.0, vat = 8, budynekId = budynekId, nrFaktury = nr
    )

    private val marzec = LocalDate.of(2026, 3, 10)

    @Test
    fun `first invoice in year is 1`() {
        assertEquals("1/2026", numerator.sugeruj(emptyList(), 1, marzec))
    }

    @Test
    fun `next number follows highest number in year`() {
        val prace = listOf(
            praca(1, LocalDate.of(2026, 1, 5), "1/2026"),
            praca(2, LocalDate.of(2026, 2, 5), "3/2026")
        )
        assertEquals("4/2026", numerator.sugeruj(prace, 3, marzec))
    }

    @Test
    fun `numbering restarts in new year`() {
        val prace = listOf(praca(1, LocalDate.of(2025, 12, 5), "5/2025"))
        assertEquals("1/2026", numerator.sugeruj(prace, 1, LocalDate.of(2026, 1, 2)))
    }

    @Test
    fun `same building in same month reuses invoice number`() {
        val prace = listOf(praca(1, LocalDate.of(2026, 3, 2), "2/2026"))
        assertEquals("2/2026", numerator.sugeruj(prace, 1, marzec))
    }

    @Test
    fun `same building in different month gets new number`() {
        val prace = listOf(praca(1, LocalDate.of(2026, 2, 2), "2/2026"))
        assertEquals("3/2026", numerator.sugeruj(prace, 1, marzec))
    }

    @Test
    fun `other building in same month gets next number`() {
        val prace = listOf(praca(1, LocalDate.of(2026, 3, 2), "2/2026"))
        assertEquals("3/2026", numerator.sugeruj(prace, 2, marzec))
    }

    @Test
    fun `no building selected gets next number`() {
        val prace = listOf(praca(1, LocalDate.of(2026, 3, 2), "2/2026"))
        assertEquals("3/2026", numerator.sugeruj(prace, null, marzec))
    }

    @Test
    fun `malformed and blank invoice numbers are ignored`() {
        val prace = listOf(
            praca(1, LocalDate.of(2026, 3, 1), "FV/3"),
            praca(2, LocalDate.of(2026, 3, 1), "abc"),
            praca(3, LocalDate.of(2026, 3, 1), ""),
            praca(4, LocalDate.of(2026, 3, 1), null),
            praca(5, LocalDate.of(2026, 3, 1), "2/2026")
        )
        assertEquals("3/2026", numerator.sugeruj(prace, 9, marzec))
    }
}