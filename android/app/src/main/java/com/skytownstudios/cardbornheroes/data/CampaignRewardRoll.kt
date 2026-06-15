package com.skytownstudios.cardbornheroes.data

import kotlin.random.Random

object CampaignRewardRoll {
    fun rollCrowns(min: Int, max: Int): Int =
        if (max <= min) min else Random.nextInt(min, max + 1)

    fun rollMaterials(rewards: List<CampaignMaterialReward>): Map<String, Int> {
        val out = mutableMapOf<String, Int>()
        for (reward in rewards) {
            if (reward.weight <= 0) continue
            if (Random.nextDouble() > reward.weight.coerceIn(0.0, 1.0)) continue
            val qty = if (reward.max <= reward.min) reward.min
            else Random.nextInt(reward.min, reward.max + 1)
            if (qty > 0) out[reward.id] = (out[reward.id] ?: 0) + qty
        }
        return out
    }
}
