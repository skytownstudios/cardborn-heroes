package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.skytownstudios.cardbornheroes.data.BattleUnit
import com.skytownstudios.cardbornheroes.data.RigManifest
import com.skytownstudios.cardbornheroes.data.FarmArea
import com.skytownstudios.cardbornheroes.data.PendingFarmRewards
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.AssetIcon
import com.skytownstudios.cardbornheroes.ui.components.BattleFighterSprite
import com.skytownstudios.cardbornheroes.ui.components.GameButton
import com.skytownstudios.cardbornheroes.ui.components.GameCardFrame
import com.skytownstudios.cardbornheroes.ui.components.MiniCardArt
import com.skytownstudios.cardbornheroes.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BattleScreen(vm: GameViewModel) {
    Box(Modifier.fillMaxSize()) {
        BattleHub(vm)
        if (vm.showQuests) QuestModal(vm) { vm.showQuests = false }
        if (vm.showFarmClaimModal) FarmClaimModal(vm) { vm.dismissFarmClaimModal() }
    }
}

private enum class BattleMode { Campaign, AfkFarm }

@Composable
private fun BattleHub(vm: GameViewModel) {
    var mode by remember { mutableStateOf(BattleMode.Campaign) }
    val player = vm.player
    val farm = vm.content.farm(player.activeFarmId)
    val scroll = rememberScrollState()
    val canBattle = vm.activeHand.heroSlots.any { !it.isEmpty } && vm.selectedCampaignRun() != null
    val farmHasRewards = !player.pendingFarmRewards.isEmpty

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BattleModeSelector(
            mode = mode,
            farmHasRewards = farmHasRewards,
            onSelect = { mode = it }
        )

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            when (mode) {
                BattleMode.Campaign -> CampaignAreaSection(vm)
                BattleMode.AfkFarm -> FarmAreaSection(vm, farm)
            }
        }

        if (mode == BattleMode.Campaign) {
            AfkChestCompactBar(
                vm = vm,
                onOpenFarm = { mode = BattleMode.AfkFarm }
            )

            HorizontalDivider(color = SurfaceBorder.copy(alpha = 0.35f))

            Text("Active Hand", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                vm.activeHand.heroSlots.forEach { slot ->
                    val art = slot.heroId.takeIf { it.isNotEmpty() }?.let { vm.content.hero(it)?.art }
                    MiniCardArt(
                        artPath = art,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                    )
                }
            }

            GameButton(
                text = "Battle",
                onClick = { vm.beginBattle() },
                enabled = canBattle
            )
        }
    }
}

/** AFK Arena–style mode switch: campaign is the main screen; farm is its own section. */
@Composable
private fun BattleModeSelector(
    mode: BattleMode,
    farmHasRewards: Boolean,
    onSelect: (BattleMode) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MintBgDeep.copy(alpha = 0.6f))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        BattleModeTab(
            label = "Campaign",
            selected = mode == BattleMode.Campaign,
            onClick = { onSelect(BattleMode.Campaign) },
            modifier = Modifier.weight(1f)
        )
        BattleModeTab(
            label = "AFK Farm",
            selected = mode == BattleMode.AfkFarm,
            showDot = farmHasRewards,
            onClick = { onSelect(BattleMode.AfkFarm) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun BattleModeTab(
    label: String,
    selected: Boolean,
    showDot: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) HeroGold.copy(alpha = 0.22f) else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) HeroGold.copy(alpha = 0.55f) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) TextPrimary else TextMuted,
                fontSize = 13.sp
            )
            if (showDot) {
                Box(
                    Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(HeroGold)
                )
            }
        }
    }
}

