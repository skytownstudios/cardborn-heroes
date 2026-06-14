package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
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
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.GameButton
import com.skytownstudios.cardbornheroes.ui.components.MiniCardArt
import com.skytownstudios.cardbornheroes.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun BattleScreen(vm: GameViewModel) {
    Box(Modifier.fillMaxSize()) {
        BattleHub(vm)
        if (vm.showQuests) QuestModal(vm) { vm.showQuests = false }
    }
}

@Composable
private fun BattleHub(vm: GameViewModel) {
    val player = vm.player
    val farm = vm.content.farm(player.activeFarmId)
    val stages = vm.content.campaign.stages
    val scroll = rememberScrollState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            Text("Campaign", fontWeight = FontWeight.Bold, color = TextPrimary)
            IconButton(
                onClick = { vm.showQuests = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.MenuBook, contentDescription = "Quests", tint = HeroGold)
            }
        }

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            stages.forEachIndexed { index, stage ->
                val cleared = index < player.campaignStageIndex
                val current = index == player.campaignStageIndex
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    cleared -> HeroGold
                                    current -> HeroGold.copy(alpha = 0.5f)
                                    else -> MintBgDeep
                                }
                            )
                            .border(2.dp, HeroCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            fontWeight = FontWeight.Bold,
                            color = if (cleared || current) ButtonTextOnAccent else TextMuted
                        )
                    }
                    Text(
                        stage.name,
                        fontSize = 10.sp,
                        color = if (current) TextPrimary else TextMuted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(72.dp)
                    )
                }
                if (index < stages.lastIndex) {
                    Box(
                        Modifier
                            .padding(top = 24.dp)
                            .width(24.dp)
                            .height(3.dp)
                            .background(if (cleared) HeroGold else SurfaceBorder.copy(alpha = 0.4f))
                    )
                }
            }
        }

        val currentStage = stages.getOrNull(player.campaignStageIndex)
        if (currentStage != null) {
            Text(
                "Next: ${currentStage.name} · Rec. PWR ${currentStage.recommendedPower} · +${currentStage.crownReward} Crowns",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        Text("Farm Area", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vm.content.farmAreas.forEach { f ->
                val selected = f.id == player.activeFarmId
                FilterChip(
                    selected = selected,
                    onClick = { vm.selectFarm(f.id) },
                    label = { Text(f.name, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = HeroGold,
                        selectedLabelColor = ButtonTextOnAccent
                    )
                )
            }
        }

        farm?.let { f ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MintBgDeep),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(f.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text(f.description, fontSize = 12.sp, color = TextMuted)
                    Text("👑 ${vm.effectiveCrownsPerHour()} Crowns/hr", fontWeight = FontWeight.SemiBold, color = HeroGold)
                    Text("Primary: ${vm.content.material(f.primaryMaterial)?.name ?: f.primaryMaterial}", fontSize = 11.sp, color = TextMuted)
                    f.secondaryDrop?.let {
                        Text("Bonus drops scale with Hand power", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }
        }

        Text("Active Hand", fontWeight = FontWeight.SemiBold, color = TextPrimary)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            vm.activeHand.slots.forEach { slot ->
                val art = when (slot.type) {
                    "hero" -> vm.content.hero(slot.cardId)?.art
                    "gear" -> vm.content.gear(slot.cardId)?.art
                    else -> null
                }
                MiniCardArt(
                    artPath = art,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(0.75f)
                )
            }
        }

        GameButton(
            text = "Begin",
            onClick = { vm.beginBattle() },
            enabled = vm.activeHand.slots.any { !it.isEmpty } && currentStage != null
        )
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

/** Staggered V slots — allies left, enemies mirror right. */
private val allySlots = listOf(
    Pair(0.06f, 0.04f),
    Pair(0.14f, 0.30f),
    Pair(0.06f, 0.58f),
    Pair(0.28f, 0.12f),
    Pair(0.28f, 0.46f),
)

private val enemySlots = listOf(
    Pair(0.94f, 0.04f),
    Pair(0.86f, 0.30f),
    Pair(0.94f, 0.58f),
    Pair(0.72f, 0.12f),
    Pair(0.72f, 0.46f),
)

@Composable
fun BattleOverlay(vm: GameViewModel) {
    val battle = vm.battle ?: return

    LaunchedEffect(battle.stageName) {
        while (vm.battle?.finished == false) {
            delay(750)
            vm.advanceBattle()
        }
    }

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD4C4A8), Color(0xFFB8A078), Color(0xFF9A8468))
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
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                val w = maxWidth
                val h = maxHeight
                battle.allies.forEachIndexed { i, unit ->
                    if (i < allySlots.size) {
                        val (xf, yf) = allySlots[i]
                        BattleFighter(
                            unit = unit,
                            isEnemy = false,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = w * xf - 44.dp, y = h * yf)
                        )
                    }
                }
                battle.enemies.forEachIndexed { i, unit ->
                    if (i < enemySlots.size) {
                        val (xf, yf) = enemySlots[i]
                        BattleFighter(
                            unit = unit,
                            isEnemy = true,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = w * xf - 44.dp, y = h * yf)
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
                    Text(
                        if (battle.victory) "Victory! +${battle.crownRewardPending} Crowns" else "Defeat",
                        fontWeight = FontWeight.Bold,
                        color = if (battle.victory) HeroGold else Color(0xFFFF8A8A)
                    )
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
private fun BattleFighter(unit: BattleUnit, isEnemy: Boolean, modifier: Modifier = Modifier) {
    val alive = unit.hp > 0
    val hpFrac = unit.hp.toFloat() / unit.maxHp.coerceAtLeast(1)
    val platformGlow = if (isEnemy) {
        Brush.radialGradient(listOf(Color(0xFFE07A2F).copy(alpha = 0.55f), Color.Transparent))
    } else {
        Brush.radialGradient(listOf(Color(0xFF6B4CFF).copy(alpha = 0.45f), Color(0xFF0891B2).copy(alpha = 0.2f), Color.Transparent))
    }

    Column(
        modifier = modifier.width(88.dp).alpha(if (alive) 1f else 0.4f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(platformGlow)
            )
            AsyncImage(
                model = "file:///android_asset/${unit.artAsset}",
                contentDescription = unit.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF3B2F2F).copy(alpha = 0.88f))
                .border(1.dp, HeroGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(if (isEnemy) GearOrange else HeroTeal)
                )
                Text(
                    unit.name,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = SurfaceMint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
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
