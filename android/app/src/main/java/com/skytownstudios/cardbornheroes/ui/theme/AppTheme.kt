package com.skytownstudios.cardbornheroes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CardbornPalette(
    val mintBg: Color,
    val mintBgDeep: Color,
    val surfaceMint: Color,
    val surfaceBorder: Color,
    val textPrimary: Color,
    val textMuted: Color,
    val heroTeal: Color,
    val heroPurple: Color,
    val heroGold: Color,
    val gearOrange: Color,
    val heroDark: Color,
    val heroSurface: Color,
    val heroCardBorder: Color,
    val navBarBrown: Color,
    val navBarMuted: Color,
    val buttonTextOnAccent: Color,
    val panelTop: Color,
    val panelBottom: Color,
)

/** Gilded Ivory — cream neutrals + mustard gold primary (#D4AF37 palette). */
val LightPalette = CardbornPalette(
    mintBg = Color(0xFFF5F0E6),
    mintBgDeep = Color(0xFFEDE6D8),
    surfaceMint = Color(0xFFFFFFFF),
    surfaceBorder = Color(0xFFB08D57),
    textPrimary = Color(0xFF3B2F2F),
    textMuted = Color(0xFF8B7355),
    heroTeal = Color(0xFFD4AF37),
    heroPurple = Color(0xFFB08D57),
    heroGold = Color(0xFFD4AF37),
    gearOrange = Color(0xFFB08D57),
    heroDark = Color(0xFFF5F0E6),
    heroSurface = Color(0xFFFFFFFF),
    heroCardBorder = Color(0xFFB08D57),
    navBarBrown = Color(0xFF3B2F2F),
    navBarMuted = Color(0xFFB08D57),
    buttonTextOnAccent = Color(0xFF3B2F2F),
    panelTop = Color(0xFFF8F2E8),
    panelBottom = Color(0xFFEDE6D8),
)

val DarkPalette = CardbornPalette(
    mintBg = Color(0xFF1A1612),
    mintBgDeep = Color(0xFF12100E),
    surfaceMint = Color(0xFF2A2420),
    surfaceBorder = Color(0xFFB08D57),
    textPrimary = Color(0xFFF5F0E6),
    textMuted = Color(0xFFA89880),
    heroTeal = Color(0xFFD4AF37),
    heroPurple = Color(0xFFB08D57),
    heroGold = Color(0xFFD4AF37),
    gearOrange = Color(0xFFB08D57),
    heroDark = Color(0xFF1A1612),
    heroSurface = Color(0xFF2A2420),
    heroCardBorder = Color(0xFFB08D57),
    navBarBrown = Color(0xFFF5F0E6),
    navBarMuted = Color(0xFFA89880),
    buttonTextOnAccent = Color(0xFF3B2F2F),
    panelTop = Color(0xFF2E2824),
    panelBottom = Color(0xFF221E1A),
)

val LocalCardbornPalette = staticCompositionLocalOf { LightPalette }

val MintBg: Color
    @Composable get() = LocalCardbornPalette.current.mintBg

val MintBgDeep: Color
    @Composable get() = LocalCardbornPalette.current.mintBgDeep

val SurfaceMint: Color
    @Composable get() = LocalCardbornPalette.current.surfaceMint

val SurfaceBorder: Color
    @Composable get() = LocalCardbornPalette.current.surfaceBorder

val TextPrimary: Color
    @Composable get() = LocalCardbornPalette.current.textPrimary

val TextMuted: Color
    @Composable get() = LocalCardbornPalette.current.textMuted

val HeroTeal: Color
    @Composable get() = LocalCardbornPalette.current.heroTeal

val HeroPurple: Color
    @Composable get() = LocalCardbornPalette.current.heroPurple

val HeroGold: Color
    @Composable get() = LocalCardbornPalette.current.heroGold

val GearOrange: Color
    @Composable get() = LocalCardbornPalette.current.gearOrange

val HeroDark: Color
    @Composable get() = LocalCardbornPalette.current.heroDark

val HeroSurface: Color
    @Composable get() = LocalCardbornPalette.current.heroSurface

val HeroCardBorder: Color
    @Composable get() = LocalCardbornPalette.current.heroCardBorder

val NavBarBrown: Color
    @Composable get() = LocalCardbornPalette.current.navBarBrown

val NavBarMuted: Color
    @Composable get() = LocalCardbornPalette.current.navBarMuted

val ButtonTextOnAccent: Color
    @Composable get() = LocalCardbornPalette.current.buttonTextOnAccent

val PanelTop: Color
    @Composable get() = LocalCardbornPalette.current.panelTop

val PanelBottom: Color
    @Composable get() = LocalCardbornPalette.current.panelBottom

@Composable
fun CardbornHeroesTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val palette = if (darkTheme) DarkPalette else LightPalette
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.heroTeal,
            secondary = palette.heroGold,
            tertiary = palette.gearOrange,
            background = palette.mintBg,
            surface = palette.surfaceMint,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            onPrimary = palette.buttonTextOnAccent,
            onSecondary = palette.buttonTextOnAccent,
            outline = palette.surfaceBorder
        )
    } else {
        lightColorScheme(
            primary = palette.heroTeal,
            secondary = palette.heroGold,
            tertiary = palette.gearOrange,
            background = palette.mintBg,
            surface = palette.surfaceMint,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary,
            onPrimary = palette.buttonTextOnAccent,
            onSecondary = palette.buttonTextOnAccent,
            outline = palette.surfaceBorder
        )
    }

    CompositionLocalProvider(LocalCardbornPalette provides palette) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
