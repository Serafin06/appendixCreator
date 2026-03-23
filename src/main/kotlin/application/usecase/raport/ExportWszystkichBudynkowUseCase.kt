package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import java.io.File

/** Eksportuje raport miesięczny do osobnych plików Excel dla każdego budynku */
class ExportWszystkichBudynkowUseCase(
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val generujRaportUseCase: GenerujRaportUseCase,
    private val exportToExcelUseCase: ExportToExcelUseCase
) {
    operator fun invoke(folder: File, rok: Int, miesiac: Int): Result<File> {
        return try {
            val budynki = pobierzBudynkiUseCase().getOrElse { return Result.failure(it) }

            val raporty = budynki.mapNotNull { budynek ->
                generujRaportUseCase(budynek.id, rok, miesiac).getOrNull()
            }

            if (raporty.isEmpty())
                return Result.failure(IllegalStateException("Brak prac w wybranym okresie"))

            val nazwaPliku = "Raport_${miesiac}_${rok}.xlsx"
            val plik = File(folder, nazwaPliku)

            exportToExcelUseCase.invokeMulti(raporty, plik)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}