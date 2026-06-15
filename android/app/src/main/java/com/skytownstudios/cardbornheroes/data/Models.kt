package com.skytownstudios.cardbornheroes.data

data class HeroStats(val hp: Int, val atk: Int, val def: Int, val energy: Int = 0)

data class GearBonus(val atk: Int, val hp: Int, val def: Int, val energy: Int = 0)

data class HeroDef(
    val id: String,
    val name: String,
    val role: String,
    val tier: String,
    val unitKind: String,
    val art: String,
    val battleRig: String,
    val stats: HeroStats
) {
    val isUnit: Boolean get() = unitKind == "unit"
    val isHero: Boolean get() = unitKind == "hero"
}

data class RecipeHeroInput(val heroId: String, val stars: Int, val qty: Int)

data class RecipeMaterial(val id: String, val qty: Int)

data class RecipeDef(
    val id: String,
    val name: String,
    val resultType: String,
    val resultId: String,
    val materials: List<RecipeMaterial>,
    val recipeType: String = "craft",
    val heroInputs: List<RecipeHeroInput> = emptyList(),
    val resultStars: Int = 0
)

data class GearDef(
    val id: String,
    val name: String,
    val tier: String,
    val art: String,
    val battleArt: String,
    val hands: Int,
    val hand: String,
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

data class QuestDef(
    val id: String,
    val title: String,
    val description: String,
    val rewardPackId: String,
    val progressTarget: Int,
    val type: String
)

data class CampaignZone(
    val id: String,
    val name: String,
    val description: String,
    val mapArt: String,
    val icon: String,
    val baseRecommendedPower: Int,
    val enemyIds: List<String>,
    val crownRewardMin: Int,
    val crownRewardMax: Int,
    val materialRewards: List<CampaignMaterialReward> = emptyList()
)

/** A campaign zone resolved to a specific level for battle. */
data class CampaignRun(
    val campaignId: String,
    val level: Int,
    val displayName: String,
    val recommendedPower: Int,
    val enemyIds: List<String>,
    val enemyStatMultiplier: Float,
    val crownRewardMin: Int,
    val crownRewardMax: Int,
    val materialRewards: List<CampaignMaterialReward> = emptyList()
)

data class CampaignMaterialReward(
    val id: String,
    val min: Int,
    val max: Int,
    val weight: Double = 1.0
)

data class EnemyDef(
    val id: String,
    val name: String,
    val role: String,
    val art: String,
    val battleRig: String,
    val stats: HeroStats,
    val weakTo: List<String>,
    val strongAgainst: List<String>,
    val counterTip: String
)

data class CampaignDef(val id: String, val name: String, val description: String)

data class FarmDrop(val type: String, val tier: String, val ratePerHour: Double)

data class FarmPackDrop(val packId: String, val ratePerHour: Double)

/** UI row for a farm zone pack drop rate (after Hand power scaling). */
data class FarmPackRateRow(
    val label: String,
    val rate: String,
    val icon: String,
    val packName: String
)

data class FarmArea(
    val id: String,
    val name: String,
    val description: String,
    val baseCrownsPerHour: Int,
    val primaryMaterial: String,
    val primaryQtyPerHour: Double,
    val secondaryDrop: FarmDrop?,
    val packDrops: List<FarmPackDrop> = emptyList()
)

data class HeroLoadout(
    val heroId: String = "",
    val heroStars: Int = 0,
    val mainHandGearId: String = "",
    val offHandGearId: String = ""
) {
    val isEmpty: Boolean get() = heroId.isEmpty()
}

data class Hand(val heroSlots: List<HeroLoadout> = List(5) { HeroLoadout() })

/** Hand 1: Basic Knight only, no gear equipped. */
fun defaultStarterHands(): List<Hand> = listOf(
    Hand(List(5) { i -> if (i == 0) HeroLoadout(heroId = "hero_knight") else HeroLoadout() }),
    Hand(),
    Hand()
)

data class PlayerLifetimeStats(
    val wins: Int = 0,
    val losses: Int = 0,
    val crownsEarned: Int = 0,
    val packsOpened: Int = 0,
    val craftsDone: Int = 0,
    val questsClaimed: Int = 0
)

/** Rewards accumulated while farming — claimed by tapping the farm chest. */
data class PendingFarmRewards(
    val crowns: Int = 0,
    val materials: Map<String, Int> = emptyMap(),
    val heroes: Map<String, Int> = emptyMap(),
    val gear: Map<String, Int> = emptyMap(),
    val packs: Map<String, Int> = emptyMap()
) {
    val isEmpty: Boolean
        get() = crowns <= 0 && materials.isEmpty() && heroes.isEmpty() && gear.isEmpty() && packs.isEmpty()

    fun badgeCount(): Int {
        var n = if (crowns > 0) 1 else 0
        n += materials.values.sum()
        n += heroes.values.sum()
        n += gear.values.sum()
        n += packs.values.sum()
        return n
    }

    companion object {
        fun mergeCounts(base: Map<String, Int>, gains: Map<String, Int>): Map<String, Int> {
            if (gains.isEmpty()) return base
            val merged = base.toMutableMap()
            gains.forEach { (id, qty) -> merged[id] = (merged[id] ?: 0) + qty }
            return merged
        }
    }
}

data class PlayerState(
    val crowns: Int = 2000,
    val sigils: Int = 1000,
    val materials: Map<String, Int> = mapOf(
        "mat_hero_essence" to 2000,
        "mat_steel" to 20,
        "mat_arcane_dust" to 20
    ),
    val packInventory: Map<String, Int> = mapOf("ascendant_basic" to 1),
    val heroCounts: Map<String, Int> = mapOf(
        HeroInventory.stackKey("hero_knight", 0) to 1,
        HeroInventory.stackKey("hero_archer", 0) to 1
    ),
    val gearCounts: Map<String, Int> = mapOf("gear_sword" to 1, "gear_shield" to 1),
    val hands: List<Hand> = defaultStarterHands(),
    val activeHandIndex: Int = 0,
    val activeCampaignId: String = "whispering_woods",
    val activeCampaignLevel: Int = 1,
    val campaignBestLevel: Map<String, Int> = emptyMap(),
    val activeFarmId: String = "goblin_hills",
    val lastFarmTickEpochMs: Long = System.currentTimeMillis(),
    val questProgress: Map<String, Int> = emptyMap(),
    val questClaimed: Set<String> = emptySet(),
    val discoveredHeroes: Set<String> = setOf("hero_knight", "hero_archer"),
    val discoveredGear: Set<String> = setOf("gear_sword", "gear_shield"),
    val discoveredEnemies: Set<String> = emptySet(),
    val discoveredRecipes: Set<String> = emptySet(),
    val stats: PlayerLifetimeStats = PlayerLifetimeStats(),
    val farmCrownsRemainder: Double = 0.0,
    val farmMaterialRemainder: Double = 0.0,
    val pendingFarmRewards: PendingFarmRewards = PendingFarmRewards()
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
    val id: String = "",
    val name: String,
    val artAsset: String = "",
    val battleRigId: String? = null,
    val mainHandArt: String? = null,
    val offHandArt: String? = null,
    val attackStyle: String = "unarmed",
    val role: String = "",
    val hp: Int,
    val maxHp: Int,
    val atk: Int,
    val def: Int,
    val energy: Int = 0
)

data class BattleState(
    val stageName: String,
    val campaignId: String = "",
    val campaignLevel: Int = 1,
    val crownRewardMin: Int,
    val crownRewardMax: Int,
    val materialRewards: List<CampaignMaterialReward>,
    val allies: List<BattleUnit>,
    val enemies: List<BattleUnit>,
    val logLine: String = "Battle begins!",
    val finished: Boolean = false,
    val victory: Boolean = false,
    val crownRewardPending: Int = 0,
    val materialRewardPending: Map<String, Int> = emptyMap(),
    /** "ally" or "enemy" — whose turn is next (one attack per advance). */
    val nextSide: String = "ally",
    /** Name of the unit that just attacked — drives battle animation. */
    val activeAttackerName: String? = null
)
