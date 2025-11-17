package pl.rafapp.marko.appendixCreator.data.entity

import jakarta.persistence.*
import pl.rafapp.marko.appendixCreator.domain.model.Praca
import java.time.LocalDate

@Entity
@Table(name = "praca")
data class PracaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val data: LocalDate = LocalDate.now(),

    @Column(nullable = false, columnDefinition = "TEXT")
    val opis: String = "",

    @Column(nullable = false)
    val roboczogodziny: Int = 0,

    @Column(name = "koszt_dojazdu", nullable = false)
    val kosztDojazdu: Double = 0.0,  // Usuń precision i scale!

    @Column(nullable = false)
    val vat: Int = 23,

    @Column(name = "budynek_id", nullable = false)
    val budynekId: Long = 0,

    @OneToMany(mappedBy = "praca", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    val materialy: MutableList<PracaMaterialEntity> = mutableListOf()
) {
    fun toDomain() = Praca(
        id = id,
        data = data,
        opis = opis,
        roboczogodziny = roboczogodziny,
        kosztDojazdu = kosztDojazdu,
        vat = vat,
        budynekId = budynekId,
        materialy = materialy.map { it.toDomain() }
    )

    companion object {
        fun fromDomain(praca: Praca): PracaEntity {
            val entity = PracaEntity(
                id = praca.id,
                data = praca.data,
                opis = praca.opis,
                roboczogodziny = praca.roboczogodziny,
                kosztDojazdu = praca.kosztDojazdu,
                vat = praca.vat,
                budynekId = praca.budynekId
            )

            entity.materialy.addAll(
                praca.materialy.map { PracaMaterialEntity.fromDomain(it, entity) }
            )

            return entity
        }
    }
}