package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import java.io.File

/** Eksportuje raport miesięczny do osobnych plików Excel dla każdego budynku */
class ExportWszystkichBudynkowUseCase(
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val generujRaportUseCase: GenerujRaportUseCase,
    private val exportToExcelUseCase: ExportToExcelUseCase
) {
    operator fun invoke(folder: File, rok: Int, miesiac: Int): Result<List<File>> {
        return try {
            val budynki = pobierzBudynkiUseCase().getOrElse { return Result.failure(it) }
            val pliki = mutableListOf<File>()

            budynki.forEach { budynek ->
                generujRaportUseCase(budynek.id, rok, miesiac).getOrNull()?.let { dane ->
                    val nazwaPliku = "Raport_${dane.budynek.miasto}_${miesiac}_${rok}.xlsx"
                        .replace(" ", "_")
                    exportToExcelUseCase(dane, File(folder, nazwaPliku))
                        .onSuccess { pliki.add(it) }
                }
            }

            if (pliki.isEmpty())
                Result.failure(IllegalStateException("Brak prac w wybranym okresie"))
            else
                Result.success(pliki)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}