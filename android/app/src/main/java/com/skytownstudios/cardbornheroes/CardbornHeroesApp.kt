package com.skytownstudios.cardbornheroes

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.skytownstudios.cardbornheroes.ui.screens.HomeScreen
import com.skytownstudios.cardbornheroes.ui.screens.SettingsScreen
import com.skytownstudios.cardbornheroes.billing.BillingViewModel

@Composable
fun CardbornHeroesApp() {
    val billing: BillingViewModel = viewModel()
    var tab by remember { mutableIntStateOf(0) }
    MaterialTheme {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, null) },
                        label = { Text("Play") } // styled by android-game-walkthrough theme
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.Settings, null) },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { padding ->
            when (tab) {
                0 -> HomeScreen(Modifier.padding(padding), billing)
                1 -> SettingsScreen(Modifier.padding(padding), billing)
            }
        }
    }
}
