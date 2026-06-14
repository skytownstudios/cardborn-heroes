package com.skytownstudios.cardbornheroes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.AssetIcon
import com.skytownstudios.cardbornheroes.ui.components.ProfileSheet
import com.skytownstudios.cardbornheroes.ui.components.TopBar
import com.skytownstudios.cardbornheroes.ui.components.VaultModal
import com.skytownstudios.cardbornheroes.ui.screens.BattleOverlay
import com.skytownstudios.cardbornheroes.ui.screens.BattleScreen
import com.skytownstudios.cardbornheroes.ui.screens.CardsScreen
import com.skytownstudios.cardbornheroes.ui.screens.ForgeScreen
import com.skytownstudios.cardbornheroes.ui.screens.HandScreen
import com.skytownstudios.cardbornheroes.ui.screens.ShopScreen
import com.skytownstudios.cardbornheroes.ui.theme.CardbornHeroesTheme
import com.skytownstudios.cardbornheroes.ui.theme.MintBg
import com.skytownstudios.cardbornheroes.ui.theme.NavBarBrown
import com.skytownstudios.cardbornheroes.ui.theme.NavBarMuted

private enum class MainTab(
    val label: String,
    val iconAsset: String
) {
    Battle("Battle", "ui/tab_battle.png"),
    Hand("Hand", "ui/tab_hand.png"),
    Cards("Cards", "ui/tab_cards.png"),
    Forge("Forge", "ui/tab_forge.png"),
    Shop("Shop", "ui/tab_shop.png")
}

@Composable
fun CardbornApp() {
    val vm: GameViewModel = viewModel()

    CardbornHeroesTheme {
        var selectedTab by remember { mutableIntStateOf(0) }
        val tabs = MainTab.entries

        Box(Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MintBg)
                    .statusBarsPadding(),
                containerColor = MintBg,
                topBar = { TopBar(vm) },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = {
                                    AssetIcon(
                                        assetPath = tab.iconAsset,
                                        contentDescription = tab.label,
                                        size = 24.dp
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        color = if (selectedTab == index) NavBarBrown else NavBarMuted
                                    )
                                }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (tabs[selectedTab]) {
                        MainTab.Battle -> BattleScreen(vm)
                        MainTab.Hand -> HandScreen(vm)
                        MainTab.Cards -> CardsScreen(vm)
                        MainTab.Forge -> ForgeScreen(vm)
                        MainTab.Shop -> ShopScreen(vm)
                    }
                }
            }

            VaultModal(vm)
            ProfileSheet(vm)
            if (vm.battle != null) {
                BattleOverlay(vm)
            }
        }
    }
}

