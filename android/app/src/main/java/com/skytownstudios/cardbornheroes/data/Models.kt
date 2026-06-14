package com.skytownstudios.cardbornheroes.data

data class HeroStats(val hp: Int, val atk: Int, val def: Int)

data class GearBonus(val atk: Int, val hp: Int, val def: Int)

data class HeroDef(
    val id: String,
    val name: String,
    val role: String,
    val tier: String,
    val art: String,
    val battleArt: String,
    val stats: HeroStats
)

data class GearDef(
    val id: String,
    val name: String,
    val tier: String,
    val art: String,
    val bonus: GearBonus,
    val compatibleRoles: List<String>
)

data class MaterialDef(val id: String, val name: String, val art: String)

data class PackDef(
    val id: String,
    val name: String,
    val packLine: String,
    val tier: String,
    val currency: String,
    val cost: Int
)

data class RecipeMaterial(val id: String, val qty: Int)

data class RecipeDef(
    val id: String,
    val name: String,
    val resultType: String,
    val resultId: String,
    val materials: List<RecipeMaterial>
)

data class QuestDef(
    val id: String,
    val title: String,
    val description: String,
    val rewardPackId: String,
    val progressTarget: Int,
    val type: String
)

data class CampaignStage(
    val id: String,
    val name: String,
    val recommendedPower: Int,
    val crownReward: Int
)

data class CampaignDef(val id: String, val name: String, val stages: List<CampaignStage>)

data class FarmDrop(val type: String, val tier: String, val ratePerHour: Double)

data class FarmPackDrop(val packId: String, val ratePerHour: Double)

data class FarmArea(
    val id: String,
    val name: String,
    val description: String,
    val baseCrownsPerHour: Int,
    val primaryMaterial: String,
    val primaryQtyPerHour: Double,
    val secondaryDrop: FarmDrop?,
    val rarePackDrop: FarmPackDrop?
)

data class HandSlot(val type: String = "", val cardId: String = "") {
    val isEmpty: Boolean get() = cardId.isEmpty()
}

data class Hand(val slots: List<HandSlot> = List(5) { HandSlot() })

data class PlayerLifetimeStats(
    val wins: Int = 0,
    val losses: Int = 0,
    val crownsEarned: Int = 0,
    val packsOpened: Int = 0,
    val craftsDone: Int = 0,
    val questsClaimed: Int = 0
)

data class PlayerState(
    val crowns: Int = 1000,
    val sigils: Int = 100,
    val materials: Map<String, Int> = mapOf(
        "mat_hero_essence" to 20,
        "mat_steel" to 15,
        "mat_arcane_dust" to 10
    ),
    val packInventory: Map<String, Int> = mapOf("ascendant_basic" to 1),
    val heroCounts: Map<String, Int> = mapOf(
        "hero_knight" to 1,
        "hero_archer" to 1
    ),
    val gearCounts: Map<String, Int> = mapOf("gear_sword" to 1),
    val hands: List<Hand> = List(3) { Hand() },
    val activeHandIndex: Int = 0,
    val campaignStageIndex: Int = 0,
    val activeFarmId: String = "goblin_hills",
    val lastFarmTickEpochMs: Long = System.currentTimeMillis(),
    val questProgress: Map<String, Int> = emptyMap(),
    val questClaimed: Set<String> = emptySet(),
    val discoveredHeroes: Set<String> = setOf("hero_knight", "hero_archer"),
    val discoveredGear: Set<String> = setOf("gear_sword"),
    val discoveredRecipes: Set<String> = emptySet(),
    val stats: PlayerLifetimeStats = PlayerLifetimeStats(),
    val farmCrownsRemainder: Double = 0.0,
    val farmMaterialRemainder: Double = 0.0
)

data class PackRevealCard(
    val type: String,
    val id: String,
    val name: String,
    val art: String,
    val tier: String,
    val isGuaranteedSlot: Boolean,
    val qty: Int = 1
)

data class BattleUnit(
    val name: String,
    val artAsset: String,
    val hp: Int,
    val maxHp: Int,
    val atk: Int,
    val def: Int
)

data class BattleState(
    val stageName: String,
    val crownReward: Int,
    val allies: List<BattleUnit>,
    val enemies: List<BattleUnit>,
    val logLine: String = "Battle begins!",
    val finished: Boolean = false,
    val victory: Boolean = false,
    val crownRewardPending: Int = 0
)
