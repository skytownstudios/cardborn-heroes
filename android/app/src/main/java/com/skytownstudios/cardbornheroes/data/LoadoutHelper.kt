package com.skytownstudios.cardbornheroes.data

object LoadoutHelper {
    fun attackStyle(loadout: HeroLoadout, content: ContentRepository): String {
        val main = loadout.mainHandGearId.takeIf { it.isNotEmpty() }?.let { content.gear(it) }
        val off = loadout.offHandGearId.takeIf { it.isNotEmpty() }?.let { content.gear(it) }
        return when {
            main == null && off == null -> "unarmed"
            main != null && main.hands >= 2 -> "melee_2h"
            main != null && off != null -> "dual_1h"
            main != null -> "melee_1h"
            off != null -> "melee_1h"
            else -> "unarmed"
        }
    }

    fun mergedStats(loadout: HeroLoadout, content: ContentRepository): HeroStats? {
        val hero = content.hero(loadout.heroId) ?: return null
        var stats = PowerScale.scaledStats(hero.stats, loadout.heroStars)
        listOf(loadout.mainHandGearId, loadout.offHandGearId).forEach { gearId ->
            if (gearId.isEmpty()) return@forEach
            content.gear(gearId)?.bonus?.let { b ->
                stats = PowerScale.applyGearBonus(stats, b)
            }
        }
        return stats
    }

    fun mainHandArt(loadout: HeroLoadout, content: ContentRepository): String? =
        loadout.mainHandGearId.takeIf { it.isNotEmpty() }?.let { content.gear(it)?.battleArt }

    fun offHandArt(loadout: HeroLoadout, content: ContentRepository): String? =
        loadout.offHandGearId.takeIf { it.isNotEmpty() }?.let { content.gear(it)?.battleArt }

    fun handIsBuilt(hand: Hand): Boolean =
        hand.heroSlots.any { slot ->
            !slot.isEmpty && (slot.mainHandGearId.isNotEmpty() || slot.offHandGearId.isNotEmpty())
        }

    fun canEquipMainHand(gear: GearDef, hero: HeroDef, loadout: HeroLoadout, content: ContentRepository): Boolean {
        if (hero.role !in gear.compatibleRoles) return false
        if (gear.hand == "off") return false
        if (gear.hands >= 2 && loadout.offHandGearId.isNotEmpty()) return false
        return true
    }

    fun canEquipOffHand(gear: GearDef, hero: HeroDef, loadout: HeroLoadout, content: ContentRepository): Boolean {
        if (hero.role !in gear.compatibleRoles) return false
        if (gear.hand == "main") return false
        if (gear.hands >= 2) return false
        loadout.mainHandGearId.takeIf { it.isNotEmpty() }?.let { mainId ->
            content.gear(mainId)?.let { if (it.hands >= 2) return false }
        }
        return true
    }
}
