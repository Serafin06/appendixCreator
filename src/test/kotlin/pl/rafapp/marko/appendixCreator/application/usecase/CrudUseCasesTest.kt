package pl.rafapp.marko.appendixCreator.application.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.DodajBudynekUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.UsunBudynekUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.AktualizujMaterialUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.DodajMaterialUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.PobierzMaterialyUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.material.UsunMaterialUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.DodajPraceUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.EdytujPraceUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.PobierzPraceUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.praca.UsunPraceUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.ustawienia.PobierzUstawieniaUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.ustawienia.ZapiszUstawieniaUseCase
import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.model.PracaMaterial
import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import java.time.LocalDate

class CrudUseCasesTest {
    @Test
    fun `building creation trims fields and delegates to repository`() {
        val repository = FakeBudynekRepository()

        val result = DodajBudynekUseCase(repository)(" Katowice ", " Kwiatowa 1 ")

        assertEquals(Budynek(miasto = "Katowice", ulica = "Kwiatowa 1"), result.getOrThrow())
        assertEquals(listOf(result.getOrThrow()), repository.buildings)
    }

    @Test
    fun `building creation rejects blank fields without saving`() {
        val repository = FakeBudynekRepository()
        val useCase = DodajBudynekUseCase(repository)

        assertFalse(useCase(" ", "Kwiatowa 1").isSuccess)
        assertFalse(useCase("Katowice", "\t").isSuccess)
        assertTrue(repository.buildings.isEmpty())
    }

    @Test
    fun `building listing and deletion delegate and preserve repository failures`() {
        val building = Budynek(id = 4, miasto = "Katowice", ulica = "Kwiatowa 1")
        val repository = FakeBudynekRepository(mutableListOf(building))

        assertEquals(listOf(building), PobierzBudynkiUseCase(repository)().getOrThrow())
        UsunBudynekUseCase(repository)(4).getOrThrow()
        assertEquals(4, repository.deletedId)
        assertTrue(repository.buildings.isEmpty())

        val failure = IllegalStateException("database unavailable")
        repository.failure = failure
        assertSame(failure, PobierzBudynkiUseCase(repository)().exceptionOrNull())
        assertSame(failure, UsunBudynekUseCase(repository)(4).exceptionOrNull())
    }

    @Test
    fun `material creation trims fields and validates required values`() {
        val repository = FakeMaterialRepository()
        val useCase = DodajMaterialUseCase(repository)

        val material = useCase(" Cement ", " kg ", 12.5).getOrThrow()

        assertEquals(Material(nazwa = "Cement", jednostka = "kg", cenaZaJednostke = 12.5), material)
        assertEquals(listOf(material), repository.materials)
        assertFalse(useCase("", "kg", 12.5).isSuccess)
        assertFalse(useCase("Cement", " ", 12.5).isSuccess)
        assertFalse(useCase("Cement", "kg", 0.0).isSuccess)
        assertFalse(useCase("Cement", "kg", Double.NaN).isSuccess)
        assertEquals(1, repository.materials.size)
    }

    @Test
    fun `material update listing deletion and repository failures are propagated`() {
        val existing = Material(id = 9, nazwa = "Piasek", jednostka = "t", cenaZaJednostke = 100.0)
        val repository = FakeMaterialRepository(mutableListOf(existing))

        val updated = AktualizujMaterialUseCase(repository)(9, " Żwir ", " m3 ", 20.0).getOrThrow()
        assertEquals(Material(9, "Żwir", "m3", 20.0), updated)
        assertEquals(updated, repository.updatedMaterial)
        assertEquals(listOf(existing), PobierzMaterialyUseCase(repository)().getOrThrow())
        assertFalse(AktualizujMaterialUseCase(repository)(9, " ", "kg", 1.0).isSuccess)
        UsunMaterialUseCase(repository)(9).getOrThrow()
        assertEquals(9, repository.deletedId)
        assertTrue(repository.materials.isEmpty())

        val failure = IllegalStateException("repository failure")
        repository.failure = failure
        assertSame(failure, PobierzMaterialyUseCase(repository)().exceptionOrNull())
        assertSame(failure, UsunMaterialUseCase(repository)(9).exceptionOrNull())
        assertSame(failure, AktualizujMaterialUseCase(repository)(9, "Żwir", "kg", 1.0).exceptionOrNull())
    }

    @Test
    fun `work creation accepts allowed boundaries and rejects invalid input`() {
        val repository = FakePracaRepository()
        val useCase = DodajPraceUseCase(repository)
        val minimum = Praca(
            data = LocalDate.of(2026, 1, 1),
            opis = "Prace",
            roboczogodziny = 1,
            kosztDojazdu = 0.0,
            vat = 8,
            budynekId = 1
        )
        val maximum = minimum.copy(roboczogodziny = 30, vat = 23)

        assertEquals(minimum, useCase(minimum).getOrThrow())
        assertEquals(maximum, useCase(maximum).getOrThrow())
        assertFalse(useCase(minimum.copy(opis = " ")).isSuccess)
        assertFalse(useCase(minimum.copy(roboczogodziny = 0)).isSuccess)
        assertFalse(useCase(minimum.copy(roboczogodziny = 31)).isSuccess)
        assertFalse(useCase(minimum.copy(vat = 5)).isSuccess)
        assertFalse(useCase(minimum.copy(kosztDojazdu = -0.01)).isSuccess)
        assertEquals(listOf(minimum, maximum), repository.works)
    }

