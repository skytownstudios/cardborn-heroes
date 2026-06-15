package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.theme.TextMuted
import com.skytownstudios.cardbornheroes.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(vm: GameViewModel) {
    if (!vm.showProfile) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val player = vm.player
    val stats = player.stats
    val zone = vm.content.campaignZone(player.activeCampaignId)
    val run = vm.selectedCampaignRun()
    val cardsOwned = player.heroCounts.values.sum() + player.gearCounts.values.sum()
    val winRate = if (stats.wins + stats.losses > 0) {
        (stats.wins * 100 / (stats.wins + stats.losses))
    } else 0

    ModalBottomSheet(
        onDismissRequest = { vm.showProfile = false },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Profile", fontWeight = FontWeight.Bold, color = TextPrimary)
            StatRow("Hand Power", vm.activeHandPower.toString())
            StatRow("Campaign", run?.displayName ?: zone?.name ?: "—")
            StatRow("Cards Owned", cardsOwned.toString())
            StatRow("Packs Opened", stats.packsOpened.toString())
            StatRow("Win Rate", "$winRate%")
            StatRow("Crowns Earned", stats.crownsEarned.toString())
            StatRow("Crafts Completed", stats.craftsDone.toString())
            StatRow("Quests Done", stats.questsClaimed.toString())
            HorizontalDivider()
            Text("Settings", fontWeight = FontWeight.SemiBold, color = TextMuted)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", color = TextPrimary, fontWeight = FontWeight.Medium)
                Switch(
                    checked = vm.darkMode,
                    onCheckedChange = { vm.setDarkMode(it) }
                )
            }
            HorizontalDivider()
            Text("Damage Types", fontWeight = FontWeight.SemiBold, color = TextMuted)
            Text("Coming soon — per-Hand damage breakdown.", color = TextMuted)
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = TextMuted, fontWeight = FontWeight.Medium)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}
