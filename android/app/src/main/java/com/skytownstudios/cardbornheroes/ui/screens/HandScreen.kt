package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.components.MiniCardArt
import com.skytownstudios.cardbornheroes.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandScreen(vm: GameViewModel) {
    var editingHand by remember { mutableIntStateOf(vm.player.activeHandIndex) }
    val hand = vm.player.hands.getOrElse(editingHand) { com.skytownstudios.cardbornheroes.data.Hand() }
    val power = com.skytownstudios.cardbornheroes.data.HandPower.calculate(hand, vm.content)
    val showPicker = vm.equipPickerHandIndex >= 0

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hands", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..2).forEach { i ->
                val active = i == vm.player.activeHandIndex
                FilterChip(
                    selected = editingHand == i,
                    onClick = { editingHand = i },
                    label = { Text("Hand ${i + 1}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HeroGold,
                        selectedLabelColor = ButtonTextOnAccent
                    )
                )
                if (active) {
                    Text("★ Active", fontSize = 11.sp, color = HeroGold, modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        }

        Text("Power: $power", fontWeight = FontWeight.SemiBold, color = HeroGold)

        if (editingHand != vm.player.activeHandIndex) {
            GameButtonCompat("Set as Active Hand", onClick = { vm.setActiveHand(editingHand) })
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            hand.slots.forEachIndexed { slotIndex, slot ->
                val art = when (slot.type) {
                    "hero" -> vm.content.hero(slot.cardId)?.art
                    "gear" -> vm.content.gear(slot.cardId)?.art
                    else -> null
                }
                val name = when (slot.type) {
                    "hero" -> vm.content.hero(slot.cardId)?.name ?: "Empty"
                    "gear" -> vm.content.gear(slot.cardId)?.name ?: "Empty"
                    else -> "Empty"
                }
                Column(
                    Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MintBgDeep)
                        .border(1.dp, HeroCardBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            if (slot.isEmpty) {
                                vm.equipPickerHandIndex = editingHand
                                vm.equipPickerSlotIndex = slotIndex
                            } else {
                                vm.unequipSlot(editingHand, slotIndex)
                            }
                        }
                        .padding(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MiniCardArt(
                        artPath = art,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.75f)
                    )
                    Text(name, fontSize = 9.sp, color = TextMuted, maxLines = 1)
                }
            }
        }

        Text("Tap empty slot to equip · tap filled to remove", fontSize = 11.sp, color = TextMuted)
    }

    if (showPicker) {
        EquipPickerSheet(vm, editingHand, vm.equipPickerSlotIndex) {
            vm.equipPickerHandIndex = -1
            vm.equipPickerSlotIndex = -1
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipPickerSheet(
    vm: GameViewModel,
    handIndex: Int,
    slotIndex: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Equip to slot ${slotIndex + 1}", fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            item { Text("Heroes", fontWeight = FontWeight.SemiBold, color = TextMuted) }
            items(vm.player.heroCounts.keys.toList()) { id ->
                val count = vm.availableCount("hero", id, handIndex, slotIndex)
                if (count <= 0) return@items
                val hero = vm.content.hero(id) ?: return@items
                EquipRow(hero.name, hero.art, count) {
                    vm.equipCard(handIndex, slotIndex, "hero", id)
                    onDismiss()
                }
            }
            item { Text("Gear", fontWeight = FontWeight.SemiBold, color = TextMuted) }
            items(vm.player.gearCounts.keys.toList()) { id ->
                val count = vm.availableCount("gear", id, handIndex, slotIndex)
                if (count <= 0) return@items
                val gear = vm.content.gear(id) ?: return@items
                EquipRow(gear.name, gear.art, count) {
                    vm.equipCard(handIndex, slotIndex, "gear", id)
                    onDismiss()
                }
            }
        }
    }
}

@Composable
private fun EquipRow(name: String, art: String, count: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceMint)
            .clickable(onClick = onClick)
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CardArt(artPath = art, name = name, modifier = Modifier.size(48.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text("Available: $count", fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
private fun GameButtonCompat(text: String, onClick: () -> Unit) {
    com.skytownstudios.cardbornheroes.ui.components.GameButton(text, onClick)
}
