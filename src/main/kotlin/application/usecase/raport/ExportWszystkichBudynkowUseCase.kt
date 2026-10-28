package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import java.io.File

/** Eksportuje raport miesięczny do osobnych plików Excel dla każdego budynku */
/** Eksportuje gotowe raporty wszystkich budynków do jednego pliku */
class ExportWszystkichBudynkowUseCase {
    operator fun invoke(
        folder: File,
        raporty: List<DaneRaportu>,
        rok: Int,
        miesiac: Int,
        eksporter: RaportExporter
    ): Result<File> = runCatching {
        check(raporty.isNotEmpty()) { "Brak prac w wybranym okresie" }
        val plik = File(folder, "Raport_${miesiac}_${rok}.${eksporter.rozszerzenie}")
        eksporter.eksportuj(raporty, plik).getOrThrow()
    }
}