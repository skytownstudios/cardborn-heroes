package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil.compose.AsyncImage

/** Renders asset art at native aspect ratio — never stretched or squashed. */
@Composable
fun AssetIcon(
    assetPath: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp? = null
) {
    val boxModifier = if (size != null) modifier.size(size) else modifier
    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
        AsyncImage(
            model = "file:///android_asset/$assetPath",
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
