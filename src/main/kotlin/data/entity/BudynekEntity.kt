package pl.rafapp.marko.appendixCreator.data.entity

import jakarta.persistence.*
import pl.rafapp.marko.appendixCreator.domain.model.Budynek

/**
 * JPA Entity - mapowanie na tabelę budynki
 * Zawiera konwertery do/z domain model
 */
@Entity
@Table(name = "budynki")
data class BudynekEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 500)
    val adres: String = ""
) {
    fun toDomain() = Budynek(
        id = id,
        adres = adres
    )

    companion object {
        fun fromDomain(budynek: Budynek) = BudynekEntity(
            id = budynek.id,
            adres = budynek.adres
        )
    }
}