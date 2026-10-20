package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.application.usecase.budynek.PobierzBudynkiUseCase
import java.io.File

/** Eksportuje raport miesięczny do osobnych plików Excel dla każdego budynku */
class ExportWszystkichBudynkowUseCase(
    private val pobierzBudynkiUseCase: PobierzBudynkiUseCase,
    private val generujRaportUseCase: GenerujRaportUseCase
) {
    operator fun invoke(folder: File, rok: Int, miesiac: Int, eksporter: RaportExporter): Result<File> = runCatching {
        val raporty = mutableListOf<DaneRaportu>()
        val bledy = mutableListOf<String>()

        pobierzBudynkiUseCase().getOrThrow().forEach { b ->
            generujRaportUseCase(b.id, rok, miesiac)
                .onSuccess { raporty += it }
                .onFailure { if (it !is BrakPracException) bledy += "${b.ulica}: ${it.message}" }
        }

        check(bledy.isEmpty()) { "Nie wygenerowano budynków:\n" + bledy.joinToString("\n") }
        check(raporty.isNotEmpty()) { "Brak prac w wybranym okresie" }

        eksporter.eksportuj(raporty, File(folder, "Raport_${miesiac}_${rok}.${eksporter.rozszerzenie}")).getOrThrow()
    }
}