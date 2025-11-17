package pl.rafapp.marko.appendixCreator.data.entity

import jakarta.persistence.*
import pl.rafapp.marko.appendixCreator.domain.model.Material


@Entity
@Table(name = "materialy")
data class MaterialEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val nazwa: String = "",

    @Column(nullable = false, length = 50)
    val jednostka: String = "",

    @Column(name = "cena_za_jednostke", nullable = false)
    val cenaZaJednostke: Double = 0.0  // Usuń precision i scale!
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