/** Compact idle chest on the campaign screen — tap to claim or open farm settings (AFK Arena pattern). */
@Composable
private fun AfkChestCompactBar(vm: GameViewModel, onOpenFarm: () -> Unit) {
    val player = vm.player
    val pending = player.pendingFarmRewards
    val hasRewards = !pending.isEmpty
    val badge = pending.badgeCount()
    val farm = vm.content.farm(player.activeFarmId)
    val mult = com.skytownstudios.cardbornheroes.data.HandPower.farmMultiplier(vm.activeHandPower)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    if (hasRewards) vm.claimFarmRewards() else onOpenFarm()
                },
            contentAlignment = Alignment.Center
        ) {
            AssetIcon(
                "ui/icon_farm_chest.png",
                if (hasRewards) "Claim AFK loot" else "AFK farm chest",
                modifier = Modifier.size(44.dp)
            )
            if (badge > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HeroGold)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        if (badge > 99) "99+" else badge.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = ButtonTextOnAccent
                    )
                }
            }
        }

        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                if (hasRewards) "AFK loot ready!" else "Idle farming",
                fontWeight = FontWeight.SemiBold,
                color = if (hasRewards) HeroGold else TextPrimary,
                fontSize = 12.sp
            )
            Text(
                if (hasRewards) "Tap chest to claim"
                else "${farm?.name ?: "No zone"} · Hand ×${"%.1f".format(mult)}",
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        TextButton(onClick = onOpenFarm) {
            Text("Farm", color = HeroGold, fontSize = 12.sp)
        }
    }
}

@Composable
private fun CampaignAreaSection(vm: GameViewModel) {
    val player = vm.player
    val run = vm.selectedCampaignRun()
    val zone = vm.content.campaignZone(player.activeCampaignId)
    val maxLevel = vm.maxPlayableLevel(player.activeCampaignId)
    val bestLevel = player.campaignBestLevel[player.activeCampaignId] ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Campaigns", fontWeight = FontWeight.Bold, color = HeroGold, fontSize = 16.sp)
                Text("Pick a zone and level — farm the rewards you need.", fontSize = 10.sp, color = TextMuted)
            }
            IconButton(onClick = { vm.showQuests = true }) {
                AssetIcon("ui/icon_quests.png", "Quests", size = 28.dp)
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vm.content.campaignZones.forEach { campaign ->
                CampaignZoneTile(
                    zone = campaign,
                    selected = campaign.id == player.activeCampaignId,
                    bestLevel = player.campaignBestLevel[campaign.id] ?: 0,
                    onClick = { vm.selectCampaign(campaign.id) }
                )
            }
        }

        if (zone != null && run != null) {
            ActiveCampaignCard(vm, zone, run, maxLevel, bestLevel)
        }
    }
}

@Composable
private fun CampaignZoneTile(
    zone: com.skytownstudios.cardbornheroes.data.CampaignZone,
    selected: Boolean,
    bestLevel: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(HeroGold.copy(alpha = 0.28f), PanelBottom))
                } else {
                    Brush.verticalGradient(listOf(PanelTop, PanelBottom))
                }
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HeroGold else SurfaceBorder.copy(alpha = 0.45f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AssetIcon(zone.icon, zone.name, size = 40.dp)
        Text(
            zone.name,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
        if (bestLevel > 0) {
            Text("Best L$bestLevel", fontSize = 8.sp, color = HeroGold)
        }
    }
}

