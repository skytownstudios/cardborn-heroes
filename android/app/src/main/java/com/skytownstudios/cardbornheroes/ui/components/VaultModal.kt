package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skytownstudios.cardbornheroes.data.HeroInventory
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.theme.MintBgDeep
import com.skytownstudios.cardbornheroes.ui.theme.TextMuted
import com.skytownstudios.cardbornheroes.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultModal(vm: GameViewModel) {
    if (!vm.showVault) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val player = vm.player

    ModalBottomSheet(
        onDismissRequest = { vm.showVault = false },
        sheetState = sheetState
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Vault", fontWeight = FontWeight.Bold, color = TextPrimary)
            VaultSection("Currencies") {
                VaultRow("Crowns", player.crowns.toString())
                VaultRow("Sigils", player.sigils.toString())
            }
            VaultSection("Materials") {
                player.materials.forEach { (id, qty) ->
                    val name = vm.content.material(id)?.name ?: id
                    VaultRow(name, qty.toString())
                }
            }
            VaultSection("Ascendant Packs") {
                if (player.packInventory.isEmpty()) {
                    VaultRow("—", "None")
                } else {
                    player.packInventory.forEach { (id, qty) ->
                        val name = vm.content.pack(id)?.name ?: id
                        VaultRow(name, qty.toString())
                    }
                }
            }
            VaultSection("Heroes & Units") {
                player.heroCounts.forEach { (key, qty) ->
                    val (id, stars) = HeroInventory.parseStackKey(key)
                    val hero = vm.content.hero(id)
                    val base = hero?.name ?: id
                    val label = if (stars > 0) "$base ★$stars" else base
                    VaultRow(label, "×$qty")
                }
            }
            VaultSection("Gear") {
                player.gearCounts.forEach { (id, qty) ->
                    val name = vm.content.gear(id)?.name ?: id
                    VaultRow(name, "×$qty")
                }
            }
        }
    }
}

@Composable
private fun VaultSection(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(MintBgDeep, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, fontWeight = FontWeight.SemiBold, color = TextMuted)
        content()
    }
}

@Composable
private fun VaultRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextPrimary)
        Text(value, fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
