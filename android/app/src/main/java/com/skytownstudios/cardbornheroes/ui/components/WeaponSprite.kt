package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

/** Full-canvas weapon PNG; parent arm group supplies rotation pivot. */
@Composable
fun WeaponSprite(artPath: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = "file:///android_asset/$artPath",
        contentDescription = null,
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}
