package com.skytownstudios.cardbornheroes.data

import kotlin.random.Random

object FarmEngine {
    const val TICK_MS = 60_000L
    const val OFFLINE_CAP_MS = 8 * 60 * 60 * 1000L

    data class TickResult(
        val crownsGained: Int,
        val materialGains: Map<String, Int>,
        val heroDrops: Map<String, Int>,
        val gearDrops: Map<String, Int>,
        val packDrops: Map<String, Int>,
        val newLastTickMs: Long,
        val crownsRemainder: Double,
        val materialRemainder: Double
    )

    fun processTicks(
        state: PlayerState,
        content: ContentRepository,
        nowMs: Long = System.currentTimeMillis()
    ): TickResult? {
        val elapsed = (nowMs - state.lastFarmTickEpochMs).coerceAtMost(OFFLINE_CAP_MS)
        val ticks = (elapsed / TICK_MS).toInt()
        if (ticks <= 0) return null

        val farm = content.farm(state.activeFarmId) ?: return null
        val hand = state.hands.getOrNull(state.activeHandIndex) ?: Hand()
        val power = HandPower.calculate(hand, content)
        val mult = HandPower.farmMultiplier(power)

        var crownsRem = state.farmCrownsRemainder
        var matRem = state.farmMaterialRemainder
        var totalCrowns = 0
        val matGains = mutableMapOf<String, Int>()
        val heroDrops = mutableMapOf<String, Int>()
        val gearDrops = mutableMapOf<String, Int>()
        val packDrops = mutableMapOf<String, Int>()

        repeat(ticks) {
            crownsRem += farm.baseCrownsPerHour * mult / 60.0
            if (crownsRem >= 1.0) {
                val gain = crownsRem.toInt()
                totalCrowns += gain
                crownsRem -= gain
            }

            if (farm.id == "cardborn_vault") {
                matRem += 1.0 * mult / 60.0
                if (matRem >= 1.0) {
                    val gain = matRem.toInt().coerceAtLeast(1)
                    matRem -= gain
                    for (mat in content.materials) {
                        matGains[mat.id] = (matGains[mat.id] ?: 0) + gain
                    }
                }
            } else {
                matRem += farm.primaryQtyPerHour * mult / 60.0
                if (matRem >= 1.0) {
                    val gain = matRem.toInt()
                    matRem -= gain
                    matGains[farm.primaryMaterial] = (matGains[farm.primaryMaterial] ?: 0) + gain
                }
            }

            farm.secondaryDrop?.let { drop ->
                val chance = drop.ratePerHour * mult / 60.0
                if (Random.nextDouble() < chance) {
                    when (drop.type) {
                        "hero" -> {
                            val pool = content.heroesOfTier(drop.tier)
                            if (pool.isNotEmpty()) {
                                val h = pool.random()
                                heroDrops[h.id] = (heroDrops[h.id] ?: 0) + 1
                            }
                        }
                        "gear" -> {
                            val pool = content.gearOfTier(drop.tier)
                            if (pool.isNotEmpty()) {
                                val g = pool.random()
                                gearDrops[g.id] = (gearDrops[g.id] ?: 0) + 1
                            }
                        }
                    }
                }
            }

            for (drop in farm.packDrops) {
                if (drop.ratePerHour <= 0) continue
                val chance = drop.ratePerHour * mult / 60.0
                if (Random.nextDouble() < chance) {
                    packDrops[drop.packId] = (packDrops[drop.packId] ?: 0) + 1
                }
            }
        }

        return TickResult(
            crownsGained = totalCrowns,
            materialGains = matGains,
            heroDrops = heroDrops,
            gearDrops = gearDrops,
            packDrops = packDrops,
            newLastTickMs = state.lastFarmTickEpochMs + ticks * TICK_MS,
            crownsRemainder = crownsRem,
            materialRemainder = matRem
        )
    }
}
