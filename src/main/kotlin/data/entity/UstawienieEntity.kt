package pl.rafapp.marko.appendixCreator.data.entity

import jakarta.persistence.*
import pl.rafapp.marko.appendixCreator.domain.model.Ustawienia

@Entity
@Table(name = "ustawienia")
data class UstawieniaEntity(
    @Id
    val id: Int = 1,

    @Column(name = "stawka_roboczogodziny", nullable = false)
    val stawkaRoboczogodziny: Double = 50.0
) {
    fun toDomain() = Ustawienia(stawkaRoboczogodziny = stawkaRoboczogodziny)

    companion object {
        fun fromDomain(ustawienia: Ustawienia) = UstawieniaEntity(
            id = 1,
            stawkaRoboczogodziny = ustawienia.stawkaRoboczogodziny
        )
    }
}