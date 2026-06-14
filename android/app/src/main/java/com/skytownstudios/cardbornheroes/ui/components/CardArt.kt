package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.skytownstudios.cardbornheroes.ui.theme.MintBgDeep
import com.skytownstudios.cardbornheroes.ui.theme.TextMuted

@Composable
fun CardArt(
    artPath: String?,
    name: String,
    modifier: Modifier = Modifier,
    discovered: Boolean = true,
    tier: String = "basic",
    qty: Int? = null,
    framed: Boolean = true
) {
    val inner: @Composable () -> Unit = {
        Box(
            Modifier.fillMaxSize().padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            if (discovered && !artPath.isNullOrBlank()) {
                AsyncImage(
                    model = "file:///android_asset/$artPath",
                    contentDescription = name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                Text("?", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMuted)
            }
            if (qty != null && qty > 1) {
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD4AF37).copy(alpha = 0.9f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                ) {
                    Text("×$qty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3B2F2F))
                }
            }
        }
    }
    if (framed) {
        GameCardFrame(modifier = modifier, content = inner)
    } else {
        Box(modifier, contentAlignment = Alignment.Center) { inner() }
    }
}

@Composable
fun MiniCardArt(artPath: String?, modifier: Modifier = Modifier) {
    GameCardFrame(modifier = modifier) {
        Box(Modifier.fillMaxSize().padding(4.dp), contentAlignment = Alignment.Center) {
            if (!artPath.isNullOrBlank()) {
                AsyncImage(
                    model = "file:///android_asset/$artPath",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = "file:///android_asset/ui/hand_slot_empty.png",
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(0.85f),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
fun TransparentArt(artPath: String, contentDescription: String?, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "file:///android_asset/$artPath",
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}
