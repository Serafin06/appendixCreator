package pl.rafapp.appendixCreator.Data

import jakarta.persistence.*
import pl.rafapp.appendixCreator.domena.*
import java.time.LocalDate


@Entity
@Table(name = "budynki")
data class BudynekEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val adres: String = ""
) {
    fun toDomain() = Budynek(id = id, adres = adres)

    companion object {
        fun fromDomain(budynek: Budynek) = BudynekEntity(
            id = budynek.id,
            adres = budynek.adres
        )
    }
}

@Entity
@Table(name = "materialy")
data class MaterialEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val nazwa: String = "",

    @Column(nullable = false)
    val jednostka: String = "",

    @Column(name = "cena_za_jednostke", nullable = false)
    val cenaZaJednostke: Double = 0.0
) {
    fun toDomain() = Material(
        id = id,
        nazwa = nazwa,
        jednostka = jednostka,
        cenaZaJednostke = cenaZaJednostke
    )

    companion object {
        fun fromDomain(material: Material) = MaterialEntity(
            id = material.id,
            nazwa = material.nazwa,
            jednostka = material.jednostka,
            cenaZaJednostke = material.cenaZaJednostke
        )
    }
}

@Entity
@Table(name = "praca")
data class PracaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val data: LocalDate = LocalDate.now(),

    @Column(columnDefinition = "TEXT")
    val opis: String = "",

    @Column(nullable = false)
    val roboczogodziny: Int = 0,

    @Column(name = "koszt_dojazdu", nullable = false)
    val kosztDojazdu: Double = 0.0,

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

            // Dodaj materiały z referencją zwrotną
            entity.materialy.addAll(
                praca.materialy.map { PracaMaterialEntity.fromDomain(it, entity) }
            )

            return entity
        }
    }
}

@Entity
@Table(name = "praca_material")
data class PracaMaterialEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val ilosc: Double = 0.0,

    @Column(name = "material_id", nullable = false)
    val materialId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "praca_id", nullable = false)
    val praca: PracaEntity? = null
) {
    fun toDomain() = PracaMaterial(
        id = id,
        materialId = materialId,
        ilosc = ilosc
    )

    companion object {
        fun fromDomain(pracaMaterial: PracaMaterial, pracaEntity: PracaEntity) = PracaMaterialEntity(
            id = pracaMaterial.id,
            ilosc = pracaMaterial.ilosc,
            materialId = pracaMaterial.materialId,
            praca = pracaEntity
        )
    }
}
