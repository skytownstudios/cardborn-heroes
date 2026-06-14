package com.skytownstudios.cardbornheroes.data

import kotlin.random.Random

/**
 * **Ascendant Pack** — the core ranked booster line.
 * 8 slots: hero / gear / crafting material at pack tier or lower (weighted).
 * 9th slot: guaranteed pack-tier hero or gear, small chance one tier higher (Cardborn from Mythic only).
 *
 * Drop tables and formulas: [docs/ascendant-packs.md] in repo root.
 */
object PackGenerator {
    const val PACK_LINE_ASCENDANT = "ascendant"

    val PACK_TIERS = listOf("basic", "common", "uncommon", "rare", "epic", "mythic")
    val ALL_TIERS = PACK_TIERS + "cardborn"

    /**
     * Target per-slot rate for the pack's own tier on slots 1–8 (index = pack tier).
     * Lower tiers split the remainder with geometric decay (base 4).
     */
    val TOP_TIER_SLOT_RATE = floatArrayOf(1.0f, 0.25f, 0.12f, 0.09f, 0.07f, 0.06f)

    /** Chance 9th slot upgrades one tier (index = pack tier). Mythic → Cardborn uses last value. */
    val GUARANTEED_UPGRADE_CHANCE = floatArrayOf(0.40f, 0.28f, 0.20f, 0.14f, 0.08f, 0.025f)

    private const val TIER_WEIGHT_BASE = 4

    fun generate(content: ContentRepository, pack: PackDef): List<PackRevealCard> {
        require(pack.packLine == PACK_LINE_ASCENDANT) { "Unknown pack line: ${pack.packLine}" }
        return generateAscendant(content, pack.tier)
    }

    private fun generateAscendant(content: ContentRepository, packTier: String): List<PackRevealCard> {
        val packIndex = PACK_TIERS.indexOf(packTier)
        require(packIndex >= 0) { "Invalid pack tier $packTier" }

        val slotKinds = listOf("hero", "hero", "hero", "gear", "gear", "gear", "material", "material").shuffled()
        val regular = slotKinds.map { kind ->
            val rolledTier = rollTierAtOrBelow(packIndex)
            rollSlot(content, kind, rolledTier, isGuaranteedSlot = false)
        }

        val guaranteedTier = rollGuaranteedTier(packIndex)
        val guaranteedKind = if (Random.nextBoolean()) "hero" else "gear"
        val guaranteed = rollSlot(content, guaranteedKind, guaranteedTier, isGuaranteedSlot = true)

        return regular + guaranteed
    }

    /** Weights for tiers 0..packIndex. Top tier weight is calibrated to [TOP_TIER_SLOT_RATE]. */
    fun tierWeights(packIndex: Int): FloatArray {
        if (packIndex == 0) return floatArrayOf(1f)

        val targetTop = TOP_TIER_SLOT_RATE[packIndex]
        val weights = FloatArray(packIndex + 1)
        var sumLower = 0f
        for (i in 0 until packIndex) {
            weights[i] = lowerTierWeight(packIndex, i)
            sumLower += weights[i]
        }
        weights[packIndex] = targetTop * sumLower / (1f - targetTop)
        return weights
    }

    /** Geometric decay for tiers below the pack tier: 4^(packIndex - tierIndex). */
    private fun lowerTierWeight(packIndex: Int, tierIndex: Int): Float {
        val exp = packIndex - tierIndex
        var w = 1f
        repeat(exp) { w *= TIER_WEIGHT_BASE }
        return w
    }

    fun rollTierAtOrBelow(packIndex: Int): String {
        val weights = tierWeights(packIndex)
        val sum = weights.sum()
        var roll = Random.nextFloat() * sum
        for (i in 0..packIndex) {
            roll -= weights[i]
            if (roll < 0) return PACK_TIERS[i]
        }
        return PACK_TIERS[0]
    }

    /** Per-tier probability for slots 1–8 (single roll). */
    fun tierProbabilities(packTier: String): Map<String, Float> {
        val p = PACK_TIERS.indexOf(packTier)
        if (p < 0) return emptyMap()
        val weights = tierWeights(p)
        val sum = weights.sum()
        return (0..p).associate { i -> PACK_TIERS[i] to weights[i] / sum }
    }

    private fun rollGuaranteedTier(packIndex: Int): String {
        val upgradeChance = GUARANTEED_UPGRADE_CHANCE[packIndex]
        if (Random.nextFloat() < upgradeChance) {
            return when (packIndex) {
                PACK_TIERS.lastIndex -> "cardborn"
                else -> PACK_TIERS[packIndex + 1]
            }
        }
        return PACK_TIERS[packIndex]
    }

    /** P(exact tier) on 9th slot including upgrade roll. */
    fun guaranteedSlotProbabilities(packTier: String): Map<String, Float> {
        val p = PACK_TIERS.indexOf(packTier)
        if (p < 0) return emptyMap()
        val upgrade = GUARANTEED_UPGRADE_CHANCE[p]
        val result = mutableMapOf(PACK_TIERS[p] to (1f - upgrade))
        when {
            p == PACK_TIERS.lastIndex -> result["cardborn"] = upgrade
            else -> {
                val next = PACK_TIERS[p + 1]
                result[next] = (result[next] ?: 0f) + upgrade
            }
        }
        return result
    }

    private fun rollSlot(
        content: ContentRepository,
        kind: String,
        tier: String,
        isGuaranteedSlot: Boolean
    ): PackRevealCard {
        return when (kind) {
            "hero" -> {
                val pool = content.heroesOfTier(tier)
                val h = pool.random()
                PackRevealCard("hero", h.id, h.name, h.art, tier, isGuaranteedSlot)
            }
            "gear" -> {
                val pool = content.gearOfTier(tier)
                val g = pool.random()
                PackRevealCard("gear", g.id, g.name, g.art, tier, isGuaranteedSlot)
            }
            else -> {
                val mat = content.materials.random()
                val tierIndex = ALL_TIERS.indexOf(tier).coerceAtLeast(0)
                val qty = (tierIndex + 1) * 3
                PackRevealCard(
                    type = "material",
                    id = mat.id,
                    name = mat.name,
                    art = mat.art,
                    tier = tier,
                    isGuaranteedSlot = isGuaranteedSlot,
                    qty = qty
                )
            }
        }
    }
}
