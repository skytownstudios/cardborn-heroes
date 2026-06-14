package com.skytownstudios.cardbornheroes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.theme.HeroGold
import com.skytownstudios.cardbornheroes.ui.theme.MintBgDeep
import com.skytownstudios.cardbornheroes.ui.theme.SurfaceBorder
import com.skytownstudios.cardbornheroes.ui.theme.TextMuted
import com.skytownstudios.cardbornheroes.ui.theme.TextPrimary

@Composable
fun TopBar(vm: GameViewModel) {
    val player = vm.player
    val power = vm.activeHandPower

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(com.skytownstudios.cardbornheroes.ui.theme.SurfaceMint)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.3f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.clickable { vm.showProfile = true }
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MintBgDeep)
                    .border(2.dp, HeroGold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                AssetIcon("ui/icon_profile.png", "Profile", size = 28.dp)
            }
            Column {
                Text("Hero", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("PWR $power", fontSize = 11.sp, color = TextMuted)
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            CurrencyBadge("ui/currency_crowns.png", player.crowns.toString())
            CurrencyBadge("ui/currency_sigils.png", player.sigils.toString())
            IconButton(onClick = { vm.showVault = true }, modifier = Modifier.size(36.dp)) {
                AssetIcon("ui/icon_bag.png", "Vault", size = 28.dp)
            }
        }
    }
}

@Composable
private fun CurrencyBadge(iconAsset: String, amount: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MintBgDeep)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AssetIcon(iconAsset, null, size = 18.dp)
        Text(amount, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = TextPrimary)
    }
}
