package pl.rafapp.marko.appendixCreator.application.usecase.raport

import pl.rafapp.marko.appendixCreator.domain.model.Budynek
import pl.rafapp.marko.appendixCreator.domain.model.Material
import pl.rafapp.marko.appendixCreator.domain.model.Praca

class RaportBuilder {
    fun zbuduj(
        budynek: Budynek,
        prace: List<Praca>,
        materialy: Map<Long, Material>,
        stawka: Double,
        rok: Int,
        miesiac: Int
    ): DaneRaportu {
        val wiersze = prace.sortedBy { it.data }.map { praca ->
            val kosztRobocizny = praca.roboczogodziny * stawka
            val wierszeMaterialow = praca.materialy.mapNotNull { pm ->
                val m = materialy[pm.materialId] ?: return@mapNotNull null
                WierszMaterialuRaportu(m.nazwa, m.jednostka, pm.ilosc, m.cenaZaJednostke, pm.ilosc * m.cenaZaJednostke)
            }
            val kosztMaterialow = wierszeMaterialow.sumOf { it.kosztCalkowity }
            val kosztNetto = kosztRobocizny + praca.kosztDojazdu + kosztMaterialow
            WierszRaportu(
                data = praca.data, opis = praca.opis, roboczogodziny = praca.roboczogodziny,
                stawkaRoboczogodziny = stawka, kosztRobocizny = kosztRobocizny,
                kosztDojazdu = praca.kosztDojazdu, materialy = wierszeMaterialow,
                kosztMaterialow = kosztMaterialow, kosztNetto = kosztNetto, vat = praca.vat,
                kosztBrutto = kosztNetto * (1 + praca.vat / 100.0)
            )
        }
        val sumaNetto = wiersze.sumOf { it.kosztNetto }
        val sumaBrutto = wiersze.sumOf { it.kosztBrutto }
        return DaneRaportu(
            numerFaktury = prace.firstOrNull()?.nrFaktury ?: "",
            budynek = budynek, rok = rok, miesiac = miesiac, wiersze = wiersze,
            stawkaRoboczogodziny = stawka,
            sumaRoboczogodzin = wiersze.sumOf { it.roboczogodziny },
            sumaKosztowRobocizny = wiersze.sumOf { it.kosztRobocizny },
            sumaKosztowDojazdu = wiersze.sumOf { it.kosztDojazdu },
            sumaKosztowMaterialow = wiersze.sumOf { it.kosztMaterialow },
            sumaNetto = sumaNetto, sumaVat = sumaBrutto - sumaNetto, sumaBrutto = sumaBrutto
        )
    }
}