package pl.marko.bazaDanych

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "budynki")
data class Budynek(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @Column(nullable = false, unique = true)
    var adres: String = ""
)



@Entity
@Table(name = "materialy")
data class Material(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var nazwa: String = "",

    var cena: Double = 0.0,

    @Column(name = "jednostka_miary")
    var jednostkaMiary: String = ""
)


@Entity
@Table(name = "praca")
data class Praca(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    var iloscGodzin: Int = 0,

    var dojazd: Boolean = true,

    var vat: Int = 8,

    var praca: String = "",

    @Temporal(TemporalType.DATE)
    @Column(name = "data_wykonania")
    var dataWykonania: Date = Date(),

    @ManyToOne
    @JoinColumn(name = "budynek_id")
    var budynek: Budynek? = null
)


@Entity
@Table(name = "praca_material")
data class PracaMaterial(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne
    @JoinColumn(name = "praca_id")
    var praca: Praca? = null,

    @ManyToOne
    @JoinColumn(name = "material_id")
    var material: Material? = null,

    var ilosc: Double = 8.0
)
