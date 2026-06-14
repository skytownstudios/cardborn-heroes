package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.skytownstudios.cardbornheroes.ui.theme.*
import kotlinx.coroutines.delay

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
                        if (battle.victory) "Victory! +${battle.goldReward} gold" else "Defeat",
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
