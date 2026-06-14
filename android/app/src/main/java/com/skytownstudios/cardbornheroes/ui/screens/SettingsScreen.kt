package com.skytownstudios.cardbornheroes.ui.screens

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skytownstudios.cardbornheroes.MonetizationConfig
import com.skytownstudios.cardbornheroes.billing.BillingViewModel

@Composable
fun SettingsScreen(modifier: Modifier = Modifier, billing: BillingViewModel) {
    val activity = LocalContext.current as Activity
    val isPremium by billing.isPremium.collectAsState()
    val prices by billing.prices.collectAsState()
    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)
        if (isPremium) {
            Text("Premium active", color = MaterialTheme.colorScheme.primary)
        } else {
            Button(onClick = { billing.purchase(activity, MonetizationConfig.PREMIUM_MONTHLY) }) {
                Text("Monthly ${prices[MonetizationConfig.PREMIUM_MONTHLY] ?: ""}")
            }
            Button(onClick = { billing.purchase(activity, MonetizationConfig.PREMIUM_YEARLY) }) {
                Text("Yearly ${prices[MonetizationConfig.PREMIUM_YEARLY] ?: ""}")
            }
            Button(onClick = { billing.purchase(activity, MonetizationConfig.REMOVE_ADS) }) {
                Text("Remove ads ${prices[MonetizationConfig.REMOVE_ADS] ?: ""}")
            }
        }
        TextButton(onClick = { billing.restore() }) { Text("Restore purchases") }
    }
}
