package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.skytownstudios.cardbornheroes.data.Hand
import com.skytownstudios.cardbornheroes.data.HandPower
import com.skytownstudios.cardbornheroes.data.HeroLoadout
import com.skytownstudios.cardbornheroes.data.HeroInventory
import com.skytownstudios.cardbornheroes.data.LoadoutHelper
import com.skytownstudios.cardbornheroes.data.PowerScale
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.components.MiniCardArt
import com.skytownstudios.cardbornheroes.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HandScreen(vm: GameViewModel) {
    val handIndex = vm.player.activeHandIndex
    val hand = vm.player.hands.getOrElse(handIndex) { Hand() }
    val power = HandPower.calculate(hand, vm.content)
    val showPicker = vm.equipPickerHandIndex >= 0
    val showDetail = vm.slotDetailHandIndex >= 0

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hands", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
        Text("Tap a hero or weapon to view stats. Tap + to equip.", fontSize = 12.sp, color = TextMuted)

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

    if (showDetail) {
        SlotDetailSheet(
            vm,
            handIndex,
            vm.slotDetailSlotIndex,
            vm.slotDetailTarget
        ) {
            vm.closeSlotDetail()
        }
    }
}

@Composable
private fun HeroLoadoutRow(
    vm: GameViewModel,
    handIndex: Int,
    slotIndex: Int,
    loadout: HeroLoadout
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
                .clickable {
                    if (loadout.isEmpty) vm.openHeroPicker(handIndex, slotIndex)
                    else vm.openSlotDetail(handIndex, slotIndex, "hero")
                },
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
                        else if (mainGear != null) vm.openSlotDetail(handIndex, slotIndex, "main")
                        else vm.openMainHandPicker(handIndex, slotIndex)
                    }
                )
                WeaponSubSlot(
                    label = "Off",
                    artPath = offGear?.art,
                    enabled = !loadout.isEmpty && (mainGear == null || mainGear.hands < 2),
                    onClick = {
                        if (loadout.isEmpty) vm.openHeroPicker(handIndex, slotIndex)
                        else if (offGear != null) vm.openSlotDetail(handIndex, slotIndex, "off")
                        else vm.openOffHandPicker(handIndex, slotIndex)
                    }
                )
            }
        }
    }
}

@Composable
private fun WeaponSubSlot(
    label: String,
    artPath: String?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .clickable(enabled = enabled, onClick = onClick)
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
private fun SlotDetailSheet(
    vm: GameViewModel,
    handIndex: Int,
    slotIndex: Int,
    target: String,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val loadout = vm.player.hands.getOrNull(handIndex)?.heroSlots?.getOrNull(slotIndex) ?: HeroLoadout()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (target) {
                "hero" -> HeroDetailContent(vm, loadout)
                "main" -> GearDetailContent(vm, loadout.mainHandGearId, "Main hand")
                "off" -> GearDetailContent(vm, loadout.offHandGearId, "Off hand")
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        when (target) {
                            "hero" -> vm.openHeroPicker(handIndex, slotIndex)
                            "main" -> vm.openMainHandPicker(handIndex, slotIndex)
                            "off" -> vm.openOffHandPicker(handIndex, slotIndex)
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Change")
                }
                Button(
                    onClick = {
                        when (target) {
                            "hero" -> vm.clearHeroSlot(handIndex, slotIndex)
                            "main" -> vm.clearMainHand(handIndex, slotIndex)
                            "off" -> vm.clearOffHand(handIndex, slotIndex)
                        }
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceBorder)
                ) {
                    Text("Unequip", color = TextPrimary)
                }
            }
        }
    }
}