    @Test
    fun `work creation propagates repository failure`() {
        val repository = FakePracaRepository()
        val failure = IllegalStateException("repository failure")
        repository.failure = failure

        assertSame(failure, DodajPraceUseCase(repository)(poprawnaPraca()).exceptionOrNull())
        assertTrue(repository.works.isEmpty())
    }

    @Test
    fun `work creation rejects NaN travel cost`() { // ZAŁOŻENIE: walidacja NaN
        val repository = FakePracaRepository()

        assertFalse(DodajPraceUseCase(repository)(poprawnaPraca().copy(kosztDojazdu = Double.NaN)).isSuccess)
        assertTrue(repository.works.isEmpty())
    }

    @Test
    fun `work creation returns entity produced by repository`() {
        val repository = FakePracaRepository().apply { assignIds = true }

        val saved = DodajPraceUseCase(repository)(poprawnaPraca()).getOrThrow()

        assertEquals(1L, saved.id)
        assertEquals(listOf(saved), repository.works)
    }

    @Test
    fun `work creation keeps materials and rejects non-positive quantity`() { // ZAŁOŻENIE: walidacja ilości
        val repository = FakePracaRepository()
        val useCase = DodajPraceUseCase(repository)
        val zMaterialem = poprawnaPraca().copy(materialy = listOf(PracaMaterial(materialId = 5, ilosc = 2.5)))

        assertEquals(zMaterialem, useCase(zMaterialem).getOrThrow())

        for (ilosc in listOf(0.0, -1.0, Double.NaN)) {
            val zla = poprawnaPraca().copy(materialy = listOf(PracaMaterial(materialId = 5, ilosc = ilosc)))
            assertFalse(useCase(zla).isSuccess)
        }
        assertEquals(listOf(zMaterialem), repository.works)
    }

    @Test
    fun `work creation trims description`() { // ZAŁOŻENIE: przycinanie jak w budynkach
        val repository = FakePracaRepository()

        val saved = DodajPraceUseCase(repository)(poprawnaPraca().copy(opis = "  Prace  ")).getOrThrow()

        assertEquals("Prace", saved.opis)
    }

    @Test
    fun `work creation stores invoice number unchanged`() {
        val repository = FakePracaRepository()

        val saved = DodajPraceUseCase(repository)(poprawnaPraca().copy(nrFaktury = "7/2026")).getOrThrow()

        assertEquals("7/2026", saved.nrFaktury)
    }

    @Test
    fun `work editing listing filtering deletion and failures are propagated`() {
        val work = Praca(
            id = 3,
            data = LocalDate.of(2026, 2, 10),
            opis = "Naprawa",
            roboczogodziny = 2,
            kosztDojazdu = 10.0,
            vat = 23,
            budynekId = 7
        )
        val otherWork = work.copy(id = 4, budynekId = 8)
        val repository = FakePracaRepository(mutableListOf(work, otherWork))
        val updated = work.copy(opis = "Poprawiona naprawa")

        assertEquals(updated, EdytujPraceUseCase(repository)(updated).getOrThrow())
        assertEquals(updated, repository.updatedWork)
        assertEquals(listOf(work, otherWork), PobierzPraceUseCase(repository)().getOrThrow())
        assertEquals(listOf(work), PobierzPraceUseCase(repository).dlaBudynku(7).getOrThrow())
        UsunPraceUseCase(repository)(3).getOrThrow()
        assertEquals(3, repository.deletedId)
        assertEquals(listOf(otherWork), repository.works)

        val failure = IllegalStateException("repository failure")
        repository.failure = failure
        assertSame(failure, EdytujPraceUseCase(repository)(updated).exceptionOrNull())
        assertSame(failure, PobierzPraceUseCase(repository)().exceptionOrNull())
        assertSame(failure, PobierzPraceUseCase(repository).dlaBudynku(7).exceptionOrNull())
        assertSame(failure, UsunPraceUseCase(repository)(3).exceptionOrNull())
    }

    @Test
    fun `settings can be read and saved with input validation`() {
        val repository = FakeUstawieniaRepository()

        assertEquals(Ustawienia(), PobierzUstawieniaUseCase(repository)().getOrThrow())
        val saved = ZapiszUstawieniaUseCase(repository)(75.0, 0.0).getOrThrow()
        assertEquals(Ustawienia(75.0, 0.0), saved)
        assertEquals(saved, repository.settings)
        assertFalse(ZapiszUstawieniaUseCase(repository)(0.0, 10.0).isSuccess)
        assertFalse(ZapiszUstawieniaUseCase(repository)(-1.0, 10.0).isSuccess)
        assertFalse(ZapiszUstawieniaUseCase(repository)(50.0, -0.01).isSuccess)

        val failure = IllegalStateException("repository failure")
        repository.failure = failure
        assertSame(failure, PobierzUstawieniaUseCase(repository)().exceptionOrNull())
        assertSame(failure, ZapiszUstawieniaUseCase(repository)(50.0, 10.0).exceptionOrNull())
    }

    private fun poprawnaPraca() = Praca(
        data = LocalDate.of(2026, 1, 1),
        opis = "Prace",
        roboczogodziny = 1,
        kosztDojazdu = 0.0,
        vat = 8,
        budynekId = 1
    )
}
