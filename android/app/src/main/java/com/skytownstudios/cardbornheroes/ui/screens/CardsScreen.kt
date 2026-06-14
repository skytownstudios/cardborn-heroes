package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.theme.TextMuted
import com.skytownstudios.cardbornheroes.ui.theme.TextPrimary

@Composable
fun CardsScreen(vm: GameViewModel) {
    var subTab by remember { mutableIntStateOf(0) }
    var filter by remember { mutableIntStateOf(0) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cards", fontWeight = FontWeight.Bold, color = TextPrimary)
        TabRow(selectedTabIndex = subTab) {
            Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Owned") })
            Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Lexicon") })
        }
        Spacer(Modifier.height(8.dp))

        if (subTab == 0) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == 0, onClick = { filter = 0 }, label = { Text("All") })
                FilterChip(selected = filter == 1, onClick = { filter = 1 }, label = { Text("Heroes") })
                FilterChip(selected = filter == 2, onClick = { filter = 2 }, label = { Text("Gear") })
            }
            Spacer(Modifier.height(8.dp))
            OwnedGrid(vm, filter)
        } else {
            LexiconGrid(vm)
        }
    }
}

@Composable
private fun OwnedGrid(vm: GameViewModel, filter: Int) {
    data class OwnedCard(val type: String, val id: String, val name: String, val art: String, val tier: String, val qty: Int)

    val cards = buildList {
        if (filter == 0 || filter == 1) {
            vm.player.heroCounts.forEach { (id, qty) ->
                vm.content.hero(id)?.let { h ->
                    add(OwnedCard("hero", id, h.name, h.art, h.tier, qty))
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
