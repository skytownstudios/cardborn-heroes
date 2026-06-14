package com.skytownstudios.cardbornheroes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Gilded Ivory — cream neutrals + mustard gold primary (#D4AF37 palette). */
val MintBg = Color(0xFFF5F0E6)
val MintBgDeep = Color(0xFFEDE6D8)
val SurfaceMint = Color(0xFFFFFFFF)
val SurfaceBorder = Color(0xFFB08D57)

val TextPrimary = Color(0xFF3B2F2F)
val TextMuted = Color(0xFF8B7355)

val HeroTeal = Color(0xFFD4AF37)
val HeroPurple = Color(0xFFB08D57)
val HeroGold = Color(0xFFD4AF37)
val GearOrange = Color(0xFFB08D57)

val HeroDark = MintBg
val HeroSurface = SurfaceMint
val HeroCardBorder = Color(0xFFB08D57)
val NavBarBrown = Color(0xFF3B2F2F)
val NavBarMuted = Color(0xFFB08D57)
val ButtonTextOnAccent = Color(0xFF3B2F2F)

val CardbornColors = lightColorScheme(
    primary = HeroTeal,
    secondary = HeroGold,
    tertiary = GearOrange,
    background = MintBg,
    surface = SurfaceMint,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = ButtonTextOnAccent,
    onSecondary = ButtonTextOnAccent,
    outline = SurfaceBorder
)

@Composable
fun CardbornHeroesTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CardbornColors,
        content = content
    )
}
