package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

@Composable
fun AssetIcon(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp? = null
) {
    val mod = if (size != null) modifier.size(size) else modifier
    AsyncImage(
        model = "file:///android_asset/$assetPath",
        contentDescription = contentDescription,
        modifier = mod,
        contentScale = ContentScale.Fit
    )
}
