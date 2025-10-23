package pl.rafapp.appendixCreator.dataBase

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "budynki")
data class Budynek(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val adres: String = "",

    @OneToMany(mappedBy = "budynek", cascade = [CascadeType.ALL], orphanRemoval = true)
    val prace: MutableList<Praca> = mutableListOf()
)

@Entity
@Table(name = "materialy")
data class Material(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val nazwa: String = "",

    @Column(nullable = false)
    val jednostka: String = "",

    @Column(name = "cena_za_jednostke", nullable = false)
    val cenaZaJednostke: Double = 0.0
)

@Entity
@Table(name = "praca")
data class Praca(
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budynek_id", nullable = false)
    val budynek: Budynek? = null,

    @OneToMany(mappedBy = "praca", cascade = [CascadeType.ALL], orphanRemoval = true)
    val materialy: MutableList<PracaMaterial> = mutableListOf()
)

@Entity
@Table(name = "praca_material")
data class PracaMaterial(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val ilosc: Double = 0.0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "praca_id", nullable = false)
    val praca: Praca? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    val material: Material? = null
)