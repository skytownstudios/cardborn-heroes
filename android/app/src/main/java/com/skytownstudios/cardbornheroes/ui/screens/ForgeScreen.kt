package com.skytownstudios.cardbornheroes.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skytownstudios.cardbornheroes.data.RecipeDef
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.components.GameButton
import com.skytownstudios.cardbornheroes.ui.components.GamePanel
import com.skytownstudios.cardbornheroes.ui.theme.*

@Composable
fun ForgeScreen(vm: GameViewModel) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Forge", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
            Text("Craft heroes and gear from materials.", fontSize = 12.sp, color = TextMuted)
        }
        items(vm.content.recipes) { recipe ->
            RecipeCard(vm, recipe)
        }
    }
}

@Composable
private fun RecipeCard(vm: GameViewModel, recipe: RecipeDef) {
    val canCraft = vm.canCraft(recipe)
    val resultArt = when (recipe.resultType) {
        "hero" -> vm.content.hero(recipe.resultId)?.art
        "gear" -> vm.content.gear(recipe.resultId)?.art
        else -> null
    }
    val resultName = when (recipe.resultType) {
        "hero" -> vm.content.hero(recipe.resultId)?.name ?: recipe.resultId
        "gear" -> vm.content.gear(recipe.resultId)?.name ?: recipe.resultId
        else -> recipe.name
    }
    val tier = when (recipe.resultType) {
        "hero" -> vm.content.hero(recipe.resultId)?.tier ?: "basic"
        "gear" -> vm.content.gear(recipe.resultId)?.tier ?: "basic"
        else -> "basic"
    }

    GamePanel(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardArt(
                artPath = resultArt,
                name = resultName,
                tier = tier,
                modifier = Modifier.size(88.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(recipe.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                recipe.materials.forEach { mat ->
                    val owned = vm.player.materials[mat.id] ?: 0
                    val matName = vm.content.material(mat.id)?.name ?: mat.id
                    Text(
                        "$matName: $owned / ${mat.qty}",
                        fontSize = 12.sp,
                        color = if (owned >= mat.qty) TextPrimary else TextMuted
                    )
                }
                GameButton(
                    text = if (canCraft) "Craft" else "Need materials",
                    onClick = { vm.craftRecipe(recipe.id) },
                    enabled = canCraft,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