@Composable
private fun HeroDetailContent(vm: GameViewModel, loadout: HeroLoadout) {
    val hero = vm.content.hero(loadout.heroId) ?: return
    val merged = LoadoutHelper.mergedStats(loadout, vm.content)
    val style = LoadoutHelper.attackStyle(loadout, vm.content)

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardArt(artPath = hero.art, name = hero.name, modifier = Modifier.size(72.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(hero.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            val kind = if (hero.isHero) "Hero" else "Unit"
            val starLabel = if (loadout.heroStars > 0) " · ★${loadout.heroStars}" else ""
            Text(
                "$kind · ${hero.tier.replaceFirstChar { it.uppercase() }} · ${hero.role.replaceFirstChar { it.uppercase() }}$starLabel",
                color = TextMuted,
                fontSize = 12.sp
            )
        }
    }

    HorizontalDivider()

    Text("Base stats", fontWeight = FontWeight.SemiBold, color = TextMuted)
    val scaled = PowerScale.scaledStats(hero.stats, loadout.heroStars)
    StatLine("Life", scaled.hp.toString())
    StatLine("ATK", scaled.atk.toString())
    StatLine("DEF", scaled.def.toString())
    if (scaled.energy > 0) StatLine("Energy", scaled.energy.toString())
    StatLine("Power", PowerScale.combatPower(scaled).toString())

    if (merged != null && (loadout.mainHandGearId.isNotEmpty() || loadout.offHandGearId.isNotEmpty())) {
        HorizontalDivider()
        Text("With gear", fontWeight = FontWeight.SemiBold, color = TextMuted)
        StatLine("HP", merged.hp.toString())
        StatLine("ATK", merged.atk.toString())
        StatLine("DEF", merged.def.toString())
        if (merged.energy > 0) StatLine("Energy", merged.energy.toString())
        StatLine("Power", PowerScale.combatPower(merged).toString())
        StatLine("Style", style.replace('_', ' '))
    }
}

@Composable
private fun GearDetailContent(vm: GameViewModel, gearId: String, slotLabel: String) {
    val gear = vm.content.gear(gearId) ?: return

    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CardArt(artPath = gear.art, name = gear.name, modifier = Modifier.size(72.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(gear.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 18.sp)
            Text("$slotLabel · ${gear.tier.replaceFirstChar { it.uppercase() }}", color = TextMuted, fontSize = 12.sp)
        }
    }

    HorizontalDivider()

    Text("Bonus stats", fontWeight = FontWeight.SemiBold, color = TextMuted)
    StatLine("HP", "+${gear.bonus.hp}")
    StatLine("ATK", "+${gear.bonus.atk}")
    StatLine("DEF", "+${gear.bonus.def}")
    StatLine("Hands", if (gear.hands >= 2) "Two-handed" else "One-handed")
    StatLine("Roles", gear.compatibleRoles.joinToString(", ") { it.replaceFirstChar { c -> c.uppercase() } })
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontWeight = FontWeight.Medium)
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold)
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
    val loadout = vm.player.hands.getOrNull(handIndex)?.heroSlots?.getOrNull(slotIndex) ?: HeroLoadout()
    val title = when (target) {
        "main" -> "Main hand — slot ${slotIndex + 1}"
        "off" -> "Off hand — slot ${slotIndex + 1}"
        else -> "Hero — slot ${slotIndex + 1}"
    }
    val hasEquipped = when (target) {
        "hero" -> !loadout.isEmpty
        "main" -> loadout.mainHandGearId.isNotEmpty()
        "off" -> loadout.offHandGearId.isNotEmpty()
        else -> false
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
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
            if (hasEquipped) {
                item {
                    TextButton(
                        onClick = {
                            when (target) {
                                "hero" -> vm.clearHeroSlot(handIndex, slotIndex)
                                "main" -> vm.clearMainHand(handIndex, slotIndex)
                                "off" -> vm.clearOffHand(handIndex, slotIndex)
                            }
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unequip current", color = HeroGold)
                    }
                }
            }
            when (target) {
                "hero" -> {
                    items(vm.heroStacksForPicker(handIndex, slotIndex), key = { "${it.heroId}@${it.stars}" }) { stack ->
                        val hero = vm.content.hero(stack.heroId) ?: return@items
                        val label = if (stack.stars > 0) "${hero.name} ★${stack.stars}" else hero.name
                        val kind = if (hero.isHero) "Hero" else "Unit"
                        EquipRow("$label · $kind", hero.art, stack.count) {
                            vm.equipHero(handIndex, slotIndex, stack.heroId, stack.stars)
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
