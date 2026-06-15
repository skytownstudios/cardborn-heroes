package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.skytownstudios.cardbornheroes.ui.theme.HeroGold
import com.skytownstudios.cardbornheroes.ui.theme.MintBg
import com.skytownstudios.cardbornheroes.ui.theme.MintBgDeep
import com.skytownstudios.cardbornheroes.ui.theme.NavBarBrown
import com.skytownstudios.cardbornheroes.ui.theme.NavBarMuted
import com.skytownstudios.cardbornheroes.ui.theme.PanelBottom
import com.skytownstudios.cardbornheroes.ui.theme.PanelTop
import com.skytownstudios.cardbornheroes.ui.theme.SurfaceBorder

enum class GameTab(val backgroundAsset: String) {
    Battle("maps/whispering_woods.png"),
    Hand("battle/arena_whispering_woods.png"),
    Cards("shop/pack_card_back.png"),
    Forge("forge/recipe_frame.png"),
    Shop("ui/pack_ascendant_epic.png")
}

@Composable
fun GameTabBackground(tab: GameTab, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize()) {
        AsyncImage(
            model = "file:///android_asset/${tab.backgroundAsset}",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.42f
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MintBg.copy(alpha = 0.82f),
                            MintBgDeep.copy(alpha = 0.88f),
                            PanelBottom.copy(alpha = 0.92f)
                        )
                    )
                )
        )
        Box(Modifier.fillMaxSize().padding(horizontal = 4.dp)) {
            content()
        }
    }
}

/** Decorative panel for lists / shop rows — game card feel instead of flat Material Card. */
@Composable
fun GamePanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.verticalGradient(
                    listOf(PanelTop, PanelBottom)
                )
            )
            .border(2.dp, SurfaceBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        content()
    }
}

@Composable
fun GameCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MintBgDeep.copy(alpha = 0.45f))
            .border(2.dp, HeroGold.copy(alpha = 0.65f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun GameNavBar(
    tabs: List<Pair<String, String>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.navigationBarsPadding(),
        containerColor = MintBgDeep,
        contentColor = NavBarBrown
    ) {
        tabs.forEachIndexed { index, (label, iconAsset) ->
            val selected = index == selectedIndex
            NavigationBarItem(
                selected = selected,
                onClick = { onSelect(index) },
                icon = {
                    AssetIcon(
                        iconAsset,
                        label,
                        size = if (selected) 28.dp else 24.dp
                    )
                },
                label = {
                    Text(
                        label,
                        fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        color = if (selected) HeroGold else NavBarMuted
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = HeroGold,
                    selectedTextColor = HeroGold,
                    unselectedIconColor = NavBarMuted,
                    unselectedTextColor = NavBarMuted,
                    indicatorColor = HeroGold.copy(alpha = 0.18f)
                )
            )
        }
    }
}

fun packArtForTier(tier: String): String = "ui/pack_ascendant_$tier.png"

@Composable
fun PackArt(tier: String, contentDescription: String?, modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = "file:///android_asset/${packArtForTier(tier)}",
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
