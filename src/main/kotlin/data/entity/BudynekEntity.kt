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

    @Column(nullable = false)
    val miasto: String = "",

    @Column(nullable = false, length = 500)
    val ulica: String = ""
) {
    fun toDomain() = Budynek(
        id = id,
        miasto = miasto,
        ulica = ulica
    )

    companion object {
        fun fromDomain(budynek: Budynek) = BudynekEntity(
            id = budynek.id,
            miasto = budynek.miasto,
            ulica = budynek.ulica
        )
    }
}