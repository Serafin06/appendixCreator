package pl.rafapp.marko.appendixCreator.application.usecase

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import pl.rafapp.marko.appendixCreator.application.usecase.raport.*
import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.model.Praca
import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia
import java.io.File
import java.nio.file.Files
import java.time.LocalDate
import kotlin.test.*

/** Atrapa eksportera - zapamiętuje, co dostała. */
private class FakeRaportExporter(override val rozszerzenie: String = "pdf") : RaportExporter {
    var otrzymane: List<DaneRaportu>? = null
    var plik: File? = null

    override fun eksportuj(raporty: List<DaneRaportu>, plik: File): Result<File> {
        otrzymane = raporty
        this.plik = plik
        return Result.success(plik)
    }

    override fun eksportujZbiorczy(dane: DaneRaportuZbiorczego, plik: File) = Result.success(plik)
}

class ExportUseCasesTest {

    private val folder = Files.createTempDirectory("raporty").toFile()

    private fun praca(budynekId: Long) = Praca(
        data = LocalDate.of(2026, 3, 1), opis = "Praca", roboczogodziny = 1,
        kosztDojazdu = 25.0, vat = 8, budynekId = budynekId
    )

    private fun raport(budynek: Budynek): DaneRaportu =
        GenerujRaportUseCase(
            FakeBudynekRepository(mutableListOf(budynek)),
            FakePracaRepository(mutableListOf(praca(budynek.id))),
            FakeMaterialRepository(),
            FakeUstawieniaRepository(Ustawienia(stawkaRoboczogodziny = 50.0))
        )(budynek.id, 2026, 3).getOrThrow()

    private fun exportAll(budynki: List<Budynek>, prace: FakePracaRepository, exporter: RaportExporter): Result<File> {
        val repo = FakeBudynekRepository(budynki.toMutableList())
        val generator = GenerujRaportUseCase(repo, prace, FakeMaterialRepository(),
            FakeUstawieniaRepository(Ustawienia(stawkaRoboczogodziny = 50.0)))
        return ExportWszystkichBudynkowUseCase(PobierzBudynkiUseCase(repo), generator)(folder, 2026, 3, exporter)
    }

    @Test
    fun `export all passes only buildings with work to exporter and names file by exporter extension`() {
        val b1 = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val b2 = Budynek(id = 2, miasto = "Gliwice", ulica = "Lipowa 2") // bez prac
        val exporter = FakeRaportExporter("pdf")

        val result = exportAll(listOf(b1, b2), FakePracaRepository(mutableListOf(praca(1))), exporter)

        assertEquals("Raport_3_2026.pdf", result.getOrThrow().name)
        assertEquals(listOf(b1), exporter.otrzymane!!.map { it.budynek })
    }

    @Test
    fun `export all fails with building name when a real error occurs`() {
        val b1 = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")
        val prace = FakePracaRepository().apply { failure = IllegalStateException("db error") }
        val exporter = FakeRaportExporter()

        val error = exportAll(listOf(b1), prace, exporter).exceptionOrNull()

        assertTrue(error!!.message!!.contains("Kwiatowa 1"))
        assertNull(exporter.otrzymane) // nic nie wyeksportowano
    }

    @Test
    fun `export all fails when there is no work in period`() {
        val b1 = Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")

        val error = exportAll(listOf(b1), FakePracaRepository(), FakeRaportExporter()).exceptionOrNull()

        assertIs<IllegalStateException>(error)
    }

    @Test
    fun `pdf exporter creates valid pdf file for several buildings`() {
        val raporty = listOf(
            raport(Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")),
            raport(Budynek(id = 2, miasto = "Gliwice", ulica = "Żwirki i Wigury 12"))
        )
        val plik = File(folder, "test.pdf")

        ExportToPdfUseCase().eksportuj(raporty, plik).getOrThrow()

        assertTrue(plik.readBytes().decodeToString(0, 5).startsWith("%PDF"))
    }

    @Test
    fun `excel exporter handles duplicate and illegal sheet names`() {
        val raporty = listOf(
            raport(Budynek(id = 1, miasto = "Katowice", ulica = "Kwiatowa 1")),
            raport(Budynek(id = 2, miasto = "Gliwice", ulica = "Kwiatowa 1")),   // duplikat
            raport(Budynek(id = 3, miasto = "Tychy", ulica = "Polna 3/5"))        // znak '/'
        )
        val plik = File(folder, "test.xlsx")

        ExportToExcelUseCase().eksportuj(raporty, plik).getOrThrow()

        XSSFWorkbook(plik).use { wb ->
            assertEquals(3, wb.numberOfSheets)
            assertEquals(3, (0 until 3).map { wb.getSheetName(it).lowercase() }.toSet().size)
        }
    }

    @AfterTest
    fun cleanup() { folder.deleteRecursively() }
}

