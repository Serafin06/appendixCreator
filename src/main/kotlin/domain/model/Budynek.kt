package pl.rafapp.marko.appendixCreator.domain.model

/**
 * Domain model - reprezentuje budynek w systemie
 * Nie zależy od żadnego frameworka
 */
data class Budynek(
    val id: Long = 0,
    val miasto: String,
    val ulica: String
){
    val pelnyAdres: String
        get() = "$ulica, $miasto"
}