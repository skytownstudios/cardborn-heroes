package com.skytownstudios.cardbornheroes.data

object HandPower {
    fun calculate(hand: Hand, content: ContentRepository): Int {
        var power = 0
        for (slot in hand.heroSlots) {
            if (slot.isEmpty) continue
            LoadoutHelper.mergedStats(slot, content)?.let { stats ->
                power += PowerScale.combatPower(stats)
            }
        }
        return power
    }

    fun farmMultiplier(handPower: Int): Float =
        (handPower / 20f).coerceIn(0.5f, 3.0f)
}
