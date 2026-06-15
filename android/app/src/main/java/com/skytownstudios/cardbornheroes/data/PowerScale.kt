package com.skytownstudios.cardbornheroes.data

import kotlin.math.pow

/** Normalized combat power: life + attack + defense + energy. */
object PowerScale {
    val TIER_ORDER = listOf("basic", "common", "uncommon", "rare", "epic", "mythic", "cardborn")
    private const val STAR_GROWTH = 1.5

    fun combatPower(stats: HeroStats): Int = stats.hp + stats.atk + stats.def + stats.energy

    fun tierIndex(tier: String): Int = TIER_ORDER.indexOf(tier).coerceAtLeast(0)

    /** Stars within a tier — each star is ×1.5 (0★=10 → 1★≈15 on same card). */
    fun starMultiplier(stars: Int): Float = STAR_GROWTH.pow(stars.coerceAtLeast(0)).toFloat()

    /**
     * Cross-tier effective rank for display / enemy scaling.
     * basic 5★ and common 0★ share the same effective rank (5).
     */
    fun effectiveStarLevel(tier: String, stars: Int): Int = tierIndex(tier) * 5 + stars

    fun scaledStats(base: HeroStats, stars: Int): HeroStats {
        val mult = starMultiplier(stars)
        return HeroStats(
            hp = (base.hp * mult).toInt().coerceAtLeast(1),
            atk = (base.atk * mult).toInt().coerceAtLeast(1),
            def = (base.def * mult).toInt().coerceAtLeast(0),
            energy = (base.energy * mult).toInt().coerceAtLeast(0)
        )
    }

    fun applyGearBonus(stats: HeroStats, bonus: GearBonus): HeroStats = HeroStats(
        hp = stats.hp + bonus.hp,
        atk = stats.atk + bonus.atk,
        def = stats.def + bonus.def,
        energy = stats.energy + bonus.energy
    )
}
