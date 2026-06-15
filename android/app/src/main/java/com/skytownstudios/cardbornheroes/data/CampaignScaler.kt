package com.skytownstudios.cardbornheroes.data

import kotlin.math.pow
import kotlin.math.roundToInt

/** Scales a campaign zone to a playable level — same mobs, harder stats, better loot. */
object CampaignScaler {
    const val LEVEL_STAT_GROWTH = 1.35
    const val LEVEL_REWARD_GROWTH = 1.25

    fun maxPlayableLevel(bestLevelBeaten: Int): Int = (bestLevelBeaten + 1).coerceAtLeast(1)

    fun resolve(zone: CampaignZone, level: Int): CampaignRun {
        val lvl = level.coerceAtLeast(1)
        val statMult = LEVEL_STAT_GROWTH.pow((lvl - 1).toDouble())
        val rewardMult = LEVEL_REWARD_GROWTH.pow((lvl - 1).toDouble())
        return CampaignRun(
            campaignId = zone.id,
            level = lvl,
            displayName = "${zone.name} · Lvl $lvl",
            recommendedPower = (zone.baseRecommendedPower * statMult).roundToInt(),
            enemyIds = zone.enemyIds,
            enemyStatMultiplier = statMult.toFloat(),
            crownRewardMin = (zone.crownRewardMin * rewardMult).roundToInt().coerceAtLeast(1),
            crownRewardMax = (zone.crownRewardMax * rewardMult).roundToInt().coerceAtLeast(1),
            materialRewards = zone.materialRewards.map { reward ->
                reward.copy(
                    min = (reward.min * rewardMult).roundToInt(),
                    max = (reward.max * rewardMult).roundToInt().coerceAtLeast(reward.min)
                )
            }
        )
    }
}
