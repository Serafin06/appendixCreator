package pl.rafapp.marko.appendixCreator.domain.model

/**
 * Ustawienia aplikacji przechowywane w bazie
 */

data class Ustawienia(
    val stawkaRoboczogodziny: Double = 50.0,
    val kosztDojazdu: Double = 25.0
)