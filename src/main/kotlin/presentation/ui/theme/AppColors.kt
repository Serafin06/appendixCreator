package pl.rafapp.marko.appendixCreator.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Ciemny motyw - budowlany
object AppColors {
    // Główne kolory
    val Primary = Color(0xFF2196F3)         // Niebieski
    val OnPrimary = Color(0xFFFFFFFF)
    val PrimaryContainer = Color(0xFF1976D2)
    val OnPrimaryContainer = Color(0xFFE3F2FD)

    // Drugorzędne
    val Secondary = Color(0xFFFF9800)       // Pomarańczowy (akcent)
    val OnSecondary = Color(0xFF000000)
    val SecondaryContainer = Color(0xFFF57C00)
    val OnSecondaryContainer = Color(0xFFFFF3E0)

    // Tło
    val Background = Color(0xFF1E1E1E)      // Ciemnoszary
    val OnBackground = Color(0xFFE0E0E0)
    val Surface = Color(0xFF2D2D2D)         // Jaśniejszy szary
    val OnSurface = Color(0xFFE0E0E0)

    // Warianty
    val SurfaceVariant = Color(0xFF424242)
    val OnSurfaceVariant = Color(0xFFBDBDBD)

    // Błędy
    val Error = Color(0xFFCF6679)
    val OnError = Color(0xFF000000)
    val ErrorContainer = Color(0xFFB00020)
    val OnErrorContainer = Color(0xFFFFC1C1)

    val darkScheme = darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        primaryContainer = PrimaryContainer,
        onPrimaryContainer = OnPrimaryContainer,

        secondary = Secondary,
        onSecondary = OnSecondary,
        secondaryContainer = SecondaryContainer,
        onSecondaryContainer = OnSecondaryContainer,

        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        onSurface = OnSurface,

        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = OnSurfaceVariant,

        error = Error,
        onError = OnError,
        errorContainer = ErrorContainer,
        onErrorContainer = OnErrorContainer
    )
}