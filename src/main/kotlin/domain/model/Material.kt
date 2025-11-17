package pl.rafapp.marko.appendixCreator.domain.model

/**
 * Domain model - reprezentuje materiał budowlany
 */
data class Material(
    val id: Long = 0,
    val nazwa: String,
    val jednostka: String,
    val cenaZaJednostke: Double
)