package com.skytownstudios.cardbornheroes.data

/** Human-readable odds for Ascendant packs (shown in Shop). */
object AscendantPackOdds {
    fun summary(packTier: String): String {
        val slot = PackGenerator.tierProbabilities(packTier)
        val top = slot[packTier]?.let { pct(it) } ?: "—"
        val g = PackGenerator.guaranteedSlotProbabilities(packTier)
        val gTop = g[packTier]?.let { pct(it) } ?: "—"
        val upgrade = g.entries.filter { it.key != packTier }.maxByOrNull { it.value }
        val upText = upgrade?.let { " · #9 upgrade ${it.key} ${pct(it.value)}" } ?: ""
        return "Slots 1–8: $top at ${packTier.replaceFirstChar { c -> c.uppercase() }}+ · #9 $gTop $packTier$upText"
    }

    private fun pct(v: Float) = "${(v * 100).toInt()}%"
}
