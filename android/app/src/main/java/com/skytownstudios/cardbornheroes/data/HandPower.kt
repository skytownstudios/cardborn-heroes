package com.skytownstudios.cardbornheroes.data

object HandPower {
    fun calculate(hand: Hand, content: ContentRepository): Int {
        var power = 0
        for (slot in hand.slots) {
            if (slot.isEmpty) continue
            when (slot.type) {
                "hero" -> {
                    val h = content.hero(slot.cardId) ?: continue
                    power += h.stats.hp + h.stats.atk + h.stats.def
                }
                "gear" -> {
                    val g = content.gear(slot.cardId) ?: continue
                    power += g.bonus.hp + g.bonus.atk + g.bonus.def
                }
            }
        }
        return power
    }

    fun farmMultiplier(handPower: Int): Float =
        (handPower / 500f).coerceIn(0.5f, 3.0f)
}
