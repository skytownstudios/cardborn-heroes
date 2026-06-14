package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.skytownstudios.cardbornheroes.ui.theme.ButtonTextOnAccent
import com.skytownstudios.cardbornheroes.ui.theme.HeroGold
import com.skytownstudios.cardbornheroes.ui.theme.TextPrimary

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HeroGold,
            contentColor = ButtonTextOnAccent,
            disabledContainerColor = HeroGold.copy(alpha = 0.4f),
            disabledContentColor = TextPrimary.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 20.dp)
    ) {
        Text(text, fontWeight = FontWeight.Bold)
    }
}
