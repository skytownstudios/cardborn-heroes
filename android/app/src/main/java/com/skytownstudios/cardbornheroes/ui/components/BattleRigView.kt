package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.skytownstudios.cardbornheroes.data.RigManifest
import kotlinx.coroutines.delay

private val rigPartRoles = setOf("knight")

@Composable
fun BattleFighterSprite(
    rigId: String,
    rigManifest: RigManifest,
    modifier: Modifier = Modifier,
    attackPulse: Int = 0,
    isAttacking: Boolean = false,
    attackStyle: String = "unarmed",
    mainHandArt: String? = null,
    offHandArt: String? = null,
    flipHorizontal: Boolean = false
) {
    if (rigId in rigPartRoles) {
        BattleRigView(
            rigId = rigId,
            rigManifest = rigManifest,
            modifier = modifier,
            attackPulse = attackPulse,
            isAttacking = isAttacking,
            attackStyle = attackStyle,
            flipHorizontal = flipHorizontal
        )
    } else {
        PortraitBattleView(
            rigId = rigId,
            attackPulse = attackPulse,
            isAttacking = isAttacking,
            flipHorizontal = flipHorizontal,
            modifier = modifier
        )
    }
}

@Composable
fun BattleRigView(
    rigId: String,
    rigManifest: RigManifest,
    modifier: Modifier = Modifier,
    attackPulse: Int = 0,
    isAttacking: Boolean = false,
    attackStyle: String = "unarmed",
    flipHorizontal: Boolean = false
) {
    val roleDef = rigManifest.role(rigId) ?: return
    var swingPhase by remember { mutableStateOf(false) }

    LaunchedEffect(attackPulse, isAttacking) {
        if (attackPulse <= 0 || !isAttacking) return@LaunchedEffect
        swingPhase = true
        delay(480)
        swingPhase = false
    }

    val anim = when (attackStyle) {
        "melee_1h", "dual_1h", "melee_2h" -> roleDef.melee1hAttack
        else -> roleDef.unarmedAttack
    }

    val swing by animateFloatAsState(
        targetValue = if (swingPhase) anim.rotationSwing else anim.rotationRest,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "rigSwing"
    )
    val lunge by animateFloatAsState(
        targetValue = if (swingPhase) (if (flipHorizontal) -18f else 18f) else 0f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "lunge"
    )

    val flip = if (flipHorizontal) -1f else 1f
    val base = "heroes/$rigId/rig"

    Box(
        modifier = modifier
            .clipToBounds()
            .graphicsLayer {
                scaleX = flip
                translationX = lunge * flip
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(Modifier.fillMaxSize().clipToBounds()) {
            RigLayer("$base/leg_left.png")
            RigLayer("$base/leg_right.png")
            RigLayer("$base/torso.png")
            RigLayer("$base/arm_left.png")
            RigLayer("$base/head.png")

            Box(
                Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .graphicsLayer {
                        rotationZ = swing * flip
                        transformOrigin = TransformOrigin(
                            roleDef.armRightPivot.x,
                            roleDef.armRightPivot.y
                        )
                    }
            ) {
                RigLayer("$base/arm_right.png")
            }
        }
    }
}

@Composable
private fun PortraitBattleView(
    rigId: String,
    modifier: Modifier = Modifier,
    attackPulse: Int = 0,
    isAttacking: Boolean = false,
    flipHorizontal: Boolean = false
) {
    var attackPhase by remember { mutableStateOf(false) }

    LaunchedEffect(attackPulse, isAttacking) {
        if (attackPulse <= 0 || !isAttacking) return@LaunchedEffect
        attackPhase = true
        delay(480)
        attackPhase = false
    }

    val lunge by animateFloatAsState(
        targetValue = if (attackPhase) (if (flipHorizontal) -20f else 20f) else 0f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "portraitLunge"
    )
    val flip = if (flipHorizontal) -1f else 1f

    Box(
        modifier = modifier
            .clipToBounds()
            .graphicsLayer {
                scaleX = flip
                translationX = lunge * flip
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = "file:///android_asset/heroes/$rigId/portrait.png",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
    }
}

@Composable
private fun RigLayer(assetPath: String) {
    AsyncImage(
        model = "file:///android_asset/$assetPath",
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}
