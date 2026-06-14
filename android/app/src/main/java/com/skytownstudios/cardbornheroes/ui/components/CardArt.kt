package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.skytownstudios.cardbornheroes.ui.theme.HeroCardBorder
import com.skytownstudios.cardbornheroes.ui.theme.MintBgDeep
import com.skytownstudios.cardbornheroes.ui.theme.SurfaceMint
import com.skytownstudios.cardbornheroes.ui.theme.TextMuted

@Composable
fun CardArt(
    artPath: String?,
    name: String,
    modifier: Modifier = Modifier,
    discovered: Boolean = true,
    tier: String = "basic",
    qty: Int? = null
) {
    val borderColor = tierColor(tier)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (discovered) SurfaceMint else MintBgDeep)
            .border(2.dp, borderColor, RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (discovered && !artPath.isNullOrBlank()) {
            AsyncImage(
                model = "file:///android_asset/$artPath",
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(1f),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMuted)
        }
        if (qty != null && qty > 1) {
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .background(borderColor.copy(alpha = 0.85f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text("×$qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = SurfaceMint)
            }
        }
    }
}

@Composable
fun MiniCardArt(artPath: String?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MintBgDeep)
            .border(1.dp, HeroCardBorder, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!artPath.isNullOrBlank()) {
            AsyncImage(
                model = "file:///android_asset/$artPath",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("—", fontSize = 12.sp, textAlign = TextAlign.Center, color = TextMuted)
        }
    }
}

private fun tierColor(tier: String) = when (tier) {
    "common" -> HeroCardBorder.copy(alpha = 0.7f)
    "uncommon" -> androidx.compose.ui.graphics.Color(0xFF6B8E4E)
    "rare" -> androidx.compose.ui.graphics.Color(0xFF4A7EB8)
    "epic" -> androidx.compose.ui.graphics.Color(0xFF8B4CB8)
    "mythic" -> androidx.compose.ui.graphics.Color(0xFFB84C4C)
    "cardborn" -> androidx.compose.ui.graphics.Color(0xFFD4AF37)
    else -> HeroCardBorder
}
