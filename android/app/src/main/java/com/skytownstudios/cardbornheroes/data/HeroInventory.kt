package com.skytownstudios.cardbornheroes.data

/** Inventory stacks keyed by hero id + star rank (e.g. hero_knight@2). */
object HeroInventory {
    const val MAX_STARS = 5
    private const val SEP = "@"

    fun stackKey(heroId: String, stars: Int): String = "$heroId$SEP$stars"

    fun parseStackKey(key: String): Pair<String, Int> {
        val idx = key.lastIndexOf(SEP)
        if (idx < 0) return key to 0
        val id = key.substring(0, idx)
        val stars = key.substring(idx + 1).toIntOrNull() ?: 0
        return id to stars
    }

    fun count(counts: Map<String, Int>, heroId: String, stars: Int): Int =
        counts[stackKey(heroId, stars)] ?: 0

    fun add(counts: Map<String, Int>, heroId: String, stars: Int, qty: Int): Map<String, Int> {
        if (qty <= 0) return counts
        val key = stackKey(heroId, stars)
        val next = counts.toMutableMap()
        next[key] = (next[key] ?: 0) + qty
        return next
    }

    fun remove(counts: Map<String, Int>, heroId: String, stars: Int, qty: Int): Map<String, Int> {
        if (qty <= 0) return counts
        val key = stackKey(heroId, stars)
        val left = (counts[key] ?: 0) - qty
        val next = counts.toMutableMap()
        if (left <= 0) next.remove(key) else next[key] = left
        return next
    }

    /** Migrate flat heroCounts from schema v2 to star stacks. */
    fun migrateFlatCounts(flat: Map<String, Int>): Map<String, Int> {
        val out = mutableMapOf<String, Int>()
        flat.forEach { (key, qty) ->
            if (qty <= 0) return@forEach
            val (id, stars) = parseStackKey(key)
            out[stackKey(id, stars)] = (out[stackKey(id, stars)] ?: 0) + qty
        }
        return out
    }

    fun stacksForHero(counts: Map<String, Int>, heroId: String): List<Pair<Int, Int>> =
        counts.mapNotNull { (key, qty) ->
            val (id, stars) = parseStackKey(key)
            if (id == heroId && qty > 0) stars to qty else null
        }.sortedBy { it.first }

    fun allStacks(counts: Map<String, Int>): List<HeroStack> =
        counts.mapNotNull { (key, qty) ->
            if (qty <= 0) return@mapNotNull null
            val (id, stars) = parseStackKey(key)
            HeroStack(id, stars, qty)
        }.sortedWith(compareBy({ it.heroId }, { it.stars }))

    fun starStatMultiplier(stars: Int): Float = PowerScale.starMultiplier(stars)
}

data class HeroStack(val heroId: String, val stars: Int, val count: Int)
