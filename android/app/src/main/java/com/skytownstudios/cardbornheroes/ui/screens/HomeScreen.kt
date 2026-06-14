package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skytownstudios.cardbornheroes.billing.BillingViewModel

@Composable
fun HomeScreen(modifier: Modifier = Modifier, billing: BillingViewModel) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Game goes here", style = MaterialTheme.typography.headlineMedium)
            Text("Apply android-game-walkthrough or other game skill", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