@Composable
private fun ActiveCampaignCard(
    vm: GameViewModel,
    zone: com.skytownstudios.cardbornheroes.data.CampaignZone,
    run: com.skytownstudios.cardbornheroes.data.CampaignRun,
    maxLevel: Int,
    bestLevel: Int
) {
    val player = vm.player

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, HeroGold.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameCardFrame(Modifier.size(64.dp)) {
                    AssetIcon(zone.icon, zone.name, modifier = Modifier.fillMaxSize().padding(8.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(zone.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text(
                        zone.description,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Level", fontWeight = FontWeight.SemiBold, color = TextMuted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    LevelStepButton("−", enabled = player.activeCampaignLevel > 1) {
                        vm.setCampaignLevel(player.activeCampaignLevel - 1)
                    }
                    Text(
                        "Lvl ${player.activeCampaignLevel}",
                        fontWeight = FontWeight.Bold,
                        color = HeroGold,
                        fontSize = 14.sp,
                        modifier = Modifier.widthIn(min = 48.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    LevelStepButton("＋", enabled = player.activeCampaignLevel < maxLevel) {
                        vm.setCampaignLevel(player.activeCampaignLevel + 1)
                    }
                }
            }
            if (maxLevel > 1) {
                Text(
                    if (bestLevel > 0) "Cleared L$bestLevel · up to L$maxLevel available"
                    else "Beat L$maxLevel to unlock the next tier",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FarmStatBadge(
                    icon = "ui/currency_crowns.png",
                    label = "Crowns",
                    value = "${run.crownRewardMin}–${run.crownRewardMax}",
                    modifier = Modifier.weight(1f)
                )
                FarmStatBadge(
                    icon = "ui/icon_bag.png",
                    label = "PWR needed",
                    value = "${run.recommendedPower}",
                    modifier = Modifier.weight(1f)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Loot drops", fontWeight = FontWeight.SemiBold, color = TextMuted, fontSize = 11.sp)
                run.materialRewards.forEach { reward ->
                    val mat = vm.content.material(reward.id)
                    FarmStatBadge(
                        icon = mat?.art ?: "ui/mat_steel.png",
                        label = mat?.name ?: reward.id,
                        value = "${reward.min}–${reward.max}",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            val foeLine = run.enemyIds.mapNotNull { id ->
                vm.content.enemy(id)?.let { e ->
                    if (id in player.discoveredEnemies) e.name else "???"
                }
            }.joinToString(" · ")
            if (foeLine.isNotEmpty()) {
                Text("Foes: $foeLine", fontSize = 10.sp, color = TextMuted, maxLines = 2)
            }
        }
    }
}

@Composable
private fun LevelStepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (enabled) HeroGold.copy(alpha = 0.2f) else SurfaceBorder.copy(alpha = 0.2f))
            .border(1.dp, if (enabled) HeroGold.copy(alpha = 0.5f) else SurfaceBorder, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (enabled) HeroGold else TextMuted, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun FarmAreaSection(vm: GameViewModel, farm: FarmArea?) {
    val player = vm.player
    val handBonus = com.skytownstudios.cardbornheroes.data.HandPower
        .farmMultiplier(vm.activeHandPower)

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("AFK Farm", fontWeight = FontWeight.Bold, color = HeroGold, fontSize = 16.sp)
                Text("Send your Hand to gather loot while you play.", fontSize = 10.sp, color = TextMuted)
            }
            FarmHandBonusBadge(handBonus)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vm.content.farmAreas.forEach { area ->
                FarmZoneTile(
                    area = area,
                    selected = area.id == player.activeFarmId,
                    onClick = { vm.selectFarm(area.id) }
                )
            }
        }

        farm?.let { active ->
            ActiveFarmZoneCard(vm, active)
        }
    }
}

@Composable
private fun FarmHandBonusBadge(multiplier: Float) {
    Box(
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(HeroGold.copy(alpha = 0.15f))
            .border(1.dp, HeroGold.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            "Hand ×${"%.1f".format(multiplier)}",
            color = HeroGold,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun FarmZoneTile(area: FarmArea, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(84.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.verticalGradient(listOf(HeroGold.copy(alpha = 0.28f), PanelBottom))
                } else {
                    Brush.verticalGradient(listOf(PanelTop, PanelBottom))
                }
            )
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) HeroGold else SurfaceBorder.copy(alpha = 0.45f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AssetIcon(farmIconFor(area.id), area.name, size = 40.dp)
        Text(
            area.name,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) TextPrimary else TextMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 11.sp
        )
    }
}

@Composable
private fun ActiveFarmZoneCard(vm: GameViewModel, farm: FarmArea) {
    val material = vm.content.material(farm.primaryMaterial)
    val pending = vm.player.pendingFarmRewards
    val hasRewards = !pending.isEmpty

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(PanelTop, PanelBottom)))
            .border(2.dp, HeroGold.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GameCardFrame(Modifier.size(64.dp)) {
                    AssetIcon(farmIconFor(farm.id), farm.name, modifier = Modifier.fillMaxSize().padding(4.dp))
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FarmingStatusChip(hasRewards = hasRewards)
                    Text(farm.name, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
                    Text(
                        farm.description,
                        fontSize = 11.sp,
                        color = TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FarmStatBadge(
                        icon = "ui/currency_crowns.png",
                        label = "Crowns",
                        value = "${vm.effectiveCrownsPerHour()}/hr",
                        modifier = Modifier.weight(1f)
                    )
                    FarmStatBadge(
                        icon = if (farm.id == "cardborn_vault") "ui/icon_bag.png" else (material?.art ?: "ui/mat_steel.png"),
                        label = if (farm.id == "cardborn_vault") "All materials" else (material?.name ?: "Loot"),
                        value = vm.effectiveMaterialPerHour(farm),
                        modifier = Modifier.weight(1f)
                    )
                }
                vm.effectiveSecondaryDrop(farm)?.let { (label, rate) ->
                    FarmStatBadge(
                        icon = when (farm.secondaryDrop?.type) {
                            "hero" -> "ui/tab_hand.png"
                            "gear" -> "ui/tab_forge.png"
                            else -> "ui/icon_quests.png"
                        },
                        label = label,
                        value = rate,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                val packRows = vm.effectivePackDrops(farm)
                if (packRows.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        packRows.forEach { pack ->
                            FarmStatBadge(
                                icon = pack.icon,
                                label = pack.label,
                                value = pack.rate,
                                sublabel = pack.packName,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MintBgDeep.copy(alpha = 0.55f))
                    .border(1.dp, SurfaceBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                FarmRewardChest(vm)
            }
        }
    }
}

@Composable
private fun FarmingStatusChip(hasRewards: Boolean) {
    val pulse = rememberInfiniteTransition(label = "farmPulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse),
        label = "dotAlpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (hasRewards) HeroGold.copy(alpha = 0.2f)
                else HeroGold.copy(alpha = 0.1f)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(
            Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(HeroGold.copy(alpha = dotAlpha))
        )
        Text(
            if (hasRewards) "Rewards ready!" else "Farming active",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = HeroGold
        )
    }
}

@Composable
private fun FarmStatBadge(
    icon: String,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    sublabel: String? = null
) {
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MintBgDeep.copy(alpha = 0.7f))
            .border(1.dp, SurfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        AssetIcon(icon, label, size = 20.dp)
        Column {
            Text(label, fontSize = 9.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HeroGold)
            if (!sublabel.isNullOrBlank()) {
                Text(
                    sublabel,
                    fontSize = 8.sp,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun farmIconFor(farmId: String): String = when (farmId) {
    "goblin_hills" -> "maps/farm_goblin_hills.png"
    "arcane_ruins" -> "maps/farm_arcane_ruins.png"
    "heroes_rest" -> "maps/farm_heroes_rest.png"
    "cardborn_vault" -> "maps/farm_cardborn_vault.png"
    else -> "maps/farm_goblin_hills.png"
}

@Composable
private fun FarmRewardChest(vm: GameViewModel) {
    val pending = vm.player.pendingFarmRewards
    val hasRewards = !pending.isEmpty
    val badge = pending.badgeCount()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = hasRewards) { vm.claimFarmRewards() }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier.size(64.dp),
            contentAlignment = Alignment.Center
        ) {
            AssetIcon(
                "ui/icon_farm_chest.png",
                if (hasRewards) "Claim farm rewards" else "Farm chest",
                modifier = Modifier.size(64.dp)
            )
            if (badge > 0) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(HeroGold)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        if (badge > 99) "99+" else badge.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ButtonTextOnAccent
                    )
                }
            }
        }
        Text(
            if (hasRewards) "Tap chest to claim" else "Loot fills while you play",
            fontSize = 10.sp,
            color = if (hasRewards) HeroGold else TextMuted,
            fontWeight = if (hasRewards) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FarmClaimModal(vm: GameViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rewards = vm.lastClaimedFarmRewards

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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Farm Rewards", fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Collected from your active farm while you were away.", fontSize = 12.sp, color = TextMuted)
            FarmRewardLines(vm, rewards)
            GameButton(text = "Collect", onClick = onDismiss)
        }
    }
}

@Composable
private fun FarmRewardLines(vm: GameViewModel, rewards: PendingFarmRewards) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (rewards.crowns > 0) {
            FarmRewardLine("Crowns", "+${rewards.crowns}")
        }
        rewards.materials.forEach { (id, qty) ->
            val name = vm.content.material(id)?.name ?: id
            FarmRewardLine(name, "+$qty")
        }
        rewards.heroes.forEach { (id, qty) ->
            val name = vm.content.hero(id)?.name ?: id
            FarmRewardLine(name, "+$qty")
        }
        rewards.gear.forEach { (id, qty) ->
            val name = vm.content.gear(id)?.name ?: id
            FarmRewardLine(name, "+$qty")
        }
        rewards.packs.forEach { (id, qty) ->
            val name = vm.content.pack(id)?.name ?: id
            FarmRewardLine(name, "+$qty")
        }
    }
}

@Composable
private fun FarmRewardLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextPrimary, fontWeight = FontWeight.Medium)
        Text(value, color = HeroGold, fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuestModal(vm: GameViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Quests", fontWeight = FontWeight.Bold, color = TextPrimary)
            vm.content.quests.forEach { quest ->
                val progress = vm.questProgress(quest)
                val claimed = quest.id in vm.player.questClaimed
                val complete = progress >= quest.progressTarget
                Card(colors = CardDefaults.cardColors(containerColor = MintBgDeep)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(quest.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(quest.description, fontSize = 12.sp, color = TextMuted)
                        Text("Progress: $progress / ${quest.progressTarget}", fontSize = 12.sp, color = TextPrimary)
                        val packName = vm.content.pack(quest.rewardPackId)?.name ?: quest.rewardPackId
                        Text("Reward: $packName", fontSize = 11.sp, color = HeroGold)
                        GameButton(
                            text = when {
                                claimed -> "Claimed"
                                complete -> "Claim"
                                else -> "In progress"
                            },
                            onClick = { vm.claimQuest(quest.id) },
                            enabled = complete && !claimed
                        )
                    }
                }
            }
        }
    }
}

/** Battle arena positions — x/y as fractions; fighters anchored bottom-center in slot. */
private val allySlots = listOf(
    Pair(0.06f, 0.38f),
    Pair(0.06f, 0.58f),
    Pair(0.22f, 0.48f),
    Pair(0.22f, 0.68f),
    Pair(0.38f, 0.58f),
)

private val enemySlots = listOf(
    Pair(0.94f, 0.38f),
    Pair(0.94f, 0.58f),
    Pair(0.78f, 0.48f),
    Pair(0.78f, 0.68f),
    Pair(0.62f, 0.58f),
)

private val fighterSpriteWidth = 120.dp
private val fighterSpriteHeight = 150.dp
private val fighterColumnWidth = 108.dp

@Composable
fun BattleOverlay(vm: GameViewModel) {
    val battle = vm.battle ?: return
    var attackPulse by remember { mutableIntStateOf(0) }
    val attackerName = battle.activeAttackerName

    LaunchedEffect(battle.stageName) {
        while (vm.battle?.finished == false) {
            delay(650)
            vm.advanceBattle()
            attackPulse++
        }
    }

    Box(Modifier.fillMaxSize()) {
        AsyncImage(
            model = "file:///android_asset/battle/arena_whispering_woods.png",
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.92f),
            contentScale = ContentScale.Crop
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFFD4C4A8).copy(alpha = 0.35f),
                            Color(0xFFB8A078).copy(alpha = 0.45f),
                            Color(0xFF9A8468).copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3B2F2F).copy(alpha = 0.72f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    battle.stageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceMint
                )
                Text(
                    battle.logLine,
                    color = HeroGold,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }

            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                val w = maxWidth
                val h = maxHeight
                battle.allies.forEachIndexed { i, unit ->
                    if (i < allySlots.size && unit.hp > 0) {
                        val (xf, yf) = allySlots[i]
                        BattleFighter(
                            unit = unit,
                            rigManifest = vm.content.rigManifest,
                            isEnemy = false,
                            attackPulse = attackPulse,
                            isAttacking = unit.name == attackerName && !battle.finished,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (w * xf - fighterColumnWidth / 2).coerceAtLeast(0.dp),
                                    y = h * yf
                                )
                        )
                    }
                }
                battle.enemies.forEachIndexed { i, unit ->
                    if (i < enemySlots.size && unit.hp > 0) {
                        val (xf, yf) = enemySlots[i]
                        BattleFighter(
                            unit = unit,
                            rigManifest = vm.content.rigManifest,
                            isEnemy = true,
                            attackPulse = attackPulse,
                            isAttacking = unit.name == attackerName && !battle.finished,
                            flipHorizontal = true,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(
                                    x = (w * xf - fighterColumnWidth / 2).coerceAtMost(w - fighterColumnWidth),
                                    y = h * yf
                                )
                        )
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3B2F2F).copy(alpha = 0.85f))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (battle.finished) {
                    if (battle.victory) {
                        Text(
                            "Victory! +${battle.crownRewardPending} crowns",
                            fontWeight = FontWeight.Bold,
                            color = HeroGold
                        )
                        if (battle.materialRewardPending.isNotEmpty()) {
                            battle.materialRewardPending.forEach { (id, qty) ->
                                val name = vm.content.material(id)?.name ?: id
                                Text("+$qty $name", fontSize = 12.sp, color = SurfaceMint)
                            }
                        }
                    } else {
                        Text("Defeat", fontWeight = FontWeight.Bold, color = Color(0xFFFF8A8A))
                    }
                    GameButton("Continue", onClick = { vm.closeBattle(battle.victory) })
                } else {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = HeroGold,
                        strokeWidth = 3.dp
                    )
                    Text("Auto battling…", color = SurfaceMint, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun BattleFighter(
    unit: BattleUnit,
    rigManifest: RigManifest,
    isEnemy: Boolean,
    attackPulse: Int = 0,
    isAttacking: Boolean = false,
    flipHorizontal: Boolean = false,
    modifier: Modifier = Modifier
) {
    val alive = unit.hp > 0
    val hpFrac = unit.hp.toFloat() / unit.maxHp.coerceAtLeast(1)

    Column(
        modifier = modifier.width(fighterColumnWidth).alpha(if (alive) 1f else 0.35f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .width(fighterSpriteWidth)
                .height(fighterSpriteHeight)
        ) {
            if (!unit.battleRigId.isNullOrBlank()) {
                BattleFighterSprite(
                    rigId = unit.battleRigId,
                    rigManifest = rigManifest,
                    attackPulse = attackPulse,
                    isAttacking = isAttacking,
                    attackStyle = unit.attackStyle,
                    mainHandArt = unit.mainHandArt,
                    offHandArt = unit.offHandArt,
                    flipHorizontal = flipHorizontal,
                    modifier = Modifier
                        .size(fighterSpriteWidth)
                        .align(Alignment.BottomCenter)
                )
            } else if (unit.artAsset.isNotBlank()) {
                AsyncImage(
                    model = "file:///android_asset/${unit.artAsset}",
                    contentDescription = unit.name,
                    modifier = Modifier
                        .width(fighterSpriteWidth)
                        .height(fighterSpriteHeight),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF3B2F2F).copy(alpha = 0.92f))
                .border(1.dp, HeroGold.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                unit.name,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = SurfaceMint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            LinearProgressIndicator(
                progress = { hpFrac },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 3.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isEnemy) GearOrange else HeroTeal,
                trackColor = Color(0xFF5C4A4A)
            )
            Text(
                "${unit.hp}/${unit.maxHp}",
                fontSize = 8.sp,
                color = Color(0xFFB08D57),
                modifier = Modifier.padding(top = 1.dp)
            )
        }
    }
}
