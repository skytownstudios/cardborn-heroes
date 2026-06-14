package com.skytownstudios.cardbornheroes



import androidx.compose.foundation.layout.Box

import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.statusBarsPadding

import androidx.compose.material3.Scaffold

import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue

import androidx.compose.runtime.mutableIntStateOf

import androidx.compose.runtime.remember

import androidx.compose.runtime.setValue

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.lifecycle.viewmodel.compose.viewModel

import com.skytownstudios.cardbornheroes.ui.GameViewModel

import com.skytownstudios.cardbornheroes.ui.components.GameTab

import com.skytownstudios.cardbornheroes.ui.components.GameNavBar
import com.skytownstudios.cardbornheroes.ui.components.GameTabBackground

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



private enum class MainTab(

    val label: String,

    val iconAsset: String,

    val gameTab: GameTab

) {

    Battle("Battle", "ui/tab_battle.png", GameTab.Battle),

    Hand("Hand", "ui/tab_hand.png", GameTab.Hand),

    Cards("Cards", "ui/tab_cards.png", GameTab.Cards),

    Forge("Forge", "ui/tab_forge.png", GameTab.Forge),

    Shop("Shop", "ui/tab_shop.png", GameTab.Shop)

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

                    .statusBarsPadding(),

                containerColor = Color.Transparent,

                topBar = { TopBar(vm) },

                bottomBar = {

                    GameNavBar(

                        tabs = tabs.map { it.label to it.iconAsset },

                        selectedIndex = selectedTab,

                        onSelect = { selectedTab = it }

                    )

                }

            ) { innerPadding ->

                GameTabBackground(tab = tabs[selectedTab].gameTab, modifier = Modifier.padding(innerPadding)) {

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


