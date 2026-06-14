package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytownstudios.cardbornheroes.data.AscendantPackOdds
import com.skytownstudios.cardbornheroes.data.PackRevealCard
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.components.GameButton
import com.skytownstudios.cardbornheroes.ui.theme.*

@Composable
fun ShopScreen(vm: GameViewModel) {
    var subTab by remember { mutableIntStateOf(0) }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text("Shop", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
            TabRow(selectedTabIndex = subTab) {
                Tab(selected = subTab == 0, onClick = { subTab = 0 }, text = { Text("Buy") })
                Tab(selected = subTab == 1, onClick = { subTab = 1 }, text = { Text("Open Packs") })
                Tab(selected = subTab == 2, onClick = { subTab = 2 }, text = { Text("Premium") })
            }
            Spacer(Modifier.height(8.dp))
            when (subTab) {
                0 -> BuyTab(vm)
                1 -> OpenPacksTab(vm)
                2 -> PremiumTab()
            }
        }

        vm.packRevealCards?.let { cards ->
            PackRevealOverlay(vm, cards)
        }
    }
}

@Composable
private fun BuyTab(vm: GameViewModel) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(vm.content.packs) { pack ->
            val currencyLabel = if (pack.currency == "crowns") "Crowns" else "Sigils"
            val balance = if (pack.currency == "crowns") vm.player.crowns else vm.player.sigils
            val canBuy = balance >= pack.cost
            Card(colors = CardDefaults.cardColors(containerColor = MintBgDeep)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(pack.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${pack.cost} $currencyLabel", color = HeroGold, fontWeight = FontWeight.SemiBold)
                    Text(AscendantPackOdds.summary(pack.tier), fontSize = 11.sp, color = TextMuted)
                    GameButton(
                        text = if (canBuy) "Buy Pack" else "Not enough $currencyLabel",
                        onClick = { vm.buyPack(pack.id) },
                        enabled = canBuy
                    )
                }
            }
        }
    }
}

@Composable
private fun OpenPacksTab(vm: GameViewModel) {
    val packs = vm.player.packInventory.filter { it.value > 0 }
    if (packs.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No sealed packs. Buy some or earn from quests!", color = TextMuted)
        }
        return
    }
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(packs.entries.toList()) { (packId, qty) ->
            val pack = vm.content.pack(packId)
            Card(colors = CardDefaults.cardColors(containerColor = MintBgDeep)) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(pack?.name ?: packId, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Owned: ×$qty", color = TextMuted)
                    GameButton("Open Pack", onClick = { vm.startOpenPack(packId) })
                }
            }
        }
    }
}

@Composable
private fun PremiumTab() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PremiumCard("Remove Ads", "One-time purchase — coming soon")
        PremiumCard("Premium Monthly", "Bonus Sigils & perks — coming soon")
        PremiumCard("Premium Yearly", "Best value — coming soon")
    }
}

@Composable
private fun PremiumCard(title: String, subtitle: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MintBgDeep), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 12.sp, color = TextMuted)
        }
    }
}

@Composable
private fun PackRevealOverlay(vm: GameViewModel, cards: List<PackRevealCard>) {
    val index = vm.packRevealIndex
    val card = cards[index]

    Box(
        Modifier
            .fillMaxSize()
            .background(TextPrimary.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Card ${index + 1} of ${cards.size}",
                color = SurfaceMint,
                fontWeight = FontWeight.Bold
            )
            if (card.isGuaranteedSlot) {
                Text("★ Guaranteed Slot", color = HeroGold, fontSize = 12.sp)
            }
            CardArt(
                artPath = if (card.type == "material") null else card.art,
                name = card.name,
                tier = card.tier,
                qty = if (card.type == "material") card.qty else null,
                modifier = Modifier
                    .size(200.dp)
                    .background(SurfaceMint, RoundedCornerShape(12.dp))
            )
            Text(card.name, fontWeight = FontWeight.Bold, color = SurfaceMint, fontSize = 18.sp)
            if (card.type == "material") {
                Text("×${card.qty} ${card.name}", color = HeroGold)
            }
            Text(card.tier.replaceFirstChar { it.uppercase() }, color = TextMuted, fontSize = 12.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (index < cards.lastIndex && index < 7) {
                    OutlinedButton(onClick = { vm.skipToGuaranteed() }) {
                        Text("Skip to #9", color = SurfaceMint)
                    }
                }
                GameButton(
                    text = if (index < cards.lastIndex) "Next Card" else "Finish",
                    onClick = {
                        if (index < cards.lastIndex) vm.revealNextCard() else vm.finishPackReveal()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
