package pl.rafapp.marko.appendixCreator.data.entity

import jakarta.persistence.*
import pl.rafapp.marko.appendixCreator.domain.model.PracaMaterial


@Entity
@Table(name = "praca_material")
data class PracaMaterialEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val ilosc: Double = 0.0,  // Usuń precision i scale!

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