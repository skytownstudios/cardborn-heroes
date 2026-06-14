package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.delay

/** Layer order back → front for 2D battle rig animation. */
object BattleRigParts {
    val layerOrder = listOf("legs", "torso", "arm_left", "arm_right", "head", "weapon")

    fun assetPath(rigId: String, part: String) = "heroes/$rigId/rig/$part.png"
}

@Composable
fun BattleRigView(
    rigId: String,
    modifier: Modifier = Modifier,
    attackPulse: Int = 0,
    flipHorizontal: Boolean = false
) {
    var attackPhase by remember { mutableStateOf(false) }

    LaunchedEffect(attackPulse) {
        if (attackPulse <= 0) return@LaunchedEffect
        attackPhase = true
        delay(420)
        attackPhase = false
    }

    val armSlash by animateFloatAsState(
        targetValue = if (attackPhase) -38f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "armSlash"
    )
    val torsoLean by animateFloatAsState(
        targetValue = if (attackPhase) 6f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "torsoLean"
    )
    val headBob by animateFloatAsState(
        targetValue = if (attackPhase) 4f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "headBob"
    )
    val weaponExtra by animateFloatAsState(
        targetValue = if (attackPhase) -12f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "weaponExtra"
    )
    val armSway by animateFloatAsState(
        targetValue = if (attackPhase) 4f else 0f,
        animationSpec = tween(280, easing = FastOutSlowInEasing),
        label = "armSway"
    )

    val flip = if (flipHorizontal) -1f else 1f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        RigLayer(rigId, "legs", flip = flip)
        RigLayer(
            rigId, "torso", flip = flip,
            rotationZ = torsoLean * flip,
            transformOrigin = TransformOrigin(0.5f, 0.65f)
        )
        RigLayer(
            rigId, "arm_left", flip = flip,
            rotationZ = armSway * flip,
            transformOrigin = TransformOrigin(0.62f, 0.38f)
        )
        RigLayer(
            rigId, "arm_right", flip = flip,
            rotationZ = armSlash * flip,
            transformOrigin = TransformOrigin(0.38f, 0.38f)
        )
        RigLayer(
            rigId, "head", flip = flip,
            rotationZ = headBob * flip,
            translationY = if (attackPhase) (-2).dp else 0.dp,
            transformOrigin = TransformOrigin(0.5f, 0.85f)
        )
        RigLayer(
            rigId, "weapon", flip = flip,
            rotationZ = (armSlash + weaponExtra) * flip,
            transformOrigin = TransformOrigin(0.38f, 0.42f)
        )
    }
}

@Composable
private fun RigLayer(
    rigId: String,
    part: String,
    flip: Float = 1f,
    rotationZ: Float = 0f,
    translationY: androidx.compose.ui.unit.Dp = 0.dp,
    transformOrigin: TransformOrigin = TransformOrigin.Center
) {
    AsyncImage(
        model = "file:///android_asset/${BattleRigParts.assetPath(rigId, part)}",
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .offset(y = translationY)
            .graphicsLayer {
                scaleX = flip
                this.rotationZ = rotationZ
                this.transformOrigin = transformOrigin
            },
        contentScale = ContentScale.Fit
    )
}
