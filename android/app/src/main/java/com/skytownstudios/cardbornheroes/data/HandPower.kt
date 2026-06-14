package com.skytownstudios.cardbornheroes.data

object HandPower {
    fun calculate(hand: Hand, content: ContentRepository): Int {
        var power = 0
        for (slot in hand.heroSlots) {
            if (slot.isEmpty) continue
            LoadoutHelper.mergedStats(slot, content)?.let { stats ->
                power += stats.hp + stats.atk + stats.def
            }
        }
        return power
    }

    fun farmMultiplier(handPower: Int): Float =
        (handPower / 500f).coerceIn(0.5f, 3.0f)
}
