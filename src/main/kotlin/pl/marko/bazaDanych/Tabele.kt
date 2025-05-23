package pl.marko.bazaDanych

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "budynki")
open class Budynek(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val adres: String,

    @OneToMany(mappedBy = "budynek", cascade = [CascadeType.ALL], orphanRemoval = true)
    val prace: MutableList<Praca> = mutableListOf()
)

@Entity
@Table(name = "materialy")
open class Material(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val nazwa: String,
    val jednostka: String,
    val cenaZaJednostke: Double
)

@Entity
@Table(name = "praca")
open class Praca(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val data: LocalDate,
    val opis: String,
    val roboczogodziny: Int,
    val kosztDojazdu: Double,
    val vat: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "budynek_id")
    val budynek: Budynek,

    @OneToMany(mappedBy = "praca", cascade = [CascadeType.ALL], orphanRemoval = true)
    val materialy: MutableList<PracaMaterial> = mutableListOf()
)

@Entity
@Table(name = "praca_material")
open class PracaMaterial(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val ilosc: Double,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "praca_id")
    val praca: Praca,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    val material: Material
)
