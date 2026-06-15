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
import com.skytownstudios.cardbornheroes.data.HeroInventory
import com.skytownstudios.cardbornheroes.data.HeroStack
import com.skytownstudios.cardbornheroes.data.RecipeDef
import com.skytownstudios.cardbornheroes.ui.GameViewModel
import com.skytownstudios.cardbornheroes.ui.components.CardArt
import com.skytownstudios.cardbornheroes.ui.components.GameButton
import com.skytownstudios.cardbornheroes.ui.components.GamePanel
import com.skytownstudios.cardbornheroes.ui.theme.*

@Composable
fun ForgeScreen(vm: GameViewModel) {
    val craftRecipes = vm.content.recipes.filter { it.recipeType == "craft" }
    val promotionRecipes = vm.content.recipes.filter { it.recipeType == "promotion" }
    val fusionCandidates = vm.fusionCandidates()

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Forge", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 20.sp)
            Text(
                "Craft recruits & gear, promote units into Heroes, or fuse star ranks.",
                fontSize = 12.sp,
                color = TextMuted
            )
        }

        if (fusionCandidates.isNotEmpty()) {
            item {
                SectionHeader("Star Fusion", "Combine 3 of the same unit at the same star rank.")
            }
            items(fusionCandidates, key = { "${it.heroId}@${it.stars}" }) { stack ->
                FusionCard(vm, stack)
            }
        }

        if (promotionRecipes.isNotEmpty()) {
            item {
                SectionHeader("Promotions", "Sacrifice recruits to forge true Heroes.")
            }
            items(promotionRecipes, key = { it.id }) { recipe ->
                RecipeCard(vm, recipe)
            }
        }

        if (craftRecipes.isNotEmpty()) {
            item {
                SectionHeader("Craft", "Materials into recruits and gear.")
            }
            items(craftRecipes, key = { it.id }) { recipe ->
                RecipeCard(vm, recipe)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 16.sp)
        Text(subtitle, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
private fun FusionCard(vm: GameViewModel, stack: HeroStack) {
    val hero = vm.content.hero(stack.heroId) ?: return
    val nextStars = stack.stars + 1
    val canFuse = vm.canFuseHero(stack.heroId, stack.stars)
    val label = heroDisplayName(hero.name, stack.stars)

    GamePanel(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardArt(
                artPath = hero.art,
                name = label,
                tier = hero.tier,
                modifier = Modifier.size(88.dp)
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(
                    "3× → 1× ${heroDisplayName(hero.name, nextStars)}",
                    fontSize = 12.sp,
                    color = TextMuted
                )
                Text("Available: ${vm.availableHeroCount(stack.heroId, stack.stars)}", fontSize = 12.sp, color = TextPrimary)
                GameButton(
                    text = if (canFuse) "Fuse to ★$nextStars" else "Need 3 copies",
                    onClick = { vm.fuseHeroStack(stack.heroId, stack.stars) },
                    enabled = canFuse,
                    modifier = Modifier.fillMaxWidth()
                )
            }
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
    val resultHero = if (recipe.resultType == "hero") vm.content.hero(recipe.resultId) else null
    val resultName = when (recipe.resultType) {
        "hero" -> resultHero?.name?.let { heroDisplayName(it, recipe.resultStars) } ?: recipe.resultId
        "gear" -> vm.content.gear(recipe.resultId)?.name ?: recipe.resultId
        else -> recipe.name
    }
    val tier = when (recipe.resultType) {
        "hero" -> resultHero?.tier ?: "basic"
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
                if (recipe.heroInputs.isNotEmpty()) {
                    recipe.heroInputs.forEach { input ->
                        val hero = vm.content.hero(input.heroId)
                        val name = hero?.name ?: input.heroId
                        val owned = vm.availableHeroCount(input.heroId, input.stars)
                        val label = heroDisplayName(name, input.stars)
                        Text(
                            "$label: $owned / ${input.qty}",
                            fontSize = 12.sp,
                            color = if (owned >= input.qty) TextPrimary else TextMuted
                        )
                    }
                }
                recipe.materials.forEach { mat ->
                    val owned = vm.player.materials[mat.id] ?: 0
                    val matName = vm.content.material(mat.id)?.name ?: mat.id
                    Text(
                        "$matName: $owned / ${mat.qty}",
                        fontSize = 12.sp,
                        color = if (owned >= mat.qty) TextPrimary else TextMuted
                    )
                }
                val buttonLabel = when {
                    canCraft -> if (recipe.recipeType == "promotion") "Promote" else "Craft"
                    recipe.heroInputs.isNotEmpty() -> "Need units"
                    else -> "Need materials"
                }
                GameButton(
                    text = buttonLabel,
                    onClick = { vm.craftRecipe(recipe.id) },
                    enabled = canCraft,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private fun heroDisplayName(name: String, stars: Int): String =
    if (stars > 0) "$name ★$stars" else name
