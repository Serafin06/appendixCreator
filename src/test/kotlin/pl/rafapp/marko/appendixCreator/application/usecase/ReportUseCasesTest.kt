package pl.rafapp.marko.appendixCreator.application.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertTrue
import pl.rafapp.marko.appendixCreator.application.usecase.raport.BrakPracException
import pl.rafapp.marko.appendixCreator.application.usecase.raport.GenerujRaportUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.GenerujRaportZbiorczyUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.RaportBuilder
import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.model.PracaMaterial
import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import java.time.LocalDate

class ReportUseCasesTest {
    @Test
    fun `monthly report filters sorts and calculates work materials and mixed VAT totals`() {
        val building = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val buildingRepository = FakeBudynekRepository(mutableListOf(building))
        val materialRepository = FakeMaterialRepository(
            mutableListOf(Material(id = 5, nazwa = "Farba", jednostka = "l", cenaZaJednostke = 20.0))
        )
        val workRepository = FakePracaRepository(
            mutableListOf(
                Praca(
                    id = 2,
                    data = LocalDate.of(2026, 3, 20),
                    opis = "Malowanie",
                    roboczogodziny = 2,
                    kosztDojazdu = 10.0,
                    vat = 23,
                    budynekId = 1,
                    nrFaktury = "FV/3",
                    materialy = listOf(PracaMaterial(materialId = 5, ilosc = 2.0))
                ),
                Praca(
                    id = 1,
                    data = LocalDate.of(2026, 3, 5),
                    opis = "Przygotowanie",
                    roboczogodziny = 1,
                    kosztDojazdu = 5.0,
                    vat = 8,
                    budynekId = 1,
                    nrFaktury = "FV/3"
                ),
                Praca(
                    data = LocalDate.of(2026, 4, 1),
                    opis = "Kolejny miesiąc",
                    roboczogodziny = 10,
                    kosztDojazdu = 100.0,
                    vat = 23,
                    budynekId = 1
                ),
                Praca(
                    data = LocalDate.of(2026, 3, 15),
                    opis = "Inny budynek",
                    roboczogodziny = 10,
                    kosztDojazdu = 100.0,
                    vat = 23,
                    budynekId = 2
                )
            )
        )
        val useCase = GenerujRaportUseCase(
            buildingRepository,
            workRepository,
            materialRepository,
            FakeUstawieniaRepository(Ustawienia(stawkaRoboczogodziny = 50.0)),
            RaportBuilder()
        )

        val report = useCase(1, 2026, 3).getOrThrow()

        assertEquals(listOf(LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 20)), report.wiersze.map { it.data })
        assertEquals("FV/3", report.numerFaktury)
        assertEquals(3, report.sumaRoboczogodzin)
        assertEquals(150.0, report.sumaKosztowRobocizny)
        assertEquals(15.0, report.sumaKosztowDojazdu)
        assertEquals(40.0, report.sumaKosztowMaterialow)
        assertEquals(205.0, report.sumaNetto)
        assertEquals(38.9, report.sumaVat, 0.000001)
        assertEquals(243.9, report.sumaBrutto, 0.000001)
        assertEquals(55.0, report.wiersze[0].kosztNetto)
        assertEquals(184.5, report.wiersze[1].kosztBrutto)
        assertEquals(40.0, report.wiersze[1].materialy.single().kosztCalkowity)
    }

    @Test
    fun `monthly report fails for missing building and empty selected period`() {
        val building = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val useCase = GenerujRaportUseCase(
            FakeBudynekRepository(mutableListOf(building)),
            FakePracaRepository(),
            FakeMaterialRepository(),
            FakeUstawieniaRepository(),
            RaportBuilder()
        )

        assertIs<IllegalArgumentException>(useCase(2, 2026, 3).exceptionOrNull())
        assertIs<BrakPracException>(useCase(1, 2026, 3).exceptionOrNull())
    }

    @Test
    fun `monthly report ignores materials no longer present in catalog`() {
        val building = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val work = Praca(
            data = LocalDate.of(2026, 3, 5),
            opis = "Naprawa",
            roboczogodziny = 1,
            kosztDojazdu = 0.0,
            vat = 8,
            budynekId = 1,
            materialy = listOf(PracaMaterial(materialId = 404, ilosc = 1.0))
        )
        val useCase = GenerujRaportUseCase(
            FakeBudynekRepository(mutableListOf(building)),
            FakePracaRepository(mutableListOf(work)),
            FakeMaterialRepository(),
            FakeUstawieniaRepository(Ustawienia(stawkaRoboczogodziny = 50.0)),
            RaportBuilder()
        )

        val report = useCase(1, 2026, 3).getOrThrow()

        assertTrue(report.wiersze.single().materialy.isEmpty())
        assertEquals(50.0, report.sumaNetto)
    }

    @Test
    fun `monthly report preserves repository errors`() {
        val failure = IllegalStateException("database unavailable")
        val repository = FakeBudynekRepository().apply { this.failure = failure }
        val useCase = GenerujRaportUseCase(
            repository,
            FakePracaRepository(),
            FakeMaterialRepository(),
            FakeUstawieniaRepository(),
            RaportBuilder()
        )

        assertSame(failure, useCase(1, 2026, 3).exceptionOrNull())
    }

    @Test
    fun `aggregate report totals both VAT rates and skips buildings without work`() {
        val building1 = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val building2 = Budynek(id = 2, miasto = "Gliwice", ulica = "Lipowa 2")
        val building3 = Budynek(id = 3, miasto = "Tychy", ulica = "Polna 3")
        val buildings = FakeBudynekRepository(mutableListOf(building1, building2, building3))
        val works = FakePracaRepository(
            mutableListOf(
                Praca(
                    data = LocalDate.of(2026, 3, 1),
                    opis = "Praca 8%",
                    roboczogodziny = 1,
                    kosztDojazdu = 0.0,
                    vat = 8,
                    budynekId = 1
                ),
                Praca(
                    data = LocalDate.of(2026, 3, 2),
                    opis = "Praca 23%",
                    roboczogodziny = 2,
                    kosztDojazdu = 10.0,
                    vat = 23,
                    budynekId = 1
                ),
                Praca(
                    data = LocalDate.of(2026, 3, 3),
                    opis = "Drugi budynek",
                    roboczogodziny = 1,
                    kosztDojazdu = 5.0,
                    vat = 8,
                    budynekId = 2
                )
            )
        )

        val useCase = GenerujRaportZbiorczyUseCase(
            buildings,
            works,
            FakeMaterialRepository(),
            FakeUstawieniaRepository(Ustawienia(stawkaRoboczogodziny = 100.0)),
            RaportBuilder()
        )

        val report = useCase(2026, 3).getOrThrow()

        assertEquals(listOf(building1, building2), report.wierszeBudynkow.map { it.budynek })
        assertEquals(205.0, report.lacznaNetto8)
        assertEquals(16.4, report.lacznaVat8, 0.000001)
        assertEquals(221.4, report.lacznaBrutto8, 0.000001)
        assertEquals(210.0, report.lacznaNetto23)
        assertEquals(48.3, report.lacznaVat23, 0.000001)
        assertEquals(258.3, report.lacznaBrutto23, 0.000001)
        assertEquals(415.0, report.sumaNetto)
        assertEquals(64.7, report.sumaVat, 0.000001)
        assertEquals(479.7, report.sumaBrutto, 0.000001)
    }

    @Test
    fun `aggregate report fails when no building has work or building listing fails`() {
        val building = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val buildings = FakeBudynekRepository(mutableListOf(building))
        val useCase = GenerujRaportZbiorczyUseCase(
            buildings,
            FakePracaRepository(),
            FakeMaterialRepository(),
            FakeUstawieniaRepository(),
            RaportBuilder()
        )

        assertIs<IllegalStateException>(useCase(2026, 3).exceptionOrNull())

        val failure = IllegalStateException("database unavailable")
        buildings.failure = failure
        assertSame(failure, useCase(2026, 3).exceptionOrNull())
    }

}
