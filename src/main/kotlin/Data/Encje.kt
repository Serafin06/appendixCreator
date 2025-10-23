package pl.rafapp.appendixCreator.Data

import jakarta.persistence.*
import pl.rafapp.appendixCreator.domena.*


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

