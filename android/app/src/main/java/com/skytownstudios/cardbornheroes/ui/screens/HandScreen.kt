package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HandScreen(vm: GameViewModel) {
    val handIndex = vm.player.activeHandIndex
    val hand = vm.player.hands.getOrElse(handIndex) { com.skytownstudios.cardbornheroes.data.Hand() }
    val power = com.skytownstudios.cardbornheroes.data.HandPower.calculate(hand, vm.content)
    val showPicker = vm.equipPickerHandIndex >= 0

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hands", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
        Text("Equip up to 5 heroes. Attach weapons to each hero.", fontSize = 12.sp, color = TextMuted)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (0..2).forEach { i ->
                val selected = i == handIndex
                FilterChip(
                    selected = selected,
                    onClick = { vm.setActiveHand(i) },
                    label = {
                        Text(
                            if (selected) "Hand ${i + 1} · Active" else "Hand ${i + 1}",
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HeroGold,
                        selectedLabelColor = ButtonTextOnAccent
                    )
                )
            }
        }

        Text("Power: $power", fontWeight = FontWeight.SemiBold, color = HeroGold)

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            hand.heroSlots.forEachIndexed { slotIndex, loadout ->
                HeroLoadoutRow(vm, handIndex, slotIndex, loadout)
            }
        }
    }

    if (showPicker) {
        EquipPickerSheet(vm, handIndex, vm.equipPickerSlotIndex, vm.equipPickerTarget) {
            vm.closeEquipPicker()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HeroLoadoutRow(
    vm: GameViewModel,
    handIndex: Int,
    slotIndex: Int,
    loadout: com.skytownstudios.cardbornheroes.data.HeroLoadout
) {
    val hero = loadout.heroId.takeIf { it.isNotEmpty() }?.let { vm.content.hero(it) }
    val mainGear = loadout.mainHandGearId.takeIf { it.isNotEmpty() }?.let { vm.content.gear(it) }
    val offGear = loadout.offHandGearId.takeIf { it.isNotEmpty() }?.let { vm.content.gear(it) }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MintBgDeep)
            .border(1.dp, HeroCardBorder, RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .width(72.dp)
                .combinedClickable(
                    onClick = {
                        if (loadout.isEmpty) vm.openHeroPicker(handIndex, slotIndex)
                    },
                    onLongClick = {
                        if (!loadout.isEmpty) vm.clearHeroSlot(handIndex, slotIndex)
                    }
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MiniCardArt(
                artPath = hero?.art,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
            )
            Text(
                hero?.name ?: "Hero ${slotIndex + 1}",
                fontSize = 9.sp,
                color = if (hero != null) TextPrimary else TextMuted,
                maxLines = 1
            )
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (hero != null) hero.name else "Empty slot",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WeaponSubSlot(
                    label = "Main",
                    artPath = mainGear?.art,
                    enabled = !loadout.isEmpty,
                    onClick = {
                        if (loadout.isEmpty) vm.openHeroPicker(handIndex, slotIndex)
                        else vm.openMainHandPicker(handIndex, slotIndex)
                    },
                    onLongClick = {
                        if (mainGear != null) vm.clearMainHand(handIndex, slotIndex)
                    }
                )
                WeaponSubSlot(
                    label = "Off",
                    artPath = offGear?.art,
                    enabled = !loadout.isEmpty && (mainGear == null || mainGear.hands < 2),
                    onClick = {
                        if (loadout.isEmpty) vm.openHeroPicker(handIndex, slotIndex)
                        else vm.openOffHandPicker(handIndex, slotIndex)
                    },
                    onLongClick = {
                        if (offGear != null) vm.clearOffHand(handIndex, slotIndex)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WeaponSubSlot(
    label: String,
    artPath: String?,
    enabled: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (enabled) SurfaceMint else MintBgDeep.copy(alpha = 0.5f))
                .border(1.dp, SurfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!artPath.isNullOrBlank()) {
                MiniCardArt(artPath = artPath, modifier = Modifier.size(40.dp))
            } else {
                Text("+", fontSize = 18.sp, color = TextMuted)
            }
        }
        Text(label, fontSize = 9.sp, color = TextMuted)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipPickerSheet(
    vm: GameViewModel,
    handIndex: Int,
    slotIndex: Int,
    target: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val title = when (target) {
        "main" -> "Main hand — slot ${slotIndex + 1}"
        "off" -> "Off hand — slot ${slotIndex + 1}"
        else -> "Hero — slot ${slotIndex + 1}"
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            when (target) {
                "hero" -> {
                    items(vm.player.heroCounts.keys.toList()) { id ->
                        val count = vm.availableHeroCount(id, handIndex, slotIndex)
                        if (count <= 0) return@items
                        val hero = vm.content.hero(id) ?: return@items
                        EquipRow(hero.name, hero.art, count) {
                            vm.equipHero(handIndex, slotIndex, id)
                            onDismiss()
                        }
                    }
                }
                "main", "off" -> {
                    items(vm.player.gearCounts.keys.toList()) { id ->
                        if (!vm.canEquipGearOnSlot(id, handIndex, slotIndex, target)) return@items
                        val gear = vm.content.gear(id) ?: return@items
                        val count = vm.availableGearCount(id, handIndex, slotIndex, target)
                        if (count <= 0) return@items
                        EquipRow(gear.name, gear.art, count) {
                            if (target == "main") vm.equipMainHand(handIndex, slotIndex, id)
                            else vm.equipOffHand(handIndex, slotIndex, id)
                            onDismiss()
                        }
                    }
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
