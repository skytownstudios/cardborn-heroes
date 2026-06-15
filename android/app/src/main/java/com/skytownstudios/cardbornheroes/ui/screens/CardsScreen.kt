package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytownstudios.cardbornheroes.data.HeroInventory
import com.skytownstudios.cardbornheroes.data.PowerScale
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.theme.*

@Composable
fun CardsScreen(vm: GameViewModel) {
    var subTab by remember { mutableIntStateOf(0) }
    var filter by remember { mutableIntStateOf(0) }
    var selectedEnemyId by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cards", fontWeight = FontWeight.Bold, color = TextPrimary)
        TabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Owned") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Lexicon") })
            Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Bestiary") })
        }
        Spacer(Modifier.height(8.dp))

        when (subTab) {
            0 -> {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("All") })
                    FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("Heroes") })
                    FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("Gear") })
                }
                Spacer(Modifier.height(8.dp))
                OwnedGrid(vm, filter)
            }
            1 -> LexiconGrid(vm)
            2 -> BestiaryGrid(vm, selectedEnemyId) { selectedEnemyId = it }
        }
    }

    selectedEnemyId?.let { id ->
        vm.content.enemy(id)?.let { enemy ->
            EnemyDetailSheet(enemy, vm.player.discoveredEnemies.contains(id)) {
                selectedEnemyId = null
            }
        }
    }
}

@Composable
private fun OwnedGrid(vm: GameViewModel, filter: Int) {
    data class OwnedCard(val type: String, val id: String, val name: String, val art: String, val tier: String, val qty: Int)

    val cards = buildList {
        if (filter == 0 || filter == 1) {
            vm.player.heroCounts.forEach { (key, qty) ->
                val (id, stars) = HeroInventory.parseStackKey(key)
                vm.content.hero(id)?.let { h ->
                    val name = if (stars > 0) "${h.name} ★$stars" else h.name
                    add(OwnedCard("hero", id, name, h.art, h.tier, qty))
                }
            }
        }
        if (filter == 0 || filter == 2) {
            vm.player.gearCounts.forEach { (id, qty) ->
                vm.content.gear(id)?.let { g ->
                    add(OwnedCard("gear", id, g.name, g.art, g.tier, qty))
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(cards) { card ->
            Column {
                CardArt(
                    artPath = card.art,
                    name = card.name,
                    tier = card.tier,
                    qty = card.qty,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                )
                Text(card.name, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1)
            }
        }
    }
}

@Composable
private fun LexiconGrid(vm: GameViewModel) {
    data class LexEntry(val type: String, val id: String, val name: String, val art: String, val tier: String, val discovered: Boolean)

    val entries = buildList {
        vm.content.heroes.forEach { h ->
            add(LexEntry("hero", h.id, h.name, h.art, h.tier, h.id in vm.player.discoveredHeroes))
        }
        vm.content.gear.forEach { g ->
            add(LexEntry("gear", g.id, g.name, g.art, g.tier, g.id in vm.player.discoveredGear))
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(entries) { entry ->
            Column {
                CardArt(
                    artPath = if (entry.discovered) entry.art else null,
                    name = entry.name,
                    tier = entry.tier,
                    discovered = entry.discovered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                )
                Text(
                    if (entry.discovered) entry.name else "???",
                    fontWeight = FontWeight.Medium,
                    color = if (entry.discovered) TextPrimary else TextMuted,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun BestiaryGrid(vm: GameViewModel, selectedId: String?, onSelect: (String) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(vm.content.enemies, key = { it.id }) { enemy ->
            val discovered = enemy.id in vm.player.discoveredEnemies
            val power = PowerScale.combatPower(enemy.stats)
            Column(
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = discovered) { onSelect(enemy.id) }
            ) {
                CardArt(
                    artPath = if (discovered) enemy.art else null,
                    name = enemy.name,
                    discovered = discovered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.72f)
                )
                Text(
                    if (discovered) enemy.name else "???",
                    fontWeight = FontWeight.Medium,
                    color = if (discovered) TextPrimary else TextMuted,
                    maxLines = 1,
                    fontSize = 12.sp
                )
                if (discovered) {
                    Text("PWR $power", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnemyDetailSheet(
    enemy: com.skytownstudios.cardbornheroes.data.EnemyDef,
    discovered: Boolean,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                CardArt(
                    artPath = if (discovered) enemy.art else null,
                    name = enemy.name,
                    modifier = Modifier.size(72.dp),
                    discovered = discovered
                )
                Column {
                    Text(enemy.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
                    Text(
                        "${enemy.role.replaceFirstChar { it.uppercase() }} · PWR ${PowerScale.combatPower(enemy.stats)}",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
            if (discovered) {
                Text("Stats", fontWeight = FontWeight.SemiBold, color = TextMuted)
                EnemyStatRow("Life", enemy.stats.hp.toString())
                EnemyStatRow("ATK", enemy.stats.atk.toString())
                EnemyStatRow("DEF", enemy.stats.def.toString())
                if (enemy.stats.energy > 0) EnemyStatRow("Energy", enemy.stats.energy.toString())

                HorizontalDivider()

                Text("Weak to", fontWeight = FontWeight.SemiBold, color = TextMuted)
                Text(enemy.weakTo.joinToString { it.replaceFirstChar { c -> c.uppercase() } }, color = TextPrimary)

                Text("Strong vs", fontWeight = FontWeight.SemiBold, color = TextMuted)
                Text(enemy.strongAgainst.joinToString { it.replaceFirstChar { c -> c.uppercase() } }, color = TextPrimary)

                Text("Counter tip", fontWeight = FontWeight.SemiBold, color = TextMuted)
                Text(enemy.counterTip, color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun EnemyStatRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextMuted, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Medium, color = TextPrimary, fontSize = 13.sp)
    }
}
